package services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dao.CustomerDAO;
import dao.OrderDAO;
import dao.OrderItemDAO;
import dao.PaymentDAO;
import dao.ProductVariantDAO;
import dao.StockMovementDAO;
import model.Customer;
import model.MovementType;
import model.Order;
import model.OrderItem;
import model.OrderStatus;
import model.Payment;
import model.PaymentMethod;
import model.ProductVariant;
import model.Role;
import model.User;
import utils.BusinessException;
import utils.Formatter;
import utils.Session;
import utils.Transaction;

/**
 * Nghiệp vụ bán hàng tại quầy (mục 2.3 và mục 4, BR02, BR03, BR05, BR06, BR08..BR11).
 */
public class OrderService extends BaseService {
    /** Loại chứng từ ghi vào stock_movements.ref_type. */
    public static final String REF_TYPE = "ORDER";
    /** Tỷ lệ giảm giá tối đa của nhân viên trên tổng đơn (BR08). */
    public static final BigDecimal STAFF_MAX_DISCOUNT_RATE = new BigDecimal("0.20");

    private final OrderDAO orderDAO = new OrderDAO();
    private final OrderItemDAO itemDAO = new OrderItemDAO();
    private final PaymentDAO paymentDAO = new PaymentDAO();
    private final ProductVariantDAO variantDAO = new ProductVariantDAO();
    private final StockMovementDAO movementDAO = new StockMovementDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();

    /**
     * Tạo đơn mới trong bộ nhớ ở trạng thái DRAFT cho người dùng hiện tại (bước 1 của luồng bán).
     *
     * @return đơn nháp chưa lưu
     */
    public Order newOrder() {
        User u = Session.requireLogin();
        Order o = new Order();
        o.setUserId(u.getId());
        o.setUserName(u.getFullName());
        o.setStatus(OrderStatus.DRAFT);
        o.setCreatedAt(LocalDateTime.now());
        return o;
    }

    /**
     * Tạo dòng hàng mới cho một biến thể (bước 2). Kiểm tra biến thể còn kinh doanh (BR02)
     * và cảnh báo sớm khi số lượng vượt tồn hiện tại.
     *
     * @param order     đơn đang lập
     * @param variantId mã biến thể
     * @param quantity  số lượng muốn thêm
     * @return dòng hàng đã thêm hoặc đã cộng dồn trong đơn
     * @throws BusinessException nếu biến thể ngừng kinh doanh hoặc vượt tồn
     */
    public OrderItem addItem(Order order, Long variantId, int quantity) {
        Session.requireLogin();
        if (quantity <= 0) {
            throw new BusinessException("Số lượng phải lớn hơn 0.");
        }
        ProductVariant v = db(() -> variantDAO.getVariantById(variantId));
        if (v == null) {
            throw new BusinessException("Không tìm thấy biến thể.");
        }
        if (!v.isActive()) {
            throw new BusinessException("Biến thể " + v.getSku() + " đã ngừng kinh doanh, không thể thêm vào đơn (BR02).");
        }
        OrderItem existing = null;
        for (OrderItem it : order.getItems()) {
            if (it.getVariantId().equals(variantId)) {
                existing = it;
            }
        }
        int newQty = quantity + (existing == null ? 0 : existing.getQuantity());
        if (newQty > v.getStockQuantity()) {
            throw new BusinessException("Không đủ hàng: " + v.getSku() + " chỉ còn " + v.getStockQuantity()
                    + " sản phẩm.");
        }
        if (existing != null) {
            existing.setQuantity(newQty);
            existing.setUnitPrice(v.getSellingPrice());
            recalculate(order);
            return existing;
        }
        OrderItem it = new OrderItem();
        it.setVariantId(v.getId());
        it.setQuantity(quantity);
        it.setUnitPrice(v.getSellingPrice());
        it.setUnitCost(v.getCostPrice());
        it.setDiscountAmount(BigDecimal.ZERO);
        it.setSku(v.getSku());
        it.setVariantName(v.getDisplayName());
        order.getItems().add(it);
        recalculate(order);
        return it;
    }

    /**
     * Lấy tồn kho hiện tại của biến thể để cảnh báo sớm khi sửa số lượng.
     *
     * @param variantId mã biến thể
     * @return số lượng tồn hiện tại
     */
    public int getAvailableStock(Long variantId) {
        Session.requireLogin();
        return db(() -> variantDAO.findStockQuantityById(variantId));
    }

    /**
     * Tính lại tổng tiền đơn từ các dòng (BR09):
     * tổng = tổng (số lượng x đơn giá - giảm dòng) - giảm cấp đơn. Không nhập tay.
     *
     * @param order đơn cần tính
     * @return tổng tiền sau khi tính
     */
    public static BigDecimal recalculate(Order order) {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (OrderItem it : order.getItems()) {
            subtotal = subtotal.add(it.getLineTotal());
        }
        BigDecimal total = subtotal.subtract(nz(order.getDiscountAmount()));
        order.setTotalAmount(total);
        return total;
    }

    /**
     * Tổng tiền hàng trước mọi giảm giá = tổng (số lượng x đơn giá).
     *
     * @param order đơn hàng
     * @return tổng tiền gộp
     */
    public static BigDecimal grossAmount(Order order) {
        BigDecimal g = BigDecimal.ZERO;
        for (OrderItem it : order.getItems()) {
            g = g.add(it.getGrossAmount());
        }
        return g;
    }

    /**
     * Tổng giảm giá của đơn = tổng giảm dòng + giảm cấp đơn.
     *
     * @param order đơn hàng
     * @return tổng giảm giá
     */
    public static BigDecimal totalDiscount(Order order) {
        BigDecimal d = nz(order.getDiscountAmount());
        for (OrderItem it : order.getItems()) {
            d = d.add(nz(it.getDiscountAmount()));
        }
        return d;
    }

    /**
     * Kiểm tra giảm giá (BR08): giảm dòng không vượt thành tiền dòng, giảm đơn không vượt
     * tiền hàng sau giảm dòng; nhân viên giảm tối đa 20% tổng đơn, Admin không giới hạn.
     *
     * @param order đơn cần kiểm tra
     * @param user  người lập đơn
     * @throws BusinessException nếu vi phạm
     */
    public void validateDiscounts(Order order, User user) {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (OrderItem it : order.getItems()) {
            BigDecimal d = nz(it.getDiscountAmount());
            if (d.signum() < 0) {
                throw new BusinessException("Giảm giá dòng không được âm.");
            }
            if (d.compareTo(it.getGrossAmount()) > 0) {
                throw new BusinessException("Giảm giá dòng " + it.getSku() + " vượt quá thành tiền (BR08).");
            }
            subtotal = subtotal.add(it.getLineTotal());
        }
        BigDecimal od = nz(order.getDiscountAmount());
        if (od.signum() < 0) {
            throw new BusinessException("Giảm giá đơn không được âm.");
        }
        if (od.compareTo(subtotal) > 0) {
            throw new BusinessException("Giảm giá đơn vượt quá tiền hàng (BR08).");
        }
        if (user.getRole() != Role.ADMIN) {
            BigDecimal max = grossAmount(order).multiply(STAFF_MAX_DISCOUNT_RATE).setScale(2, RoundingMode.DOWN);
            if (totalDiscount(order).compareTo(max) > 0) {
                throw new BusinessException("Nhân viên chỉ được giảm tối đa 20% tổng đơn ("
                        + Formatter.money(max) + " đ) (BR08).");
            }
        }
    }

    /**
     * Lưu đơn ở trạng thái DRAFT (thêm mới hoặc ghi đè dòng hàng của đơn nháp cũ).
     * Đơn giá được lấy lại theo giá bán hiện tại.
     *
     * @param order đơn cần lưu
     * @return đơn đã lưu (có id)
     */
    public Order saveDraft(Order order) {
        User user = Session.requireLogin();
        if (order.getStatus() != null && order.getStatus() != OrderStatus.DRAFT) {
            throw new BusinessException("Chỉ lưu được đơn đang ở trạng thái Nháp.");
        }
        if (order.getItems().isEmpty()) {
            throw new BusinessException("Đơn hàng chưa có sản phẩm nào.");
        }
        Set<Long> seen = new HashSet<>();
        for (OrderItem it : order.getItems()) {
            if (it.getQuantity() <= 0) {
                throw new BusinessException("Số lượng của " + it.getSku() + " phải lớn hơn 0.");
            }
            if (!seen.add(it.getVariantId())) {
                throw new BusinessException("Biến thể " + it.getSku() + " bị lặp trong đơn.");
            }
            ProductVariant v = db(() -> variantDAO.getVariantById(it.getVariantId()));
            if (v == null || !v.isActive()) {
                throw new BusinessException("Biến thể " + it.getSku() + " đã ngừng kinh doanh (BR02).");
            }
            it.setUnitPrice(v.getSellingPrice());
            it.setUnitCost(v.getCostPrice());
        }
        recalculate(order);
        validateDiscounts(order, user);
        order.setStatus(OrderStatus.DRAFT);
        if (order.getUserId() == null) {
            order.setUserId(user.getId());
        }
        return Transaction.execute(conn -> {
            if (order.getId() == null) {
                orderDAO.insert(order);
            } else {
                if (!orderDAO.updateDraft(order)) {
                    throw new BusinessException("Đơn #" + order.getId() + " không còn ở trạng thái Nháp.");
                }
                itemDAO.deleteByOrderId(order.getId());
            }
            itemDAO.insertAll(order.getId(), order.getItems());
            return order;
        });
    }

    /**
     * Xác nhận và thanh toán đơn (bước 5 của luồng bán). Đơn được lưu nháp trước,
     * sau đó trong MỘT giao dịch: kiểm tra tồn lần cuối, chụp giá bán và giá vốn,
     * trừ kho có điều kiện, ghi StockMovement, ghi Payment, chuyển đơn sang PAID.
     * Nếu lỗi, giao dịch hoàn tác: đơn vẫn là DRAFT và kho không đổi (BR05, BR06, BR10).
     *
     * @param order  đơn cần thanh toán
     * @param method phương thức thanh toán
     * @return đơn đã thanh toán (đọc lại từ database)
     */
    public Order confirmAndPay(Order order, PaymentMethod method) {
        User user = Session.requireLogin();
        if (method == null) {
            throw new BusinessException("Phải chọn phương thức thanh toán.");
        }
        saveDraft(order);
        Long orderId = order.getId();
        Transaction.execute(conn -> {
            List<OrderItem> items = itemDAO.findByOrderId(orderId);
            for (OrderItem it : items) {
                ProductVariant v = variantDAO.getVariantForUpdate(it.getVariantId());
                if (v == null || !v.isActive()) {
                    throw new BusinessException("Biến thể " + it.getSku() + " đã ngừng kinh doanh (BR02).");
                }
                // BR06: chụp giá bán và giá vốn tại thời điểm bán
                it.setUnitPrice(v.getSellingPrice());
                it.setUnitCost(v.getCostPrice());
                itemDAO.updateSnapshot(it.getId(), v.getSellingPrice(), v.getCostPrice());
                // BR05: trừ kho có điều kiện, 0 dòng cập nhật = hết hàng -> rollback
                if (!variantDAO.decreaseStockIfEnough(it.getVariantId(), it.getQuantity())) {
                    throw new BusinessException("Không đủ hàng cho " + it.getSku() + " (còn "
                            + v.getStockQuantity() + "). Đơn vẫn ở trạng thái Nháp.");
                }
                movementDAO.record(it.getVariantId(), MovementType.SALE, -it.getQuantity(), REF_TYPE, orderId);
            }
            order.setItems(items);
            recalculate(order);
            validateDiscounts(order, user);
            if (order.getTotalAmount().signum() < 0) {
                throw new BusinessException("Tổng tiền đơn không hợp lệ.");
            }
            if (!orderDAO.updateDraft(order)) {
                throw new BusinessException("Đơn không còn ở trạng thái Nháp.");
            }
            if (paymentDAO.existsForOrder(orderId)) {
                throw new BusinessException("Đơn đã có thanh toán (BR10).");
            }
            Payment p = new Payment();
            p.setOrderId(orderId);
            p.setAmount(order.getTotalAmount());
            p.setMethod(method);
            p.setPaidAt(LocalDateTime.now());
            paymentDAO.insert(p);
            if (!orderDAO.changeStatus(orderId, OrderStatus.DRAFT, OrderStatus.PAID)) {
                throw new BusinessException("Đơn không còn ở trạng thái Nháp.");
            }
            return null;
        });
        return getOrder(orderId);
    }

    /**
     * Hủy đơn (BR11). Đơn DRAFT: ai cũng hủy được, không ảnh hưởng kho.
     * Đơn PAID: chỉ Admin; hoàn kho, ghi StockMovement loại CANCEL và đánh dấu CANCELLED
     * trong một giao dịch.
     *
     * @param orderId mã đơn
     */
    public void cancelOrder(Long orderId) {
        Session.requireLogin();
        Order o = db(() -> orderDAO.findById(orderId));
        if (o == null) {
            throw new BusinessException("Không tìm thấy đơn #" + orderId + ".");
        }
        if (o.getStatus() == OrderStatus.CANCELLED) {
            throw new BusinessException("Đơn đã bị hủy trước đó.");
        }
        if (o.getStatus() == OrderStatus.DRAFT) {
            if (!db(() -> orderDAO.changeStatus(orderId, OrderStatus.DRAFT, OrderStatus.CANCELLED))) {
                throw new BusinessException("Trạng thái đơn vừa thay đổi, hãy tải lại.");
            }
            return;
        }
        if (!Session.isAdmin()) {
            throw new BusinessException("Chỉ Admin được hủy đơn đã thanh toán (BR11).");
        }
        Transaction.execute(conn -> {
            if (!orderDAO.changeStatus(orderId, OrderStatus.PAID, OrderStatus.CANCELLED)) {
                throw new BusinessException("Trạng thái đơn vừa thay đổi, hãy tải lại.");
            }
            for (OrderItem it : itemDAO.findByOrderId(orderId)) {
                variantDAO.increaseStock(it.getVariantId(), it.getQuantity());
                movementDAO.record(it.getVariantId(), MovementType.CANCEL, it.getQuantity(), REF_TYPE, orderId);
            }
            return null;
        });
    }

    /**
     * Lấy đơn đầy đủ: thông tin chung, dòng hàng và phương thức thanh toán.
     *
     * @param orderId mã đơn
     * @return đơn hàng
     */
    public Order getOrder(Long orderId) {
        Session.requireLogin();
        Order o = db(() -> orderDAO.findById(orderId));
        if (o == null) {
            throw new BusinessException("Không tìm thấy đơn #" + orderId + ".");
        }
        o.setItems(db(() -> itemDAO.findByOrderId(orderId)));
        return o;
    }

    /**
     * Lấy thông tin thanh toán của đơn.
     *
     * @param orderId mã đơn
     * @return thanh toán hoặc null
     */
    public Payment getPayment(Long orderId) {
        Session.requireLogin();
        return db(() -> paymentDAO.findByOrderId(orderId));
    }

    /**
     * Tìm đơn theo khoảng thời gian, trạng thái, từ khóa.
     *
     * @param from    từ thời điểm (bao gồm)
     * @param to      đến thời điểm (không bao gồm)
     * @param status  trạng thái (null = tất cả)
     * @param keyword mã đơn hoặc tên/SĐT khách
     * @return danh sách đơn
     */
    public List<Order> searchOrders(LocalDateTime from, LocalDateTime to, OrderStatus status, String keyword) {
        Session.requireLogin();
        return db(() -> orderDAO.search(from, to, status, keyword));
    }

    /**
     * Tạo nội dung hóa đơn dạng văn bản để xem và in (bước 6).
     *
     * @param orderId mã đơn đã thanh toán
     * @return nội dung hóa đơn
     */
    public String buildInvoice(Long orderId) {
        Order o = getOrder(orderId);
        Payment p = getPayment(orderId);
        Customer c = o.getCustomerId() == null ? null : db(() -> customerDAO.findById(o.getCustomerId()));
        String line = repeat('-', 64);
        StringBuilder sb = new StringBuilder();
        sb.append(center("CỬA HÀNG GIÀY DÉP", 64)).append('\n');
        sb.append(center("HÓA ĐƠN BÁN HÀNG", 64)).append('\n');
        sb.append(line).append('\n');
        sb.append("Số HĐ    : ").append(o.getId()).append('\n');
        sb.append("Ngày     : ").append(Formatter.dateTime(o.getCreatedAt())).append('\n');
        sb.append("Thu ngân : ").append(o.getUserName()).append('\n');
        if (c != null) {
            sb.append("Khách    : ").append(c.getFullName())
                    .append(c.getPhone() == null ? "" : " - " + c.getPhone()).append('\n');
        }
        sb.append("Trạng thái: ").append(o.getStatus().getLabel()).append('\n');
        sb.append(line).append('\n');
        sb.append(String.format("%-28s %4s %14s %14s%n", "Sản phẩm", "SL", "Đơn giá", "Thành tiền"));
        sb.append(line).append('\n');
        for (OrderItem it : o.getItems()) {
            sb.append(it.getSku()).append(" - ").append(it.getVariantName()).append('\n');
            sb.append(String.format("%-28s %4d %14s %14s%n", "", it.getQuantity(),
                    Formatter.money(it.getUnitPrice()), Formatter.money(it.getGrossAmount())));
            if (nz(it.getDiscountAmount()).signum() > 0) {
                sb.append(String.format("%-48s %14s%n", "   Giảm giá dòng", "-" + Formatter.money(it.getDiscountAmount())));
            }
        }
        sb.append(line).append('\n');
        sb.append(String.format("%-48s %14s%n", "Tiền hàng", Formatter.money(grossAmount(o))));
        sb.append(String.format("%-48s %14s%n", "Tổng giảm giá", "-" + Formatter.money(totalDiscount(o))));
        sb.append(String.format("%-48s %14s%n", "TỔNG THANH TOÁN", Formatter.money(o.getTotalAmount())));
        if (p != null) {
            sb.append(String.format("%-48s %14s%n", "Thanh toán (" + p.getMethod().getLabel() + ")",
                    Formatter.money(p.getAmount())));
        }
        sb.append(line).append('\n');
        sb.append(center("Cảm ơn quý khách!", 64)).append('\n');
        return sb.toString();
    }

    /**
     * Lặp một ký tự n lần.
     *
     * @param ch ký tự
     * @param n  số lần
     * @return chuỗi kết quả
     */
    private static String repeat(char ch, int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append(ch);
        }
        return sb.toString();
    }

    /**
     * Căn giữa chuỗi trong độ rộng cho trước.
     *
     * @param s     chuỗi
     * @param width độ rộng
     * @return chuỗi đã thêm khoảng trắng bên trái
     */
    private static String center(String s, int width) {
        int pad = Math.max(0, (width - s.length()) / 2);
        return repeat(' ', pad) + s;
    }
}

package controller;

import java.awt.Component;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import model.Customer;
import model.Order;
import model.OrderItem;
import model.OrderStatus;
import model.PaymentMethod;
import model.ProductVariant;
import services.OrderService;
import services.PartnerService;
import services.ProductService;
import utils.BusinessException;
import utils.Formatter;
import view.InvoiceDialog;
import view.SalesPanel;
import view.UiKit;

/**
 * Xử lý luồng bán hàng tại quầy (mục 4): tạo đơn, thêm dòng, giảm giá, gắn khách,
 * lưu nháp, thanh toán, in hóa đơn và hủy đơn nháp.
 */
public class SalesController extends BaseController {
    private final SalesPanel view;
    private final OrderService orderService = new OrderService();
    private final ProductService productService = new ProductService();
    private final PartnerService partnerService = new PartnerService();
    private Order order;
    private Customer customer;
    private List<ProductVariant> found = new ArrayList<>();

    /**
     * Gắn controller vào màn hình bán hàng và đăng ký sự kiện.
     *
     * @param view màn hình bán hàng
     */
    public SalesController(SalesPanel view) {
        this.view = view;
        view.btnSearch.addActionListener(e -> handle(this::searchVariants));
        view.txtKeyword.addActionListener(e -> handle(this::searchVariants));
        view.btnAdd.addActionListener(e -> handle(this::addSelected));
        view.tblVariants.addMouseListener(new java.awt.event.MouseAdapter() {
            /**
             * Nhấp đúp vào biến thể để thêm nhanh vào đơn.
             *
             * @param e sự kiện chuột
             */
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    handle(SalesController.this::addSelected);
                }
            }
        });
        view.btnEditQty.addActionListener(e -> handle(this::editQuantity));
        view.btnLineDiscount.addActionListener(e -> handle(this::editLineDiscount));
        view.btnRemove.addActionListener(e -> handle(this::removeLine));
        view.btnApplyDiscount.addActionListener(e -> handle(this::applyOrderDiscount));
        view.txtOrderDiscount.addActionListener(e -> handle(this::applyOrderDiscount));
        view.btnFindCustomer.addActionListener(e -> handle(this::findCustomer));
        view.txtCustomer.addActionListener(e -> handle(this::findCustomer));
        view.btnNewCustomer.addActionListener(e -> handle(this::newCustomer));
        view.btnClearCustomer.addActionListener(e -> setCustomer(null));
        view.btnNewOrder.addActionListener(e -> handle(this::confirmNewOrder));
        view.btnSaveDraft.addActionListener(e -> handle(this::saveDraft));
        view.btnPay.addActionListener(e -> handle(this::pay));
        view.btnCancel.addActionListener(e -> handle(this::cancel));
        view.txtCashGiven.getDocument().addDocumentListener(new DocumentListener() {
            /**
             * Cập nhật tiền thối khi thêm ký tự.
             *
             * @param e sự kiện
             */
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateChange();
            }

            /**
             * Cập nhật tiền thối khi xóa ký tự.
             *
             * @param e sự kiện
             */
            @Override
            public void removeUpdate(DocumentEvent e) {
                updateChange();
            }

            /**
             * Không dùng với ô văn bản thường.
             *
             * @param e sự kiện
             */
            @Override
            public void changedUpdate(DocumentEvent e) {
                updateChange();
            }
        });
        handle(this::startNewOrder);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Component parent() {
        return view;
    }

    /**
     * Làm mới danh sách biến thể khi mở tab.
     */
    public void refresh() {
        handle(this::searchVariants);
    }

    /**
     * Mở một đơn nháp đã lưu để tiếp tục bán (gọi từ màn hình danh sách đơn).
     *
     * @param orderId mã đơn nháp
     */
    public void openDraft(Long orderId) {
        handle(() -> {
            Order o = orderService.getOrder(orderId);
            if (o.getStatus() != OrderStatus.DRAFT) {
                throw new BusinessException("Chỉ mở lại được đơn đang ở trạng thái Nháp.");
            }
            order = o;
            customer = null;
            if (o.getCustomerId() != null) {
                Customer c = new Customer();
                c.setId(o.getCustomerId());
                c.setFullName(o.getCustomerName());
                customer = c;
            }
            view.txtOrderDiscount.setText(Formatter.money(o.getDiscountAmount()));
            renderOrder();
        });
    }

    /**
     * Tìm biến thể đang kinh doanh theo tên/SKU/thương hiệu, size, màu.
     */
    private void searchVariants() {
        found = productService.searchVariants(view.txtKeyword.getText(), view.txtSize.getText(),
                view.txtColor.getText(), true);
        List<Object[]> rows = new ArrayList<>();
        for (ProductVariant v : found) {
            rows.add(new Object[] { v.getId(), v.getSku(), v.getProductName(), v.getBrandName(), v.getSize(),
                    v.getColor(), Formatter.money(v.getSellingPrice()), v.getStockQuantity() });
        }
        UiKit.fill(view.tblVariants, rows);
    }

    /**
     * Thêm biến thể đang chọn vào đơn với số lượng ở ô số lượng (cảnh báo sớm nếu vượt tồn).
     */
    private void addSelected() {
        ensureEditable();
        int row = UiKit.selectedModelRow(view.tblVariants);
        if (row < 0) {
            throw new BusinessException("Hãy chọn sản phẩm cần thêm.");
        }
        Long variantId = (Long) view.tblVariants.getModel().getValueAt(row, 0);
        int qty = (Integer) view.spnQuantity.getValue();
        orderService.addItem(order, variantId, qty);
        view.spnQuantity.setValue(1);
        renderOrder();
    }

    /**
     * Sửa số lượng dòng đang chọn trong đơn, kiểm tra tồn hiện tại.
     */
    private void editQuantity() {
        ensureEditable();
        OrderItem it = selectedLine();
        String s = JOptionPane.showInputDialog(view, "Số lượng mới cho " + it.getSku() + ":", it.getQuantity());
        if (s == null) {
            return;
        }
        int qty = Formatter.parseInt(s, "Số lượng");
        if (qty <= 0) {
            throw new BusinessException("Số lượng phải lớn hơn 0. Muốn bỏ sản phẩm hãy bấm Xóa dòng.");
        }
        int stock = orderService.getAvailableStock(it.getVariantId());
        if (qty > stock) {
            throw new BusinessException("Không đủ hàng: " + it.getSku() + " chỉ còn " + stock + " sản phẩm.");
        }
        it.setQuantity(qty);
        OrderService.recalculate(order);
        renderOrder();
    }

    /**
     * Nhập giảm giá (số tiền) cho dòng đang chọn (BR08).
     */
    private void editLineDiscount() {
        ensureEditable();
        OrderItem it = selectedLine();
        String s = JOptionPane.showInputDialog(view, "Số tiền giảm cho dòng " + it.getSku() + ":",
                Formatter.money(it.getDiscountAmount()));
        if (s == null) {
            return;
        }
        BigDecimal d = Formatter.parseMoney(s);
        BigDecimal old = it.getDiscountAmount();
        it.setDiscountAmount(d);
        try {
            orderService.validateDiscounts(order, utils.Session.requireLogin());
        } catch (BusinessException ex) {
            it.setDiscountAmount(old);
            throw ex;
        }
        OrderService.recalculate(order);
        renderOrder();
    }

    /**
     * Xóa dòng đang chọn khỏi đơn.
     */
    private void removeLine() {
        ensureEditable();
        OrderItem it = selectedLine();
        order.getItems().remove(it);
        OrderService.recalculate(order);
        renderOrder();
    }

    /**
     * Áp dụng giảm giá cấp đơn (BR08).
     */
    private void applyOrderDiscount() {
        ensureEditable();
        BigDecimal d = Formatter.parseMoney(view.txtOrderDiscount.getText());
        BigDecimal old = order.getDiscountAmount();
        order.setDiscountAmount(d);
        try {
            orderService.validateDiscounts(order, utils.Session.requireLogin());
        } catch (BusinessException ex) {
            order.setDiscountAmount(old);
            view.txtOrderDiscount.setText(Formatter.money(old));
            throw ex;
        }
        OrderService.recalculate(order);
        renderOrder();
    }

    /**
     * Tìm khách hàng theo SĐT/tên; một kết quả thì gắn luôn, nhiều kết quả thì cho chọn.
     */
    private void findCustomer() {
        String kw = view.txtCustomer.getText().trim();
        if (kw.isEmpty()) {
            throw new BusinessException("Nhập số điện thoại hoặc tên khách hàng để tìm.");
        }
        List<Customer> list = partnerService.searchCustomers(kw);
        if (list.isEmpty()) {
            if (UiKit.confirm(view, "Không tìm thấy khách hàng. Thêm khách hàng mới?")) {
                newCustomer();
            }
            return;
        }
        Customer chosen = list.get(0);
        if (list.size() > 1) {
            chosen = (Customer) JOptionPane.showInputDialog(view, "Chọn khách hàng:", "Khách hàng",
                    JOptionPane.QUESTION_MESSAGE, null, list.toArray(), list.get(0));
        }
        if (chosen != null) {
            setCustomer(chosen);
        }
    }

    /**
     * Thêm nhanh khách hàng mới và gắn vào đơn.
     */
    private void newCustomer() {
        JTextField name = new JTextField(18);
        JTextField phone = new JTextField(view.txtCustomer.getText().trim().matches("\\d+")
                ? view.txtCustomer.getText().trim() : "", 18);
        int r = JOptionPane.showConfirmDialog(view, UiKit.form(null, new String[] { "Họ tên:", "Số điện thoại:" },
                new javax.swing.JComponent[] { name, phone }), "Khách hàng mới", JOptionPane.OK_CANCEL_OPTION);
        if (r == JOptionPane.OK_OPTION) {
            setCustomer(partnerService.saveCustomer(null, name.getText(), phone.getText()));
        }
    }

    /**
     * Gắn (hoặc bỏ gắn khi null) khách hàng vào đơn.
     *
     * @param c khách hàng
     */
    private void setCustomer(Customer c) {
        if (order != null && order.getStatus() != OrderStatus.DRAFT) {
            return;
        }
        customer = c;
        if (order != null) {
            order.setCustomerId(c == null ? null : c.getId());
        }
        view.lblCustomer.setText(c == null ? "Khách lẻ" : c.toString());
        view.txtCustomer.setText("");
    }

    /**
     * Hỏi trước khi bỏ đơn đang lập dở để tạo đơn mới.
     */
    private void confirmNewOrder() {
        if (order != null && order.getStatus() == OrderStatus.DRAFT && order.getId() == null
                && !order.getItems().isEmpty()
                && !UiKit.confirm(view, "Đơn hiện tại chưa lưu. Bỏ đơn này để tạo đơn mới?")) {
            return;
        }
        startNewOrder();
    }

    /**
     * Tạo đơn mới (DRAFT) trong bộ nhớ và làm trống màn hình (bước 1).
     */
    private void startNewOrder() {
        order = orderService.newOrder();
        customer = null;
        view.txtOrderDiscount.setText("0");
        view.txtCashGiven.setText("");
        view.rdoCash.setSelected(true);
        renderOrder();
    }

    /**
     * Lưu đơn ở trạng thái nháp để thanh toán sau.
     */
    private void saveDraft() {
        ensureEditable();
        applyOrderDiscount();
        orderService.saveDraft(order);
        UiKit.info(view, "Đã lưu đơn nháp #" + order.getId() + ".");
        renderOrder();
    }

    /**
     * Xác nhận và thanh toán đơn (bước 4-6): gọi Service chạy giao dịch, rồi hiện hóa đơn.
     * Nếu lỗi, đơn vẫn là nháp và kho không đổi.
     */
    private void pay() {
        ensureEditable();
        applyOrderDiscount();
        if (order.getItems().isEmpty()) {
            throw new BusinessException("Đơn hàng chưa có sản phẩm nào.");
        }
        PaymentMethod method = view.rdoTransfer.isSelected() ? PaymentMethod.TRANSFER : PaymentMethod.CASH;
        if (method == PaymentMethod.CASH && !view.txtCashGiven.getText().trim().isEmpty()) {
            BigDecimal given = Formatter.parseMoney(view.txtCashGiven.getText());
            if (given.compareTo(order.getTotalAmount()) < 0) {
                throw new BusinessException("Tiền khách đưa chưa đủ.");
            }
        }
        if (!UiKit.confirm(view, "Xác nhận thanh toán " + Formatter.money(order.getTotalAmount()) + " đ bằng "
                + method.getLabel().toLowerCase() + "?")) {
            return;
        }
        Order paid;
        try {
            paid = orderService.confirmAndPay(order, method);
        } catch (BusinessException ex) {
            // Đơn đã được lưu nháp trước khi xác nhận; hiển thị lại trạng thái nháp
            renderOrder();
            searchVariants();
            throw ex;
        }
        String invoice = orderService.buildInvoice(paid.getId());
        new InvoiceDialog(SwingUtilities.getWindowAncestor(view), invoice).setVisible(true);
        startNewOrder();
        searchVariants();
    }

    /**
     * Hủy đơn nháp đang lập (BR11: đơn nháp hủy tự do, không ảnh hưởng kho).
     */
    private void cancel() {
        if (order.getId() == null) {
            if (order.getItems().isEmpty() || UiKit.confirm(view, "Bỏ đơn đang lập?")) {
                startNewOrder();
            }
            return;
        }
        if (!UiKit.confirm(view, "Hủy đơn nháp #" + order.getId() + "?")) {
            return;
        }
        orderService.cancelOrder(order.getId());
        UiKit.info(view, "Đã hủy đơn #" + order.getId() + ".");
        startNewOrder();
    }

    /**
     * Hiển thị đơn hiện tại lên giỏ hàng và các ô tổng tiền.
     */
    private void renderOrder() {
        List<Object[]> rows = new ArrayList<>();
        for (OrderItem it : order.getItems()) {
            rows.add(new Object[] { it.getVariantId(), it.getSku(), it.getVariantName(), it.getQuantity(),
                    Formatter.money(it.getUnitPrice()), Formatter.money(it.getDiscountAmount()),
                    Formatter.money(it.getLineTotal()) });
        }
        UiKit.fill(view.tblCart, rows);
        OrderService.recalculate(order);
        view.lblGross.setText(Formatter.money(OrderService.grossAmount(order)) + " đ");
        view.lblDiscount.setText(Formatter.money(OrderService.totalDiscount(order)) + " đ");
        view.lblTotal.setText(Formatter.money(order.getTotalAmount()) + " đ");
        view.lblOrderInfo.setText(order.getId() == null ? "Đơn mới (chưa lưu)" : "Đơn nháp #" + order.getId());
        view.lblCustomer.setText(customer == null ? "Khách lẻ" : customer.toString());
        updateChange();
    }

    /**
     * Tính tiền thối = tiền khách đưa - tổng thanh toán.
     */
    private void updateChange() {
        try {
            BigDecimal given = Formatter.parseMoney(view.txtCashGiven.getText());
            BigDecimal change = given.subtract(order == null ? BigDecimal.ZERO : order.getTotalAmount());
            view.lblChange.setText(given.signum() == 0 ? "0" : Formatter.money(change));
        } catch (BusinessException e) {
            view.lblChange.setText("?");
        }
    }

    /**
     * Lấy dòng hàng đang chọn trong giỏ.
     *
     * @return dòng hàng
     * @throws BusinessException nếu chưa chọn dòng
     */
    private OrderItem selectedLine() {
        int row = UiKit.selectedModelRow(view.tblCart);
        if (row < 0) {
            throw new BusinessException("Hãy chọn một dòng trong đơn hàng.");
        }
        Long variantId = (Long) view.tblCart.getModel().getValueAt(row, 0);
        for (OrderItem it : order.getItems()) {
            if (it.getVariantId().equals(variantId)) {
                return it;
            }
        }
        throw new BusinessException("Không tìm thấy dòng hàng.");
    }

    /**
     * Bảo đảm đơn hiện tại vẫn còn sửa được (trạng thái nháp).
     *
     * @throws BusinessException nếu đơn đã thanh toán hoặc đã hủy
     */
    private void ensureEditable() {
        if (order == null || order.getStatus() != OrderStatus.DRAFT) {
            throw new BusinessException("Đơn không còn ở trạng thái Nháp. Hãy tạo đơn mới.");
        }
    }
}

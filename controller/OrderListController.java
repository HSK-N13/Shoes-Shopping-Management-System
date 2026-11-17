package controller;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.SwingUtilities;

import model.Order;
import model.OrderItem;
import model.OrderStatus;
import services.OrderService;
import utils.BusinessException;
import utils.Formatter;
import utils.Session;
import view.InvoiceDialog;
import view.OrderListPanel;
import view.UiKit;

/**
 * Xử lý màn hình danh sách đơn hàng.
 */
public class OrderListController extends BaseController {
    private final OrderListPanel view;
    private final OrderService orderService = new OrderService();
    private final Consumer<Long> openDraftHandler;
    private List<Order> orders = new ArrayList<>();

    /**
     * Gắn controller vào màn hình và đăng ký sự kiện.
     *
     * @param view             màn hình danh sách đơn
     * @param openDraftHandler hàm mở đơn nháp ở màn hình bán hàng (nhận mã đơn)
     */
    public OrderListController(OrderListPanel view, Consumer<Long> openDraftHandler) {
        this.view = view;
        this.openDraftHandler = openDraftHandler;
        view.btnSearch.addActionListener(e -> handle(this::search));
        view.txtKeyword.addActionListener(e -> handle(this::search));
        view.tblOrders.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                handle(this::showDetail);
            }
        });
        view.btnInvoice.addActionListener(e -> handle(this::showInvoice));
        view.btnOpenDraft.addActionListener(e -> handle(this::openDraft));
        view.btnCancel.addActionListener(e -> handle(this::cancel));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Component parent() {
        return view;
    }

    /**
     * Tải lại danh sách đơn (gọi khi mở tab).
     */
    public void refresh() {
        handle(this::search);
    }

    /**
     * Tìm đơn theo bộ lọc và hiển thị.
     */
    private void search() {
        Object st = view.cboStatus.getSelectedItem();
        OrderStatus status = st instanceof OrderStatus ? (OrderStatus) st : null;
        java.time.LocalDate from = UiKit.dateOf(view.spnFrom);
        java.time.LocalDate to = UiKit.dateOf(view.spnTo);
        if (from.isAfter(to)) {
            throw new BusinessException("Từ ngày phải trước hoặc bằng đến ngày.");
        }
        orders = orderService.searchOrders(from.atStartOfDay(), to.plusDays(1).atStartOfDay(), status,
                view.txtKeyword.getText());
        List<Object[]> rows = new ArrayList<>();
        for (Order o : orders) {
            rows.add(new Object[] { o.getId(), Formatter.dateTime(o.getCreatedAt()),
                    o.getCustomerName() == null ? "Khách lẻ" : o.getCustomerName(), o.getUserName(),
                    Formatter.money(o.getDiscountAmount()), Formatter.money(o.getTotalAmount()),
                    o.getPaymentMethod() == null ? "" : o.getPaymentMethod().getLabel(), o.getStatus().getLabel() });
        }
        UiKit.fill(view.tblOrders, rows);
        UiKit.fill(view.tblItems, new ArrayList<>());
    }

    /**
     * Hiển thị dòng hàng của đơn đang chọn.
     */
    private void showDetail() {
        Long id = selectedId();
        if (id == null) {
            return;
        }
        Order o = orderService.getOrder(id);
        List<Object[]> rows = new ArrayList<>();
        for (OrderItem it : o.getItems()) {
            rows.add(new Object[] { it.getSku(), it.getVariantName(), it.getQuantity(),
                    Formatter.money(it.getUnitPrice()), Formatter.money(it.getDiscountAmount()),
                    Formatter.money(it.getLineTotal()) });
        }
        UiKit.fill(view.tblItems, rows);
    }

    /**
     * Mở hộp thoại xem và in hóa đơn của đơn đang chọn.
     */
    private void showInvoice() {
        Long id = requireSelected();
        new InvoiceDialog(SwingUtilities.getWindowAncestor(view), orderService.buildInvoice(id)).setVisible(true);
    }

    /**
     * Chuyển đơn nháp đang chọn sang màn hình bán hàng để tiếp tục.
     */
    private void openDraft() {
        Long id = requireSelected();
        Order o = find(id);
        if (o == null || o.getStatus() != OrderStatus.DRAFT) {
            throw new BusinessException("Chỉ mở được đơn đang ở trạng thái Nháp.");
        }
        openDraftHandler.accept(id);
    }

    /**
     * Hủy đơn đang chọn (BR11): đơn nháp hủy tự do; đơn đã thanh toán chỉ Admin
     * được hủy và hệ thống hoàn lại kho.
     */
    private void cancel() {
        Long id = requireSelected();
        Order o = find(id);
        if (o == null) {
            return;
        }
        if (o.getStatus() == OrderStatus.PAID && !Session.isAdmin()) {
            throw new BusinessException("Chỉ Admin được hủy đơn đã thanh toán (BR11).");
        }
        String msg = o.getStatus() == OrderStatus.PAID
                ? "Hủy đơn đã thanh toán #" + id + "? Hàng sẽ được hoàn lại kho."
                : "Hủy đơn #" + id + "?";
        if (!UiKit.confirm(view, msg)) {
            return;
        }
        orderService.cancelOrder(id);
        UiKit.info(view, "Đã hủy đơn #" + id + ".");
        search();
    }

    /**
     * Mã đơn đang chọn.
     *
     * @return mã đơn hoặc null
     */
    private Long selectedId() {
        int row = UiKit.selectedModelRow(view.tblOrders);
        return row < 0 ? null : (Long) view.tblOrders.getModel().getValueAt(row, 0);
    }

    /**
     * Mã đơn đang chọn, bắt buộc phải có.
     *
     * @return mã đơn
     * @throws BusinessException nếu chưa chọn
     */
    private Long requireSelected() {
        Long id = selectedId();
        if (id == null) {
            throw new BusinessException("Hãy chọn một đơn hàng.");
        }
        return id;
    }

    /**
     * Tìm đơn trong danh sách đang hiển thị.
     *
     * @param id mã đơn
     * @return đơn hoặc null
     */
    private Order find(Long id) {
        for (Order o : orders) {
            if (o.getId().equals(id)) {
                return o;
            }
        }
        return null;
    }
}

package view;

import java.awt.BorderLayout;
import java.time.LocalDate;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import model.OrderStatus;

/**
 * Màn hình tra cứu đơn hàng: lọc theo ngày/trạng thái/từ khóa, xem chi tiết,
 * in lại hóa đơn, mở tiếp đơn nháp và hủy đơn (BR11).
 */
public class OrderListPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    /** Từ ngày. */
    public final JSpinner spnFrom = UiKit.dateSpinner(LocalDate.now().minusDays(30));
    /** Đến ngày. */
    public final JSpinner spnTo = UiKit.dateSpinner(LocalDate.now());
    /** Lọc trạng thái (mục đầu là "Tất cả"). */
    public final JComboBox<Object> cboStatus = new JComboBox<>(
            new Object[] { "Tất cả", OrderStatus.DRAFT, OrderStatus.PAID, OrderStatus.CANCELLED });
    /** Từ khóa: mã đơn, tên hoặc SĐT khách. */
    public final JTextField txtKeyword = new JTextField(14);
    /** Nút tìm. */
    public final JButton btnSearch = UiKit.button("Tìm");

    /** Bảng đơn hàng: cột 0 là mã đơn. */
    public final JTable tblOrders = UiKit.table("Mã đơn", "Thời gian", "Khách hàng", "Nhân viên", "Giảm giá đơn",
            "Tổng tiền", "Thanh toán", "Trạng thái");
    /** Bảng dòng hàng của đơn đang chọn. */
    public final JTable tblItems = UiKit.table("SKU", "Sản phẩm", "SL", "Đơn giá", "Giảm dòng", "Thành tiền");

    /** Nút xem/in hóa đơn. */
    public final JButton btnInvoice = UiKit.button("Xem / In hóa đơn");
    /** Nút mở tiếp đơn nháp ở màn hình bán hàng. */
    public final JButton btnOpenDraft = UiKit.button("Mở đơn nháp");
    /** Nút hủy đơn. */
    public final JButton btnCancel = UiKit.button("Hủy đơn");

    /**
     * Dựng bố cục màn hình.
     */
    public OrderListPanel() {
        super(new BorderLayout(4, 4));
        UiKit.alignRight(tblOrders, 4, 5);
        UiKit.alignRight(tblItems, 2, 3, 4, 5);
        JPanel north = new JPanel(new BorderLayout());
        north.add(UiKit.title("Đơn hàng"), BorderLayout.NORTH);
        north.add(UiKit.row(new JLabel("Từ ngày:"), spnFrom, new JLabel("Đến ngày:"), spnTo,
                new JLabel("Trạng thái:"), cboStatus, new JLabel("Mã đơn / khách:"), txtKeyword, btnSearch),
                BorderLayout.CENTER);
        add(north, BorderLayout.NORTH);
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(UiKit.scroll(tblItems, "Chi tiết đơn"), BorderLayout.CENTER);
        bottom.add(UiKit.row(btnInvoice, btnOpenDraft, btnCancel), BorderLayout.SOUTH);
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, UiKit.scroll(tblOrders, "Danh sách đơn"), bottom);
        split.setResizeWeight(0.6);
        add(split, BorderLayout.CENTER);
    }
}

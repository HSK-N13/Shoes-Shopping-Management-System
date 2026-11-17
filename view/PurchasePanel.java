package view;

import java.awt.BorderLayout;
import java.awt.Font;
import java.time.LocalDate;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import model.ReceiptStatus;
import model.Supplier;

/**
 * Màn hình nhập hàng (mục 2.2): tab lập/sửa phiếu nhập và tab danh sách phiếu
 * để xác nhận hoặc hủy (BR04).
 */
public class PurchasePanel extends JPanel {
    private static final long serialVersionUID = 1L;

    /** Các tab con. */
    public final JTabbedPane tabs = new JTabbedPane();

    // ----- Tab lập phiếu -----
    /** Nhãn cho biết đang lập phiếu mới hay sửa phiếu nháp. */
    public final JLabel lblReceiptInfo = new JLabel("Phiếu mới");
    /** Nhà cung cấp. */
    public final JComboBox<Supplier> cboSupplier = new JComboBox<>();
    /** Từ khóa tìm biến thể. */
    public final JTextField txtKeyword = new JTextField(16);
    /** Nút tìm biến thể. */
    public final JButton btnSearch = UiKit.button("Tìm");
    /** Bảng biến thể: cột 0 là id (ẩn). */
    public final JTable tblVariants = UiKit.table("ID", "SKU", "Sản phẩm", "Size", "Màu", "Tồn", "Giá vốn hiện tại");
    /** Số lượng nhập. */
    public final JSpinner spnQuantity = new JSpinner(new SpinnerNumberModel(1, 1, 100000, 1));
    /** Giá nhập. */
    public final JTextField txtUnitCost = new JTextField(10);
    /** Nút thêm dòng nhập. */
    public final JButton btnAddLine = UiKit.button("Thêm dòng >>");
    /** Bảng dòng nhập: cột 0 là id biến thể (ẩn). */
    public final JTable tblLines = UiKit.table("ID", "SKU", "Sản phẩm", "SL", "Giá nhập", "Thành tiền");
    /** Nút xóa dòng nhập. */
    public final JButton btnRemoveLine = UiKit.button("Xóa dòng");
    /** Tổng tiền phiếu. */
    public final JLabel lblTotal = new JLabel("0 đ");
    /** Nút phiếu mới. */
    public final JButton btnNew = UiKit.button("Phiếu mới");
    /** Nút lưu nháp. */
    public final JButton btnSaveDraft = UiKit.button("Lưu nháp");
    /** Nút lưu và xác nhận ngay. */
    public final JButton btnSaveConfirm = UiKit.button("Lưu & Xác nhận nhập kho");

    // ----- Tab danh sách phiếu -----
    /** Từ ngày. */
    public final JSpinner spnFrom = UiKit.dateSpinner(LocalDate.now().minusDays(30));
    /** Đến ngày. */
    public final JSpinner spnTo = UiKit.dateSpinner(LocalDate.now());
    /** Lọc trạng thái (mục đầu là "Tất cả"). */
    public final JComboBox<Object> cboStatus = new JComboBox<>(
            new Object[] { "Tất cả", ReceiptStatus.DRAFT, ReceiptStatus.CONFIRMED, ReceiptStatus.CANCELLED });
    /** Nút lọc danh sách phiếu. */
    public final JButton btnFilter = UiKit.button("Lọc");
    /** Bảng phiếu nhập: cột 0 là mã phiếu. */
    public final JTable tblReceipts = UiKit.table("Mã phiếu", "Ngày lập", "Nhà cung cấp", "Người lập", "Tổng tiền", "Trạng thái");
    /** Bảng chi tiết phiếu đang chọn. */
    public final JTable tblReceiptItems = UiKit.table("SKU", "Sản phẩm", "SL", "Giá nhập", "Thành tiền");
    /** Nút mở phiếu nháp để sửa. */
    public final JButton btnEdit = UiKit.button("Sửa phiếu nháp");
    /** Nút xác nhận phiếu. */
    public final JButton btnConfirm = UiKit.button("Xác nhận nhập kho");
    /** Nút hủy phiếu nháp. */
    public final JButton btnCancelReceipt = UiKit.button("Hủy phiếu");

    /**
     * Dựng bố cục hai tab.
     */
    public PurchasePanel() {
        super(new BorderLayout());
        UiKit.hideColumn(tblVariants, 0);
        UiKit.hideColumn(tblLines, 0);
        cboSupplier.setPreferredSize(new java.awt.Dimension(280, 26));
        UiKit.alignRight(tblVariants, 4, 5);
        UiKit.alignRight(tblLines, 2, 3, 4);
        UiKit.alignRight(tblReceipts, 4);
        UiKit.alignRight(tblReceiptItems, 2, 3, 4);
        add(UiKit.title("Nhập hàng"), BorderLayout.NORTH);

        JPanel create = new JPanel(new BorderLayout(4, 4));
        lblReceiptInfo.setFont(lblReceiptInfo.getFont().deriveFont(Font.BOLD, 14f));
        create.add(UiKit.row(lblReceiptInfo, new JLabel("   Nhà cung cấp:"), cboSupplier), BorderLayout.NORTH);
        JPanel left = new JPanel(new BorderLayout());
        left.add(UiKit.row(new JLabel("Tìm biến thể:"), txtKeyword, btnSearch), BorderLayout.NORTH);
        left.add(UiKit.scroll(tblVariants, "Biến thể"), BorderLayout.CENTER);
        left.add(UiKit.row(new JLabel("SL:"), spnQuantity, new JLabel("Giá nhập:"), txtUnitCost, btnAddLine),
                BorderLayout.SOUTH);
        JPanel right = new JPanel(new BorderLayout());
        right.add(UiKit.scroll(tblLines, "Dòng nhập"), BorderLayout.CENTER);
        lblTotal.setFont(lblTotal.getFont().deriveFont(Font.BOLD, 16f));
        JPanel rs = new JPanel(new BorderLayout());
        rs.add(UiKit.row(btnRemoveLine, new JLabel("   Tổng tiền:"), lblTotal), BorderLayout.NORTH);
        rs.add(UiKit.row(btnNew, btnSaveDraft, btnSaveConfirm), BorderLayout.SOUTH);
        right.add(rs, BorderLayout.SOUTH);
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        split.setResizeWeight(0.5);
        create.add(split, BorderLayout.CENTER);
        tabs.addTab("Lập phiếu nhập", create);

        JPanel list = new JPanel(new BorderLayout(4, 4));
        list.add(UiKit.row(new JLabel("Từ ngày:"), spnFrom, new JLabel("Đến ngày:"), spnTo,
                new JLabel("Trạng thái:"), cboStatus, btnFilter), BorderLayout.NORTH);
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(UiKit.scroll(tblReceiptItems, "Chi tiết phiếu"), BorderLayout.CENTER);
        bottom.add(UiKit.row(btnEdit, btnConfirm, btnCancelReceipt), BorderLayout.SOUTH);
        JSplitPane split2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, UiKit.scroll(tblReceipts, "Phiếu nhập"), bottom);
        split2.setResizeWeight(0.6);
        list.add(split2, BorderLayout.CENTER);
        tabs.addTab("Danh sách phiếu nhập", list);
        add(tabs, BorderLayout.CENTER);
    }
}

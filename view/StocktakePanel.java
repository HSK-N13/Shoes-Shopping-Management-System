package view;

import java.awt.BorderLayout;
import java.time.LocalDate;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import model.MovementType;

/**
 * Màn hình kiểm kê (mục 2.2) và tra cứu lịch sử biến động tồn kho (BR03).
 */
public class StocktakePanel extends JPanel {
    private static final long serialVersionUID = 1L;

    /** Chỉ số cột "Thực tế" trong model của bảng kiểm kê. */
    public static final int COL_ACTUAL = 6;

    /** Ô lọc nhanh theo SKU/tên. */
    public final JTextField txtFilter = new JTextField(16);
    /** Chỉ hiện dòng đã nhập số thực tế khác tồn hệ thống. */
    public final JCheckBox chkOnlyDiff = new JCheckBox("Chỉ hiện dòng chênh lệch");
    /** Nút tải lại tồn kho. */
    public final JButton btnReload = UiKit.button("Tải lại");
    /** Nút áp dụng điều chỉnh. */
    public final JButton btnApply = UiKit.button("Tạo điều chỉnh chênh lệch");
    /** Nút đối soát tồn với lịch sử. */
    public final JButton btnReconcile = UiKit.button("Đối soát tồn kho");
    /** Bảng kiểm kê: cột 0 id (ẩn), cột "Thực tế" sửa được. */
    public final JTable tblStock;

    /** Loại biến động cần lọc. */
    public final JComboBox<Object> cboType = new JComboBox<>(new Object[] { "Tất cả", MovementType.PURCHASE,
            MovementType.SALE, MovementType.ADJUSTMENT, MovementType.CANCEL });
    /** Từ ngày. */
    public final JSpinner spnFrom = UiKit.dateSpinner(LocalDate.now().minusDays(30));
    /** Đến ngày. */
    public final JSpinner spnTo = UiKit.dateSpinner(LocalDate.now());
    /** Lọc theo SKU (để trống = tất cả). */
    public final JTextField txtSku = new JTextField(12);
    /** Nút xem lịch sử. */
    public final JButton btnHistory = UiKit.button("Xem lịch sử");
    /** Bảng lịch sử biến động. */
    public final JTable tblHistory = UiKit.table("Thời gian", "SKU", "Biến thể", "Loại", "SL thay đổi", "Chứng từ");

    /**
     * Dựng bố cục hai tab: kiểm kê và lịch sử biến động.
     */
    public StocktakePanel() {
        super(new BorderLayout());
        DefaultTableModel model = new DefaultTableModel(
                new String[] { "ID", "SKU", "Sản phẩm", "Size", "Màu", "Tồn hệ thống", "Thực tế", "Chênh lệch" }, 0) {
            private static final long serialVersionUID = 1L;

            /**
             * Chỉ cho sửa cột số lượng thực tế.
             *
             * @param row    dòng
             * @param column cột
             * @return true nếu là cột Thực tế
             */
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == COL_ACTUAL;
            }
        };
        tblStock = new JTable(model);
        tblStock.setRowHeight(24);
        tblStock.setAutoCreateRowSorter(true);
        tblStock.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblStock.getTableHeader().setReorderingAllowed(false);
        tblStock.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        UiKit.hideColumn(tblStock, 0);
        UiKit.alignRight(tblStock, 4, 5, 6);
        UiKit.alignRight(tblHistory, 4);

        add(UiKit.title("Kiểm kê & lịch sử tồn kho"), BorderLayout.NORTH);
        JTabbedPane tabs = new JTabbedPane();
        JPanel take = new JPanel(new BorderLayout(4, 4));
        take.add(UiKit.row(new JLabel("Lọc:"), txtFilter, chkOnlyDiff, btnReload), BorderLayout.NORTH);
        take.add(UiKit.scroll(tblStock, "Nhập số lượng thực tế đếm được vào cột \"Thực tế\" (để trống = không kiểm)"),
                BorderLayout.CENTER);
        take.add(UiKit.row(btnApply, btnReconcile), BorderLayout.SOUTH);
        tabs.addTab("Kiểm kê", take);

        JPanel hist = new JPanel(new BorderLayout(4, 4));
        hist.add(UiKit.row(new JLabel("Loại:"), cboType, new JLabel("SKU:"), txtSku, new JLabel("Từ ngày:"), spnFrom,
                new JLabel("Đến ngày:"), spnTo, btnHistory), BorderLayout.NORTH);
        hist.add(UiKit.scroll(tblHistory, null), BorderLayout.CENTER);
        tabs.addTab("Lịch sử biến động", hist);
        add(tabs, BorderLayout.CENTER);
    }
}

package view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableCellRenderer;

import services.ReportService;

/**
 * Màn hình báo cáo (mục 2.4): doanh thu theo ngày/tháng/năm, lợi nhuận gộp (chỉ Admin),
 * sản phẩm bán chạy, tồn kho, hàng dưới ngưỡng và tổng giá trị nhập.
 */
public class ReportPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    /** Từ ngày. */
    public final JSpinner spnFrom = UiKit.dateSpinner(LocalDate.now().withDayOfMonth(1));
    /** Đến ngày. */
    public final JSpinner spnTo = UiKit.dateSpinner(LocalDate.now());
    /** Cách nhóm doanh thu. */
    public final JComboBox<ReportService.Period> cboPeriod = new JComboBox<>(ReportService.Period.values());
    /** Số sản phẩm bán chạy cần xem. */
    public final JSpinner spnTop = new JSpinner(new SpinnerNumberModel(10, 1, 100, 1));
    /** Nút xem báo cáo. */
    public final JButton btnView = UiKit.button("Xem báo cáo");

    /** Bảng doanh thu theo kỳ. */
    public final JTable tblRevenue = UiKit.table("Kỳ", "Số đơn", "Doanh thu thuần", "Giá vốn", "Lợi nhuận gộp");
    /** Tổng số đơn. */
    public final JLabel lblOrders = new JLabel("0");
    /** Tổng doanh thu. */
    public final JLabel lblRevenue = new JLabel("0");
    /** Tổng lợi nhuận gộp (chỉ Admin). */
    public final JLabel lblProfit = new JLabel("0");
    private final JLabel lblProfitCaption = new JLabel("Lợi nhuận gộp:");

    /** Bảng bán chạy. */
    public final JTable tblBest = UiKit.table("Hạng", "SKU", "Biến thể", "SL đã bán", "Doanh thu");
    /** Bảng tồn kho. */
    public final JTable tblInventory = UiKit.table("SKU", "Sản phẩm", "Thương hiệu", "Size", "Màu", "Tồn", "Tối thiểu", "Trạng thái");
    /** Bảng hàng dưới ngưỡng. */
    public final JTable tblLowStock = UiKit.table("SKU", "Sản phẩm", "Size", "Màu", "Tồn", "Tối thiểu", "Cần nhập thêm");

    /** Tổng giá trị nhập trong kỳ. */
    public final JLabel lblPurchaseValue = new JLabel("0");
    /** Số phiếu nhập đã xác nhận trong kỳ. */
    public final JLabel lblReceiptCount = new JLabel("0");
    /** Giá trị tồn kho theo giá vốn (chỉ Admin). */
    public final JLabel lblInventoryValue = new JLabel("0");
    private final JLabel lblInventoryValueCaption = new JLabel("Giá trị tồn kho (giá vốn):");

    /**
     * Dựng bố cục: thanh điều kiện chung và các tab báo cáo.
     */
    public ReportPanel() {
        super(new BorderLayout(4, 4));
        UiKit.alignRight(tblRevenue, 1, 2, 3, 4);
        UiKit.alignRight(tblBest, 3, 4);
        UiKit.alignRight(tblLowStock, 4, 5, 6);
        tblInventory.setDefaultRenderer(Object.class, new LowStockRenderer());

        JPanel north = new JPanel(new BorderLayout());
        north.add(UiKit.title("Báo cáo"), BorderLayout.NORTH);
        north.add(UiKit.row(new JLabel("Từ ngày:"), spnFrom, new JLabel("Đến ngày:"), spnTo, new JLabel("Nhóm:"),
                cboPeriod, new JLabel("Top bán chạy:"), spnTop, btnView), BorderLayout.CENTER);
        add(north, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        JPanel revenue = new JPanel(new BorderLayout());
        revenue.add(UiKit.scroll(tblRevenue, null), BorderLayout.CENTER);
        JPanel sum = new JPanel(new GridLayout(1, 6, 6, 0));
        sum.setBorder(BorderFactory.createTitledBorder("Tổng kỳ"));
        sum.add(new JLabel("Số đơn:"));
        sum.add(bold(lblOrders));
        sum.add(new JLabel("Doanh thu thuần:"));
        sum.add(bold(lblRevenue));
        sum.add(lblProfitCaption);
        sum.add(bold(lblProfit));
        revenue.add(sum, BorderLayout.SOUTH);
        tabs.addTab("Doanh thu", revenue);
        tabs.addTab("Bán chạy", UiKit.scroll(tblBest, null));
        tabs.addTab("Tồn kho", UiKit.scroll(tblInventory, "Dòng tô đỏ: dưới ngưỡng tồn tối thiểu"));
        tabs.addTab("Dưới ngưỡng tồn", UiKit.scroll(tblLowStock, null));
        JPanel purchase = new JPanel(new GridLayout(3, 2, 6, 6));
        purchase.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        purchase.add(new JLabel("Tổng giá trị nhập (phiếu đã xác nhận):"));
        purchase.add(bold(lblPurchaseValue));
        purchase.add(new JLabel("Số phiếu nhập đã xác nhận:"));
        purchase.add(bold(lblReceiptCount));
        purchase.add(lblInventoryValueCaption);
        purchase.add(bold(lblInventoryValue));
        JPanel pw = new JPanel(new BorderLayout());
        pw.add(purchase, BorderLayout.NORTH);
        tabs.addTab("Nhập hàng", pw);
        add(tabs, BorderLayout.CENTER);
    }

    /**
     * Ẩn giá vốn và lợi nhuận với nhân viên (BR12).
     *
     * @param admin true nếu người dùng là Admin
     */
    public void setAdminMode(boolean admin) {
        lblProfit.setVisible(admin);
        lblProfitCaption.setVisible(admin);
        lblInventoryValue.setVisible(admin);
        lblInventoryValueCaption.setVisible(admin);
        if (!admin && tblRevenue.getColumnCount() == 5) {
            tblRevenue.getColumnModel().removeColumn(tblRevenue.getColumnModel().getColumn(4));
            tblRevenue.getColumnModel().removeColumn(tblRevenue.getColumnModel().getColumn(3));
        }
    }

    /**
     * In đậm một nhãn.
     *
     * @param l nhãn
     * @return chính nhãn đó
     */
    private static JLabel bold(JLabel l) {
        l.setFont(l.getFont().deriveFont(Font.BOLD, 14f));
        return l;
    }

    /**
     * Bộ vẽ ô tô màu nền cho dòng tồn kho dưới ngưỡng tối thiểu.
     */
    private static class LowStockRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        /**
         * Vẽ ô; số căn phải, dòng có tồn nhỏ hơn tối thiểu được tô nền đỏ nhạt.
         *
         * @param table      bảng
         * @param value      giá trị ô
         * @param isSelected ô đang được chọn
         * @param hasFocus   ô đang có focus
         * @param row        dòng (theo view)
         * @param column     cột (theo view)
         * @return thành phần dùng để vẽ
         */
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(value instanceof Number ? RIGHT : LEFT);
            int m = table.convertRowIndexToModel(row);
            Object stock = table.getModel().getValueAt(m, 5);
            Object min = table.getModel().getValueAt(m, 6);
            boolean low = stock instanceof Integer && min instanceof Integer && (Integer) stock < (Integer) min;
            if (!isSelected) {
                c.setBackground(low ? UiKit.WARNING : table.getBackground());
            }
            return c;
        }
    }
}

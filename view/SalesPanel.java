package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

/**
 * Màn hình bán hàng tại quầy (mục 2.3 và luồng bán ở mục 4):
 * bên trái tìm biến thể, bên phải giỏ hàng, giảm giá, khách hàng và thanh toán.
 */
public class SalesPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    /** Nhãn cho biết đơn đang lập (mới hay nháp số mấy). */
    public final JLabel lblOrderInfo = new JLabel("Đơn mới (chưa lưu)");

    /** Từ khóa tìm biến thể (tên, SKU, thương hiệu). */
    public final JTextField txtKeyword = new JTextField(14);
    /** Lọc size. */
    public final JTextField txtSize = new JTextField(4);
    /** Lọc màu. */
    public final JTextField txtColor = new JTextField(6);
    /** Nút tìm biến thể. */
    public final JButton btnSearch = UiKit.button("Tìm");
    /** Bảng kết quả tìm: cột 0 là id biến thể (ẩn). */
    public final JTable tblVariants = UiKit.table("ID", "SKU", "Sản phẩm", "Thương hiệu", "Size", "Màu", "Giá bán", "Tồn");
    /** Số lượng muốn thêm. */
    public final JSpinner spnQuantity = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
    /** Nút thêm biến thể đang chọn vào đơn. */
    public final JButton btnAdd = UiKit.button("Thêm vào đơn >>");

    /** Bảng giỏ hàng: cột 0 là id biến thể (ẩn). */
    public final JTable tblCart = UiKit.table("ID", "SKU", "Sản phẩm", "SL", "Đơn giá", "Giảm dòng", "Thành tiền");
    /** Nút sửa số lượng dòng đang chọn. */
    public final JButton btnEditQty = UiKit.button("Sửa SL");
    /** Nút nhập giảm giá cho dòng đang chọn. */
    public final JButton btnLineDiscount = UiKit.button("Giảm giá dòng");
    /** Nút xóa dòng đang chọn. */
    public final JButton btnRemove = UiKit.button("Xóa dòng");

    /** Ô nhập SĐT/tên khách để tìm. */
    public final JTextField txtCustomer = new JTextField(12);
    /** Nút tìm khách hàng. */
    public final JButton btnFindCustomer = UiKit.button("Tìm KH");
    /** Nút thêm khách hàng mới. */
    public final JButton btnNewCustomer = UiKit.button("KH mới");
    /** Nút bỏ gắn khách hàng. */
    public final JButton btnClearCustomer = UiKit.button("Bỏ KH");
    /** Nhãn khách hàng đang gắn với đơn. */
    public final JLabel lblCustomer = new JLabel("Khách lẻ");

    /** Ô nhập giảm giá cấp đơn. */
    public final JTextField txtOrderDiscount = new JTextField("0", 10);
    /** Nút áp dụng giảm giá đơn. */
    public final JButton btnApplyDiscount = UiKit.button("Áp dụng");
    /** Tổng tiền hàng trước giảm giá. */
    public final JLabel lblGross = new JLabel("0");
    /** Tổng giảm giá. */
    public final JLabel lblDiscount = new JLabel("0");
    /** Tổng thanh toán. */
    public final JLabel lblTotal = new JLabel("0");

    /** Thanh toán tiền mặt. */
    public final JRadioButton rdoCash = new JRadioButton("Tiền mặt", true);
    /** Thanh toán chuyển khoản. */
    public final JRadioButton rdoTransfer = new JRadioButton("Chuyển khoản");
    /** Tiền khách đưa (để tính tiền thối khi trả tiền mặt). */
    public final JTextField txtCashGiven = new JTextField(10);
    /** Tiền thối lại. */
    public final JLabel lblChange = new JLabel("0");

    /** Nút tạo đơn mới. */
    public final JButton btnNewOrder = UiKit.button("Đơn mới");
    /** Nút lưu nháp. */
    public final JButton btnSaveDraft = UiKit.button("Lưu nháp");
    /** Nút xác nhận và thanh toán. */
    public final JButton btnPay = UiKit.button("Thanh toán");
    /** Nút hủy đơn nháp. */
    public final JButton btnCancel = UiKit.button("Hủy đơn");

    /**
     * Dựng bố cục màn hình bán hàng.
     */
    public SalesPanel() {
        super(new BorderLayout(4, 4));
        UiKit.hideColumn(tblVariants, 0);
        UiKit.hideColumn(tblCart, 0);
        UiKit.alignRight(tblVariants, 5, 6);
        UiKit.alignRight(tblCart, 2, 3, 4, 5);

        JPanel north = new JPanel(new BorderLayout());
        north.add(UiKit.title("Bán hàng tại quầy"), BorderLayout.WEST);
        lblOrderInfo.setFont(lblOrderInfo.getFont().deriveFont(Font.BOLD, 14f));
        north.add(lblOrderInfo, BorderLayout.EAST);
        add(north, BorderLayout.NORTH);

        JPanel left = new JPanel(new BorderLayout());
        left.add(UiKit.row(new JLabel("Tìm:"), txtKeyword, new JLabel("Size:"), txtSize,
                new JLabel("Màu:"), txtColor, btnSearch), BorderLayout.NORTH);
        left.add(UiKit.scroll(tblVariants, "Sản phẩm (biến thể đang bán)"), BorderLayout.CENTER);
        left.add(UiKit.row(new JLabel("Số lượng:"), spnQuantity, btnAdd), BorderLayout.SOUTH);

        JPanel right = new JPanel(new BorderLayout());
        right.add(UiKit.scroll(tblCart, "Đơn hàng"), BorderLayout.CENTER);
        JPanel south = new JPanel(new BorderLayout());
        south.add(UiKit.row(btnEditQty, btnLineDiscount, btnRemove), BorderLayout.NORTH);

        JPanel info = new JPanel(new GridLayout(0, 1, 2, 2));
        info.add(UiKit.row(new JLabel("Khách hàng:"), txtCustomer, btnFindCustomer, btnNewCustomer, btnClearCustomer));
        info.add(UiKit.row(new JLabel("Đang gắn:"), lblCustomer));
        info.add(UiKit.row(new JLabel("Giảm giá đơn:"), txtOrderDiscount, btnApplyDiscount));
        ButtonGroup g = new ButtonGroup();
        g.add(rdoCash);
        g.add(rdoTransfer);
        info.add(UiKit.row(new JLabel("Thanh toán:"), rdoCash, rdoTransfer, new JLabel("Khách đưa:"), txtCashGiven,
                new JLabel("Thối lại:"), lblChange));
        south.add(info, BorderLayout.CENTER);

        JPanel totals = new JPanel(new GridLayout(3, 2, 6, 2));
        totals.setBorder(BorderFactory.createTitledBorder("Tổng cộng"));
        totals.add(new JLabel("Tiền hàng:"));
        totals.add(lblGross);
        totals.add(new JLabel("Tổng giảm giá:"));
        totals.add(lblDiscount);
        JLabel lt = new JLabel("TỔNG THANH TOÁN:");
        lt.setFont(lt.getFont().deriveFont(Font.BOLD));
        totals.add(lt);
        lblTotal.setFont(lblTotal.getFont().deriveFont(Font.BOLD, 20f));
        lblTotal.setForeground(new Color(198, 40, 40));
        totals.add(lblTotal);
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(totals, BorderLayout.CENTER);
        bottom.add(UiKit.row(btnNewOrder, btnSaveDraft, btnPay, btnCancel), BorderLayout.SOUTH);
        south.add(bottom, BorderLayout.SOUTH);
        right.add(south, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        split.setResizeWeight(0.48);
        add(split, BorderLayout.CENTER);
    }
}

package view;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * Màn hình quản lý sản phẩm (mẫu giày) và biến thể size/màu (mục 2.1).
 * Nhân viên chỉ tra cứu; Admin được thêm/sửa/ngừng kinh doanh (BR12).
 */
public class ProductPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    /** Ô từ khóa tìm kiếm. */
    public final JTextField txtKeyword = new JTextField(18);
    /** Lọc theo danh mục (mục đầu là "Tất cả"). */
    public final JComboBox<Object> cboFilterCategory = new JComboBox<>();
    /** Lọc theo thương hiệu (mục đầu là "Tất cả"). */
    public final JComboBox<Object> cboFilterBrand = new JComboBox<>();
    /** Lọc theo trạng thái kinh doanh. */
    public final JComboBox<String> cboFilterStatus = new JComboBox<>(new String[] { "Tất cả", "Đang bán", "Ngừng kinh doanh" });
    /** Nút tìm kiếm. */
    public final JButton btnSearch = UiKit.button("Tìm");

    /** Bảng sản phẩm: cột 0 là id (ẩn). */
    public final JTable tblProducts = UiKit.table("ID", "Tên sản phẩm", "Danh mục", "Thương hiệu", "Trạng thái");
    /** Tên sản phẩm. */
    public final JTextField txtName = new JTextField(20);
    /** Danh mục của sản phẩm. */
    public final JComboBox<Object> cboCategory = new JComboBox<>();
    /** Thương hiệu của sản phẩm. */
    public final JComboBox<Object> cboBrand = new JComboBox<>();
    /** Mô tả. */
    public final JTextArea txtDescription = new JTextArea(3, 20);
    /** Đang kinh doanh. */
    public final JCheckBox chkActive = new JCheckBox("Đang kinh doanh", true);
    /** Nút làm mới form sản phẩm. */
    public final JButton btnNewProduct = UiKit.button("Thêm mới");
    /** Nút lưu sản phẩm. */
    public final JButton btnSaveProduct = UiKit.button("Lưu sản phẩm");
    /** Nút xóa/ngừng kinh doanh sản phẩm. */
    public final JButton btnDeleteProduct = UiKit.button("Xóa / Ngừng KD");

    /** Bảng biến thể: cột 0 là id (ẩn). */
    public final JTable tblVariants = UiKit.table("ID", "SKU", "Size", "Màu", "Giá bán", "Giá vốn", "Tồn", "Tồn tối thiểu", "Trạng thái");
    /** SKU. */
    public final JTextField txtSku = new JTextField(14);
    /** Size. */
    public final JTextField txtSize = new JTextField(6);
    /** Màu. */
    public final JTextField txtColor = new JTextField(10);
    /** Giá bán. */
    public final JTextField txtPrice = new JTextField(10);
    /** Ngưỡng tồn tối thiểu. */
    public final JTextField txtMinStock = new JTextField("5", 6);
    /** Biến thể đang kinh doanh. */
    public final JCheckBox chkVariantActive = new JCheckBox("Đang kinh doanh", true);
    /** Nút làm mới form biến thể. */
    public final JButton btnNewVariant = UiKit.button("Thêm mới");
    /** Nút lưu biến thể. */
    public final JButton btnSaveVariant = UiKit.button("Lưu biến thể");
    /** Nút xóa/ngừng kinh doanh biến thể. */
    public final JButton btnDeleteVariant = UiKit.button("Xóa / Ngừng KD");

    private final JPanel productForm;
    private final JPanel variantForm;

    /**
     * Dựng bố cục màn hình: thanh tìm kiếm, bảng + form sản phẩm, bảng + form biến thể.
     */
    public ProductPanel() {
        super(new BorderLayout(4, 4));
        UiKit.hideColumn(tblProducts, 0);
        UiKit.hideColumn(tblVariants, 0);
        cboFilterCategory.setPreferredSize(new Dimension(170, 26));
        cboFilterBrand.setPreferredSize(new Dimension(150, 26));
        UiKit.alignRight(tblVariants, 3, 4, 5, 6);

        JPanel north = new JPanel(new BorderLayout());
        north.add(UiKit.title("Quản lý sản phẩm"), BorderLayout.NORTH);
        north.add(UiKit.row(new javax.swing.JLabel("Từ khóa:"), txtKeyword, new javax.swing.JLabel("Danh mục:"),
                cboFilterCategory, new javax.swing.JLabel("Thương hiệu:"), cboFilterBrand,
                new javax.swing.JLabel("Trạng thái:"), cboFilterStatus, btnSearch), BorderLayout.CENTER);
        add(north, BorderLayout.NORTH);

        txtDescription.setLineWrap(true);
        productForm = new JPanel(new BorderLayout());
        productForm.add(UiKit.form("Thông tin sản phẩm",
                new String[] { "Tên:", "Danh mục:", "Thương hiệu:", "Mô tả:", "" },
                new JComponent[] { txtName, cboCategory, cboBrand, new JScrollPane(txtDescription), chkActive }),
                BorderLayout.CENTER);
        productForm.add(UiKit.row(btnNewProduct, btnSaveProduct, btnDeleteProduct), BorderLayout.SOUTH);
        productForm.setPreferredSize(new Dimension(380, 260));
        JPanel top = new JPanel(new BorderLayout());
        top.add(UiKit.scroll(tblProducts, "Danh sách mẫu giày"), BorderLayout.CENTER);
        top.add(productForm, BorderLayout.EAST);

        variantForm = new JPanel(new BorderLayout());
        variantForm.add(UiKit.form("Biến thể size/màu",
                new String[] { "SKU:", "Size:", "Màu:", "Giá bán:", "Tồn tối thiểu:", "" },
                new JComponent[] { txtSku, txtSize, txtColor, txtPrice, txtMinStock, chkVariantActive }),
                BorderLayout.CENTER);
        variantForm.add(UiKit.row(btnNewVariant, btnSaveVariant, btnDeleteVariant), BorderLayout.SOUTH);
        variantForm.setPreferredSize(new Dimension(380, 260));
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(UiKit.scroll(tblVariants, "Biến thể của mẫu đang chọn"), BorderLayout.CENTER);
        bottom.add(variantForm, BorderLayout.EAST);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, top, bottom);
        split.setResizeWeight(0.5);
        add(split, BorderLayout.CENTER);
    }

    /**
     * Bật/tắt chức năng chỉnh sửa theo vai trò: nhân viên chỉ xem (BR12).
     * Với nhân viên, cột giá vốn cũng bị ẩn.
     *
     * @param admin true nếu người dùng là Admin
     */
    public void setAdminMode(boolean admin) {
        productForm.setVisible(admin);
        variantForm.setVisible(admin);
        if (!admin && tblVariants.getColumnCount() == 8) {
            tblVariants.getColumnModel().removeColumn(tblVariants.getColumnModel().getColumn(4));
        }
    }
}

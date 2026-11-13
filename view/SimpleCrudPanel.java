package view;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;

/**
 * Khung giao diện CRUD đơn giản dùng chung cho danh mục, thương hiệu,
 * nhà cung cấp và khách hàng: ô tìm kiếm, bảng danh sách và form hai trường.
 */
public class SimpleCrudPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    /** Ô từ khóa tìm kiếm. */
    public final JTextField txtKeyword = new JTextField(20);
    /** Nút tìm kiếm. */
    public final JButton btnSearch = UiKit.button("Tìm");
    /** Bảng danh sách: cột 0 là id (ẩn). */
    public final JTable table;
    /** Trường thứ nhất (tên). */
    public final JTextField txtField1 = new JTextField(22);
    /** Trường thứ hai (số điện thoại), có thể không dùng. */
    public final JTextField txtField2 = new JTextField(22);
    /** Ô trạng thái sử dụng, có thể không dùng. */
    public final JCheckBox chkActive = new JCheckBox("Đang sử dụng", true);
    /** Nút làm mới form. */
    public final JButton btnNew = UiKit.button("Thêm mới");
    /** Nút lưu. */
    public final JButton btnSave = UiKit.button("Lưu");
    /** Nút xóa. */
    public final JButton btnDelete = UiKit.button("Xóa");

    private final JPanel formPanel;

    /**
     * Dựng khung CRUD.
     *
     * @param title       tiêu đề khung
     * @param label1      nhãn trường thứ nhất
     * @param label2      nhãn trường thứ hai (null = không có)
     * @param withActive  có ô trạng thái sử dụng hay không
     * @param columns     tên các cột của bảng (cột đầu là ID)
     */
    public SimpleCrudPanel(String title, String label1, String label2, boolean withActive, String... columns) {
        super(new BorderLayout(4, 4));
        table = UiKit.table(columns);
        UiKit.hideColumn(table, 0);
        JPanel north = new JPanel(new BorderLayout());
        north.add(UiKit.title(title), BorderLayout.NORTH);
        north.add(UiKit.row(new JLabel("Từ khóa:"), txtKeyword, btnSearch), BorderLayout.CENTER);
        add(north, BorderLayout.NORTH);
        add(UiKit.scroll(table, null), BorderLayout.CENTER);

        java.util.List<String> labels = new java.util.ArrayList<>();
        java.util.List<JComponent> fields = new java.util.ArrayList<>();
        labels.add(label1);
        fields.add(txtField1);
        if (label2 != null) {
            labels.add(label2);
            fields.add(txtField2);
        }
        if (withActive) {
            labels.add("");
            fields.add(chkActive);
        }
        formPanel = new JPanel(new BorderLayout());
        formPanel.add(UiKit.form("Thông tin", labels.toArray(new String[0]), fields.toArray(new JComponent[0])),
                BorderLayout.NORTH);
        formPanel.add(UiKit.row(btnNew, btnSave, btnDelete), BorderLayout.CENTER);
        formPanel.setPreferredSize(new Dimension(380, 200));
        JPanel east = new JPanel(new BorderLayout());
        east.add(formPanel, BorderLayout.NORTH);
        add(east, BorderLayout.EAST);
    }

    /**
     * Ẩn/hiện form chỉnh sửa (khi người dùng không có quyền sửa).
     *
     * @param editable true để hiện form
     */
    public void setEditable(boolean editable) {
        formPanel.setVisible(editable);
    }
}

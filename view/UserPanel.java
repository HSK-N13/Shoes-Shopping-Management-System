package view;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTable;
import javax.swing.JTextField;

import model.Role;

/**
 * Màn hình quản lý tài khoản (chỉ Admin, BR12).
 */
public class UserPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    /** Bảng tài khoản: cột 0 là id (ẩn). */
    public final JTable tblUsers = UiKit.table("ID", "Tên đăng nhập", "Họ tên", "Vai trò", "Trạng thái");
    /** Tên đăng nhập (chỉ nhập khi tạo mới). */
    public final JTextField txtUsername = new JTextField(18);
    /** Họ tên. */
    public final JTextField txtFullName = new JTextField(18);
    /** Vai trò. */
    public final JComboBox<Role> cboRole = new JComboBox<>(Role.values());
    /** Trạng thái hoạt động. */
    public final JCheckBox chkActive = new JCheckBox("Đang hoạt động", true);
    /** Mật khẩu ban đầu (chỉ khi tạo mới). */
    public final JPasswordField txtPassword = new JPasswordField(18);
    /** Nút làm mới form. */
    public final JButton btnNew = UiKit.button("Thêm mới");
    /** Nút lưu. */
    public final JButton btnSave = UiKit.button("Lưu");
    /** Nút đặt lại mật khẩu. */
    public final JButton btnResetPassword = UiKit.button("Đặt lại mật khẩu");

    /**
     * Dựng bố cục: bảng tài khoản bên trái, form bên phải.
     */
    public UserPanel() {
        super(new BorderLayout(4, 4));
        UiKit.hideColumn(tblUsers, 0);
        add(UiKit.title("Quản lý tài khoản"), BorderLayout.NORTH);
        add(UiKit.scroll(tblUsers, null), BorderLayout.CENTER);
        JPanel form = new JPanel(new BorderLayout());
        form.add(UiKit.form("Thông tin tài khoản",
                new String[] { "Tên đăng nhập:", "Họ tên:", "Vai trò:", "", "Mật khẩu (tạo mới):" },
                new JComponent[] { txtUsername, txtFullName, cboRole, chkActive, txtPassword }), BorderLayout.NORTH);
        form.add(UiKit.row(btnNew, btnSave, btnResetPassword), BorderLayout.CENTER);
        form.setPreferredSize(new Dimension(400, 260));
        JPanel east = new JPanel(new BorderLayout());
        east.add(form, BorderLayout.NORTH);
        add(east, BorderLayout.EAST);
    }
}

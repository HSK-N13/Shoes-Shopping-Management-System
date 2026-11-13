package view;

import java.awt.BorderLayout;
import java.awt.Window;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

/**
 * Hộp thoại đổi mật khẩu của người dùng đang đăng nhập.
 */
public class ChangePasswordDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    private final JPasswordField txtOld = new JPasswordField(18);
    private final JPasswordField txtNew = new JPasswordField(18);
    private final JPasswordField txtConfirm = new JPasswordField(18);
    private final JButton btnSave = UiKit.button("Lưu");
    private final JButton btnCancel = UiKit.button("Đóng");

    /**
     * Dựng hộp thoại.
     *
     * @param owner cửa sổ cha
     */
    public ChangePasswordDialog(Window owner) {
        super(owner, "Đổi mật khẩu", ModalityType.APPLICATION_MODAL);
        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        root.add(UiKit.form(null, new String[] { "Mật khẩu cũ:", "Mật khẩu mới:", "Nhập lại:" },
                new JComponent[] { txtOld, txtNew, txtConfirm }), BorderLayout.CENTER);
        root.add(UiKit.row(btnSave, btnCancel), BorderLayout.SOUTH);
        setContentPane(root);
        getRootPane().setDefaultButton(btnSave);
        btnCancel.addActionListener(e -> dispose());
        pack();
        setLocationRelativeTo(owner);
    }

    /**
     * Mật khẩu cũ đã nhập.
     *
     * @return chuỗi mật khẩu
     */
    public String getOldPassword() {
        return new String(txtOld.getPassword());
    }

    /**
     * Mật khẩu mới đã nhập.
     *
     * @return chuỗi mật khẩu
     */
    public String getNewPassword() {
        return new String(txtNew.getPassword());
    }

    /**
     * Mật khẩu nhập lại.
     *
     * @return chuỗi mật khẩu
     */
    public String getConfirmPassword() {
        return new String(txtConfirm.getPassword());
    }

    /**
     * Nút lưu.
     *
     * @return nút
     */
    public JButton getBtnSave() {
        return btnSave;
    }
}

package view;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

/**
 * Màn hình đăng nhập. Chỉ chứa thành phần giao diện; xử lý nằm ở LoginController.
 */
public class LoginFrame extends JFrame {
    private static final long serialVersionUID = 1L;

    private final JTextField txtUsername = new JTextField(18);
    private final JPasswordField txtPassword = new JPasswordField(18);
    private final JButton btnLogin = UiKit.button("Đăng nhập");
    private final JButton btnExit = UiKit.button("Thoát");

    /**
     * Dựng giao diện màn hình đăng nhập.
     */
    public LoginFrame() {
        super("Đăng nhập - Quản lý cửa hàng giày dép");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));
        javax.swing.JLabel title = UiKit.title("CỬA HÀNG GIÀY DÉP");
        title.setHorizontalAlignment(SwingConstants.CENTER);
        root.add(title, BorderLayout.NORTH);
        root.add(UiKit.form(null, new String[] { "Tên đăng nhập:", "Mật khẩu:" },
                new JComponent[] { txtUsername, txtPassword }), BorderLayout.CENTER);
        JPanel buttons = new JPanel();
        buttons.add(btnLogin);
        buttons.add(btnExit);
        root.add(buttons, BorderLayout.SOUTH);
        setContentPane(root);
        getRootPane().setDefaultButton(btnLogin);
        setMinimumSize(new Dimension(380, 220));
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
    }

    /**
     * Ô nhập tên đăng nhập.
     *
     * @return ô nhập
     */
    public JTextField getTxtUsername() {
        return txtUsername;
    }

    /**
     * Ô nhập mật khẩu.
     *
     * @return ô mật khẩu
     */
    public JPasswordField getTxtPassword() {
        return txtPassword;
    }

    /**
     * Nút đăng nhập.
     *
     * @return nút
     */
    public JButton getBtnLogin() {
        return btnLogin;
    }

    /**
     * Nút thoát.
     *
     * @return nút
     */
    public JButton getBtnExit() {
        return btnExit;
    }
}

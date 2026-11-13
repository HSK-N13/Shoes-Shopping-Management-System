package controller;

import java.awt.Component;

import model.User;
import services.AuthService;
import view.LoginFrame;
import view.MainFrame;

/**
 * Xử lý màn hình đăng nhập: kiểm tra tài khoản rồi mở cửa sổ chính theo vai trò.
 */
public class LoginController extends BaseController {
    private final LoginFrame view;
    private final AuthService authService = new AuthService();

    /**
     * Gắn controller vào màn hình đăng nhập và đăng ký sự kiện.
     *
     * @param view màn hình đăng nhập
     */
    public LoginController(LoginFrame view) {
        this.view = view;
        view.getBtnLogin().addActionListener(e -> handle(this::login));
        view.getBtnExit().addActionListener(e -> System.exit(0));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Component parent() {
        return view;
    }

    /**
     * Hiển thị màn hình đăng nhập.
     */
    public void show() {
        view.getTxtPassword().setText("");
        view.setVisible(true);
        view.getTxtUsername().requestFocusInWindow();
    }

    /**
     * Đăng nhập; thành công thì đóng màn hình đăng nhập và mở cửa sổ chính.
     */
    private void login() {
        User u = authService.login(view.getTxtUsername().getText(), new String(view.getTxtPassword().getPassword()));
        view.getTxtPassword().setText("");
        view.setVisible(false);
        new MainController(new MainFrame(), u, this).show();
    }
}

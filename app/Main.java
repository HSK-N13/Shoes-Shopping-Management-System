package app;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import controller.LoginController;
import view.LoginFrame;

/**
 * Điểm khởi động chương trình Quản lý cửa hàng giày dép.
 */
public class Main {

    /**
     * Đặt giao diện Nimbus (nếu có) rồi mở màn hình đăng nhập trên luồng giao diện.
     *
     * @param args tham số dòng lệnh (không dùng)
     */
    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
            // Dùng giao diện mặc định nếu không đặt được Nimbus
        }
        SwingUtilities.invokeLater(() -> new LoginController(new LoginFrame()).show());
    }
}

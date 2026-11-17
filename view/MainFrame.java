package view;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;

/**
 * Cửa sổ chính sau khi đăng nhập: thanh menu hệ thống, các tab chức năng
 * (được MainController thêm vào theo vai trò) và thanh trạng thái.
 */
public class MainFrame extends JFrame {
    private static final long serialVersionUID = 1L;

    private final JTabbedPane tabs = new JTabbedPane();
    private final JMenuItem miChangePassword = new JMenuItem("Đổi mật khẩu");
    private final JMenuItem miLogout = new JMenuItem("Đăng xuất");
    private final JMenuItem miExit = new JMenuItem("Thoát");
    private final JLabel lblStatus = new JLabel();

    /**
     * Dựng khung cửa sổ chính.
     */
    public MainFrame() {
        super("Quản lý cửa hàng giày dép");
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        JMenuBar bar = new JMenuBar();
        JMenu system = new JMenu("Hệ thống");
        system.add(miChangePassword);
        system.add(miLogout);
        system.addSeparator();
        system.add(miExit);
        bar.add(system);
        setJMenuBar(bar);

        JPanel root = new JPanel(new BorderLayout());
        root.add(tabs, BorderLayout.CENTER);
        lblStatus.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        root.add(lblStatus, BorderLayout.SOUTH);
        setContentPane(root);
        setMinimumSize(new Dimension(1100, 680));
        setSize(1280, 760);
        setLocationRelativeTo(null);
    }

    /**
     * Thêm một tab chức năng.
     *
     * @param title tên tab
     * @param panel nội dung tab
     */
    public void addTab(String title, JPanel panel) {
        tabs.addTab(title, panel);
    }

    /**
     * Khung chứa các tab.
     *
     * @return JTabbedPane
     */
    public JTabbedPane getTabs() {
        return tabs;
    }

    /**
     * Menu đổi mật khẩu.
     *
     * @return mục menu
     */
    public JMenuItem getMiChangePassword() {
        return miChangePassword;
    }

    /**
     * Menu đăng xuất.
     *
     * @return mục menu
     */
    public JMenuItem getMiLogout() {
        return miLogout;
    }

    /**
     * Menu thoát.
     *
     * @return mục menu
     */
    public JMenuItem getMiExit() {
        return miExit;
    }

    /**
     * Ghi nội dung thanh trạng thái (người dùng đang đăng nhập).
     *
     * @param text nội dung
     */
    public void setStatus(String text) {
        lblStatus.setText(text);
    }
}

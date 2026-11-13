package controller;

import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import connectDB.ConnectDB;
import model.User;
import services.AuthService;
import utils.Session;
import view.ChangePasswordDialog;
import view.MainFrame;
import view.OrderListPanel;
import view.ProductPanel;
import view.PurchasePanel;
import view.ReportPanel;
import view.SalesPanel;
import view.SimpleCrudPanel;
import view.StocktakePanel;
import view.UiKit;
import view.UserPanel;

/**
 * Điều phối cửa sổ chính: tạo các màn hình và controller theo vai trò (BR12),
 * làm mới dữ liệu khi chuyển tab, xử lý menu hệ thống.
 */
public class MainController extends BaseController {
    private final MainFrame view;
    private final LoginController loginController;
    private final AuthService authService = new AuthService();
    private final List<Runnable> refreshers = new ArrayList<>();

    /**
     * Dựng cửa sổ chính cho người dùng vừa đăng nhập.
     *
     * @param view            cửa sổ chính
     * @param user            người dùng đăng nhập
     * @param loginController controller đăng nhập (để quay lại khi đăng xuất)
     */
    public MainController(MainFrame view, User user, LoginController loginController) {
        this.view = view;
        this.loginController = loginController;
        boolean admin = Session.isAdmin();
        view.setStatus("Đang đăng nhập: " + user.getFullName() + " (" + user.getUsername() + ") - "
                + user.getRole().getLabel());

        SalesPanel salesPanel = new SalesPanel();
        SalesController sales = new SalesController(salesPanel);
        addTab("Bán hàng", salesPanel, sales::refresh);

        OrderListPanel orderPanel = new OrderListPanel();
        OrderListController orders = new OrderListController(orderPanel, id -> {
            view.getTabs().setSelectedComponent(salesPanel);
            sales.openDraft(id);
        });
        addTab("Đơn hàng", orderPanel, orders::refresh);

        ProductPanel productPanel = new ProductPanel();
        ProductController products = new ProductController(productPanel);
        addTab(admin ? "Sản phẩm" : "Tra cứu sản phẩm", productPanel, products::refresh);

        if (admin) {
            SimpleCrudPanel catPanel = new SimpleCrudPanel("Danh mục", "Tên danh mục:", null, true,
                    "ID", "Tên danh mục", "Trạng thái");
            CatalogController cats = new CatalogController(catPanel, CatalogController.Kind.CATEGORY);
            addTab("Danh mục", catPanel, () -> handle(cats::refresh));
            SimpleCrudPanel brandPanel = new SimpleCrudPanel("Thương hiệu", "Tên thương hiệu:", null, true,
                    "ID", "Tên thương hiệu", "Trạng thái");
            CatalogController brands = new CatalogController(brandPanel, CatalogController.Kind.BRAND);
            addTab("Thương hiệu", brandPanel, () -> handle(brands::refresh));
        }

        SimpleCrudPanel custPanel = new SimpleCrudPanel("Khách hàng", "Họ tên:", "Số điện thoại:", false,
                "ID", "Họ tên", "Số điện thoại");
        PartnerController customers = new PartnerController(custPanel, PartnerController.Kind.CUSTOMER);
        addTab("Khách hàng", custPanel, () -> handle(customers::refresh));

        SimpleCrudPanel supPanel = new SimpleCrudPanel("Nhà cung cấp", "Tên nhà cung cấp:", "Số điện thoại:", false,
                "ID", "Tên nhà cung cấp", "Số điện thoại");
        PartnerController suppliers = new PartnerController(supPanel, PartnerController.Kind.SUPPLIER);
        addTab("Nhà cung cấp", supPanel, () -> handle(suppliers::refresh));

        PurchasePanel purchasePanel = new PurchasePanel();
        PurchaseController purchases = new PurchaseController(purchasePanel);
        addTab("Nhập hàng", purchasePanel, purchases::refresh);

        StocktakePanel stockPanel = new StocktakePanel();
        StocktakeController stock = new StocktakeController(stockPanel);
        addTab("Kiểm kê", stockPanel, stock::refresh);

        ReportPanel reportPanel = new ReportPanel();
        ReportController reports = new ReportController(reportPanel);
        addTab("Báo cáo", reportPanel, reports::refresh);

        if (admin) {
            UserPanel userPanel = new UserPanel();
            UserController users = new UserController(userPanel);
            addTab("Tài khoản", userPanel, users::refresh);
        }

        view.getTabs().addChangeListener(e -> refreshSelectedTab());
        view.getMiChangePassword().addActionListener(e -> changePassword());
        view.getMiLogout().addActionListener(e -> logout());
        view.getMiExit().addActionListener(e -> exit());
        view.addWindowListener(new WindowAdapter() {
            /**
             * Bấm nút đóng cửa sổ thì hỏi trước khi thoát.
             *
             * @param e sự kiện cửa sổ
             */
            @Override
            public void windowClosing(WindowEvent e) {
                exit();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Component parent() {
        return view;
    }

    /**
     * Hiển thị cửa sổ chính và nạp dữ liệu tab đầu tiên.
     */
    public void show() {
        view.setVisible(true);
        refreshSelectedTab();
    }

    /**
     * Thêm tab và ghi nhớ hàm làm mới tương ứng.
     *
     * @param title     tên tab
     * @param panel     nội dung tab
     * @param refresher hàm làm mới khi người dùng mở tab
     */
    private void addTab(String title, JPanel panel, Runnable refresher) {
        view.addTab(title, panel);
        refreshers.add(refresher);
    }

    /**
     * Làm mới dữ liệu của tab đang được chọn.
     */
    private void refreshSelectedTab() {
        int i = view.getTabs().getSelectedIndex();
        if (i >= 0 && i < refreshers.size()) {
            refreshers.get(i).run();
        }
    }

    /**
     * Mở hộp thoại đổi mật khẩu.
     */
    private void changePassword() {
        ChangePasswordDialog dlg = new ChangePasswordDialog(view);
        dlg.getBtnSave().addActionListener(e -> {
            try {
                authService.changePassword(dlg.getOldPassword(), dlg.getNewPassword(), dlg.getConfirmPassword());
                UiKit.info(dlg, "Đã đổi mật khẩu.");
                dlg.dispose();
            } catch (RuntimeException ex) {
                UiKit.error(dlg, ex.getMessage());
            }
        });
        dlg.setVisible(true);
    }

    /**
     * Đăng xuất và quay về màn hình đăng nhập.
     */
    private void logout() {
        if (!UiKit.confirm(view, "Đăng xuất khỏi hệ thống?")) {
            return;
        }
        authService.logout();
        view.dispose();
        loginController.show();
    }

    /**
     * Thoát ứng dụng sau khi xác nhận, đóng kết nối database.
     */
    private void exit() {
        if (UiKit.confirm(view, "Thoát chương trình?")) {
            ConnectDB.getInstance().disconnect();
            System.exit(0);
        }
    }
}

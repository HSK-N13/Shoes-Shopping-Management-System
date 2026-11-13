package controller;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

import model.Role;
import model.User;
import services.UserService;
import utils.BusinessException;
import view.UiKit;
import view.UserPanel;

/**
 * Xử lý màn hình quản lý tài khoản: thêm, sửa vai trò/trạng thái, đặt lại mật khẩu.
 */
public class UserController extends BaseController {
    private final UserPanel view;
    private final UserService userService = new UserService();
    private List<User> users = new ArrayList<>();
    private Long selectedId;

    /**
     * Gắn controller vào màn hình và đăng ký sự kiện.
     *
     * @param view màn hình tài khoản
     */
    public UserController(UserPanel view) {
        this.view = view;
        view.btnNew.addActionListener(e -> clear());
        view.btnSave.addActionListener(e -> handle(this::save));
        view.btnResetPassword.addActionListener(e -> handle(this::resetPassword));
        view.tblUsers.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onSelected();
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
     * Nạp lại danh sách tài khoản (gọi khi mở tab).
     */
    public void refresh() {
        handle(this::load);
    }

    /**
     * Đọc danh sách tài khoản và hiển thị.
     */
    private void load() {
        users = userService.getAll();
        List<Object[]> rows = new ArrayList<>();
        for (User u : users) {
            rows.add(new Object[] { u.getId(), u.getUsername(), u.getFullName(), u.getRole().getLabel(),
                    u.isActive() ? "Hoạt động" : "Đã khóa" });
        }
        UiKit.fill(view.tblUsers, rows);
        clear();
    }

    /**
     * Khi chọn tài khoản: đổ dữ liệu lên form, khóa ô tên đăng nhập và mật khẩu.
     */
    private void onSelected() {
        int row = UiKit.selectedModelRow(view.tblUsers);
        if (row < 0) {
            return;
        }
        Long id = (Long) view.tblUsers.getModel().getValueAt(row, 0);
        for (User u : users) {
            if (u.getId().equals(id)) {
                selectedId = id;
                view.txtUsername.setText(u.getUsername());
                view.txtUsername.setEditable(false);
                view.txtFullName.setText(u.getFullName());
                view.cboRole.setSelectedItem(u.getRole());
                view.chkActive.setSelected(u.isActive());
                view.txtPassword.setText("");
                view.txtPassword.setEnabled(false);
            }
        }
    }

    /**
     * Tạo tài khoản mới hoặc cập nhật tài khoản đang chọn.
     */
    private void save() {
        Role role = (Role) view.cboRole.getSelectedItem();
        if (selectedId == null) {
            User u = userService.create(view.txtUsername.getText(), view.txtFullName.getText(), role,
                    new String(view.txtPassword.getPassword()));
            UiKit.info(view, "Đã tạo tài khoản " + u.getUsername() + ".");
        } else {
            userService.update(selectedId, view.txtFullName.getText(), role, view.chkActive.isSelected());
            UiKit.info(view, "Đã cập nhật tài khoản.");
        }
        load();
    }

    /**
     * Đặt lại mật khẩu cho tài khoản đang chọn.
     */
    private void resetPassword() {
        if (selectedId == null) {
            throw new BusinessException("Hãy chọn tài khoản cần đặt lại mật khẩu.");
        }
        JPasswordField pf = new JPasswordField(16);
        int r = JOptionPane.showConfirmDialog(view, pf, "Mật khẩu mới (ít nhất 6 ký tự)", JOptionPane.OK_CANCEL_OPTION);
        if (r == JOptionPane.OK_OPTION) {
            userService.resetPassword(selectedId, new String(pf.getPassword()));
            UiKit.info(view, "Đã đặt lại mật khẩu.");
        }
    }

    /**
     * Làm trống form để tạo tài khoản mới.
     */
    private void clear() {
        selectedId = null;
        view.tblUsers.clearSelection();
        view.txtUsername.setText("");
        view.txtUsername.setEditable(true);
        view.txtFullName.setText("");
        view.cboRole.setSelectedItem(Role.STAFF);
        view.chkActive.setSelected(true);
        view.txtPassword.setText("");
        view.txtPassword.setEnabled(true);
    }
}

package services;

import dao.UserDAO;
import model.User;
import utils.BusinessException;
import utils.PasswordUtil;
import utils.Session;

/**
 * Đăng nhập, đăng xuất và đổi mật khẩu của người dùng hiện tại.
 */
public class AuthService extends BaseService {
    private final UserDAO userDAO = new UserDAO();

    /**
     * Đăng nhập: kiểm tra tài khoản tồn tại, đang hoạt động và đúng mật khẩu.
     * Thành công thì lưu người dùng vào Session (BR12).
     *
     * @param username tên đăng nhập
     * @param password mật khẩu dạng rõ
     * @return người dùng đã đăng nhập
     * @throws BusinessException nếu sai thông tin hoặc tài khoản bị khóa
     */
    public User login(String username, String password) {
        String un = required(username, "Tên đăng nhập");
        if (password == null || password.isEmpty()) {
            throw new BusinessException("Mật khẩu không được để trống.");
        }
        User u = db(() -> userDAO.findByUsername(un));
        if (u == null || !PasswordUtil.verify(password, u.getPasswordHash())) {
            throw new BusinessException("Tên đăng nhập hoặc mật khẩu không đúng.");
        }
        if (!u.isActive()) {
            throw new BusinessException("Tài khoản đã bị khóa. Liên hệ Admin.");
        }
        Session.setCurrentUser(u);
        return u;
    }

    /**
     * Đăng xuất người dùng hiện tại.
     */
    public void logout() {
        Session.clear();
    }

    /**
     * Người dùng hiện tại tự đổi mật khẩu.
     *
     * @param oldPassword mật khẩu cũ
     * @param newPassword mật khẩu mới
     * @param confirm     nhập lại mật khẩu mới
     * @throws BusinessException nếu mật khẩu cũ sai hoặc mật khẩu mới không hợp lệ
     */
    public void changePassword(String oldPassword, String newPassword, String confirm) {
        User u = Session.requireLogin();
        User fresh = db(() -> userDAO.findById(u.getId()));
        if (fresh == null || !PasswordUtil.verify(oldPassword, fresh.getPasswordHash())) {
            throw new BusinessException("Mật khẩu cũ không đúng.");
        }
        UserService.validatePassword(newPassword);
        if (!newPassword.equals(confirm)) {
            throw new BusinessException("Mật khẩu nhập lại không khớp.");
        }
        String hash = PasswordUtil.hash(newPassword);
        db(() -> userDAO.updatePassword(u.getId(), hash));
        u.setPasswordHash(hash);
    }
}

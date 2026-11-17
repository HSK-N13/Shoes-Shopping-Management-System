package services;

import java.util.List;

import dao.UserDAO;
import model.Role;
import model.User;
import utils.BusinessException;
import utils.PasswordUtil;
import utils.Session;

/**
 * Quản lý tài khoản người dùng. Chỉ Admin được dùng (BR12).
 */
public class UserService extends BaseService {
    private final UserDAO userDAO = new UserDAO();

    /**
     * Lấy toàn bộ tài khoản.
     *
     * @return danh sách người dùng
     */
    public List<User> getAll() {
        Session.requireAdmin();
        return db(userDAO::findAll);
    }

    /**
     * Tạo tài khoản mới với mật khẩu được băm (BR12: đúng một vai trò).
     *
     * @param username tên đăng nhập (duy nhất)
     * @param fullName họ tên
     * @param role     vai trò ADMIN hoặc STAFF
     * @param password mật khẩu ban đầu
     * @return người dùng vừa tạo
     */
    public User create(String username, String fullName, Role role, String password) {
        Session.requireAdmin();
        String un = required(username, "Tên đăng nhập");
        if (!un.matches("[A-Za-z0-9_.]{3,50}")) {
            throw new BusinessException("Tên đăng nhập chỉ gồm chữ không dấu, số, dấu _ hoặc . (3-50 ký tự).");
        }
        if (role == null) {
            throw new BusinessException("Phải chọn vai trò.");
        }
        validatePassword(password);
        if (db(() -> userDAO.findByUsername(un)) != null) {
            throw new BusinessException("Tên đăng nhập '" + un + "' đã tồn tại.");
        }
        User u = new User();
        u.setUsername(un);
        u.setFullName(required(fullName, "Họ tên"));
        u.setRole(role);
        u.setActive(true);
        u.setPasswordHash(PasswordUtil.hash(password));
        db(() -> userDAO.insert(u));
        return u;
    }

    /**
     * Cập nhật họ tên, vai trò, trạng thái. Không cho tự khóa/hạ quyền chính mình
     * và không để hệ thống mất Admin cuối cùng.
     *
     * @param id       mã người dùng
     * @param fullName họ tên mới
     * @param role     vai trò mới
     * @param active   trạng thái mới
     */
    public void update(Long id, String fullName, Role role, boolean active) {
        User me = Session.requireAdmin();
        User u = db(() -> userDAO.findById(id));
        if (u == null) {
            throw new BusinessException("Không tìm thấy tài khoản.");
        }
        if (role == null) {
            throw new BusinessException("Phải chọn vai trò.");
        }
        if (u.getId().equals(me.getId()) && (role != Role.ADMIN || !active)) {
            throw new BusinessException("Không thể tự khóa hoặc hạ quyền tài khoản đang đăng nhập.");
        }
        boolean wasActiveAdmin = u.getRole() == Role.ADMIN && u.isActive();
        boolean stillActiveAdmin = role == Role.ADMIN && active;
        if (wasActiveAdmin && !stillActiveAdmin && db(userDAO::countActiveAdmins) <= 1) {
            throw new BusinessException("Hệ thống phải còn ít nhất một Admin đang hoạt động.");
        }
        u.setFullName(required(fullName, "Họ tên"));
        u.setRole(role);
        u.setActive(active);
        db(() -> userDAO.update(u));
    }

    /**
     * Admin đặt lại mật khẩu cho một tài khoản.
     *
     * @param id          mã người dùng
     * @param newPassword mật khẩu mới
     */
    public void resetPassword(Long id, String newPassword) {
        Session.requireAdmin();
        validatePassword(newPassword);
        String hash = PasswordUtil.hash(newPassword);
        if (!db(() -> userDAO.updatePassword(id, hash))) {
            throw new BusinessException("Không tìm thấy tài khoản.");
        }
    }

    /**
     * Kiểm tra độ dài tối thiểu của mật khẩu.
     *
     * @param password mật khẩu
     * @throws BusinessException nếu mật khẩu ngắn hơn 6 ký tự
     */
    public static void validatePassword(String password) {
        if (password == null || password.length() < 6) {
            throw new BusinessException("Mật khẩu phải có ít nhất 6 ký tự.");
        }
    }
}

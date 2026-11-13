package utils;

import model.Role;
import model.User;

/**
 * Lưu người dùng đang đăng nhập (BR12). View dùng để ẩn/hiện chức năng
 * theo vai trò, Service dùng để kiểm tra lại quyền trước khi xử lý.
 */
public final class Session {
    private static User currentUser;

    /**
     * Không cho tạo đối tượng vì lớp chỉ chứa phương thức tĩnh.
     */
    private Session() {
    }

    /**
     * Lấy người dùng đang đăng nhập.
     *
     * @return người dùng hiện tại, hoặc null nếu chưa đăng nhập
     */
    public static User getCurrentUser() {
        return currentUser;
    }

    /**
     * Ghi nhận người dùng vừa đăng nhập thành công.
     *
     * @param user người dùng đăng nhập
     */
    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    /**
     * Xóa thông tin đăng nhập (đăng xuất).
     */
    public static void clear() {
        currentUser = null;
    }

    /**
     * Kiểm tra người dùng hiện tại có phải Admin không.
     *
     * @return true nếu đã đăng nhập với vai trò ADMIN
     */
    public static boolean isAdmin() {
        return currentUser != null && currentUser.getRole() == Role.ADMIN;
    }

    /**
     * Bắt buộc đã đăng nhập; dùng ở đầu các phương thức Service.
     *
     * @return người dùng hiện tại
     * @throws BusinessException nếu chưa đăng nhập
     */
    public static User requireLogin() {
        if (currentUser == null) {
            throw new BusinessException("Bạn cần đăng nhập để thực hiện chức năng này.");
        }
        return currentUser;
    }

    /**
     * Bắt buộc người dùng hiện tại là Admin (BR12).
     *
     * @return người dùng hiện tại
     * @throws BusinessException nếu không phải Admin
     */
    public static User requireAdmin() {
        User u = requireLogin();
        if (u.getRole() != Role.ADMIN) {
            throw new BusinessException("Chỉ Admin mới được thực hiện chức năng này.");
        }
        return u;
    }
}

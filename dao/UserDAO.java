package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import model.Role;
import model.User;

/**
 * Truy cập bảng users (tài khoản đăng nhập).
 */
public class UserDAO extends BaseDAO {

    /**
     * Lấy toàn bộ tài khoản.
     *
     * @return danh sách người dùng
     * @throws SQLException lỗi database
     */
    public ArrayList<User> findAll() throws SQLException {
        ArrayList<User> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement("SELECT * FROM users ORDER BY username");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    /**
     * Tìm tài khoản theo tên đăng nhập.
     *
     * @param username tên đăng nhập
     * @return người dùng hoặc null
     * @throws SQLException lỗi database
     */
    public User findByUsername(String username) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement("SELECT * FROM users WHERE username = ?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    /**
     * Tìm tài khoản theo mã.
     *
     * @param id mã người dùng
     * @return người dùng hoặc null
     * @throws SQLException lỗi database
     */
    public User findById(Long id) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement("SELECT * FROM users WHERE id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    /**
     * Thêm tài khoản mới (mật khẩu đã được băm ở Service).
     *
     * @param u người dùng cần thêm
     * @return true nếu thành công
     * @throws SQLException lỗi database (ví dụ trùng username)
     */
    public boolean insert(User u) throws SQLException {
        String sql = "INSERT INTO users (username, password_hash, full_name, role, active) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getUsername());
            ps.setString(2, u.getPasswordHash());
            ps.setString(3, u.getFullName());
            ps.setString(4, u.getRole().name());
            ps.setBoolean(5, u.isActive());
            u.setId(executeInsert(ps));
            return true;
        }
    }

    /**
     * Cập nhật họ tên, vai trò và trạng thái (không đổi mật khẩu).
     *
     * @param u người dùng với dữ liệu mới
     * @return true nếu có dòng được cập nhật
     * @throws SQLException lỗi database
     */
    public boolean update(User u) throws SQLException {
        String sql = "UPDATE users SET full_name = ?, role = ?, active = ? WHERE id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, u.getFullName());
            ps.setString(2, u.getRole().name());
            ps.setBoolean(3, u.isActive());
            ps.setLong(4, u.getId());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Đổi mật khẩu (đã băm) của tài khoản.
     *
     * @param id           mã người dùng
     * @param passwordHash chuỗi băm mới
     * @return true nếu có dòng được cập nhật
     * @throws SQLException lỗi database
     */
    public boolean updatePassword(Long id, String passwordHash) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement("UPDATE users SET password_hash = ? WHERE id = ?")) {
            ps.setString(1, passwordHash);
            ps.setLong(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Đếm số Admin đang hoạt động (để không khóa mất Admin cuối cùng).
     *
     * @return số Admin active
     * @throws SQLException lỗi database
     */
    public int countActiveAdmins() throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT COUNT(*) FROM users WHERE role = 'ADMIN' AND active = 1");
                ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /**
     * Chuyển một dòng kết quả thành đối tượng User.
     *
     * @param rs kết quả truy vấn đang trỏ tới dòng cần đọc
     * @return người dùng
     * @throws SQLException lỗi đọc cột
     */
    private User map(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getLong("id"));
        u.setUsername(rs.getString("username"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setFullName(rs.getString("full_name"));
        u.setRole(Role.valueOf(rs.getString("role")));
        u.setActive(rs.getBoolean("active"));
        return u;
    }
}

package model;

/**
 * Tài khoản đăng nhập hệ thống; mỗi người dùng có đúng một vai trò ADMIN hoặc STAFF (BR12).
 */
public class User {
    /** Mã người dùng. */
    private Long id;
    /** Tên đăng nhập (duy nhất). */
    private String username;
    /** Mật khẩu đã băm. */
    private String passwordHash;
    /** Họ tên. */
    private String fullName;
    /** Vai trò. */
    private Role role;
    /** Trạng thái hoạt động. */
    private boolean active = true;

    /**
     * Tạo đối tượng rỗng.
     */
    public User() {
    }

    /**
     * Lấy mã người dùng.
     *
     * @return mã người dùng
     */
    public Long getId() {
        return id;
    }

    /**
     * Gán mã người dùng.
     *
     * @param id mã người dùng
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Lấy tên đăng nhập (duy nhất).
     *
     * @return tên đăng nhập (duy nhất)
     */
    public String getUsername() {
        return username;
    }

    /**
     * Gán tên đăng nhập (duy nhất).
     *
     * @param username tên đăng nhập (duy nhất)
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Lấy mật khẩu đã băm.
     *
     * @return mật khẩu đã băm
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * Gán mật khẩu đã băm.
     *
     * @param passwordHash mật khẩu đã băm
     */
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    /**
     * Lấy họ tên.
     *
     * @return họ tên
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Gán họ tên.
     *
     * @param fullName họ tên
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * Lấy vai trò.
     *
     * @return vai trò
     */
    public Role getRole() {
        return role;
    }

    /**
     * Gán vai trò.
     *
     * @param role vai trò
     */
    public void setRole(Role role) {
        this.role = role;
    }

    /**
     * Lấy trạng thái hoạt động.
     *
     * @return trạng thái hoạt động
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Gán trạng thái hoạt động.
     *
     * @param active trạng thái hoạt động
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Chuỗi hiển thị trong ComboBox/JList.
     *
     * @return chuỗi mô tả ngắn gọn
     */
    @Override
    public String toString() {
        return fullName + " (" + username + ")";
    }
}

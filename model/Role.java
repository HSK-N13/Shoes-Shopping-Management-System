package model;

/**
 * Vai trò người dùng (BR12).
 */
public enum Role {
    /** Quản trị viên. */
    ADMIN("Quản trị viên"),
    /** Nhân viên. */
    STAFF("Nhân viên");

    /** Nhãn tiếng Việt hiển thị trên giao diện. */
    private final String label;

    /**
     * Khởi tạo hằng enum với nhãn hiển thị.
     *
     * @param label nhãn tiếng Việt
     */
    Role(String label) {
        this.label = label;
    }

    /**
     * Lấy nhãn tiếng Việt.
     *
     * @return nhãn hiển thị
     */
    public String getLabel() {
        return label;
    }

    /**
     * Hiển thị nhãn tiếng Việt trong ComboBox.
     *
     * @return nhãn hiển thị
     */
    @Override
    public String toString() {
        return label;
    }
}

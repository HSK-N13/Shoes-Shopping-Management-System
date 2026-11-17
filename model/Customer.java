package model;

/**
 * Khách hàng (không bắt buộc gắn vào đơn).
 */
public class Customer {
    /** Mã khách hàng. */
    private Long id;
    /** Họ tên. */
    private String fullName;
    /** Số điện thoại. */
    private String phone;

    /**
     * Tạo đối tượng rỗng.
     */
    public Customer() {
    }

    /**
     * Lấy mã khách hàng.
     *
     * @return mã khách hàng
     */
    public Long getId() {
        return id;
    }

    /**
     * Gán mã khách hàng.
     *
     * @param id mã khách hàng
     */
    public void setId(Long id) {
        this.id = id;
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
     * Lấy số điện thoại.
     *
     * @return số điện thoại
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Gán số điện thoại.
     *
     * @param phone số điện thoại
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Chuỗi hiển thị trong ComboBox/JList.
     *
     * @return chuỗi mô tả ngắn gọn
     */
    @Override
    public String toString() {
        return fullName + (phone == null || phone.isEmpty() ? "" : " - " + phone);
    }
}

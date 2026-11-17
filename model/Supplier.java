package model;

/**
 * Nhà cung cấp hàng nhập.
 */
public class Supplier {
    /** Mã nhà cung cấp. */
    private Long id;
    /** Tên nhà cung cấp. */
    private String name;
    /** Số điện thoại. */
    private String phone;

    /**
     * Tạo đối tượng rỗng.
     */
    public Supplier() {
    }

    /**
     * Lấy mã nhà cung cấp.
     *
     * @return mã nhà cung cấp
     */
    public Long getId() {
        return id;
    }

    /**
     * Gán mã nhà cung cấp.
     *
     * @param id mã nhà cung cấp
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Lấy tên nhà cung cấp.
     *
     * @return tên nhà cung cấp
     */
    public String getName() {
        return name;
    }

    /**
     * Gán tên nhà cung cấp.
     *
     * @param name tên nhà cung cấp
     */
    public void setName(String name) {
        this.name = name;
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
        return name;
    }
}

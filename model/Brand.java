package model;

/**
 * Thương hiệu sản phẩm (ví dụ: Nike, Adidas).
 */
public class Brand {
    /** Mã thương hiệu. */
    private Long id;
    /** Tên thương hiệu. */
    private String name;
    /** Trạng thái sử dụng. */
    private boolean active = true;

    /**
     * Tạo đối tượng rỗng.
     */
    public Brand() {
    }

    /**
     * Lấy mã thương hiệu.
     *
     * @return mã thương hiệu
     */
    public Long getId() {
        return id;
    }

    /**
     * Gán mã thương hiệu.
     *
     * @param id mã thương hiệu
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Lấy tên thương hiệu.
     *
     * @return tên thương hiệu
     */
    public String getName() {
        return name;
    }

    /**
     * Gán tên thương hiệu.
     *
     * @param name tên thương hiệu
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Lấy trạng thái sử dụng.
     *
     * @return trạng thái sử dụng
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Gán trạng thái sử dụng.
     *
     * @param active trạng thái sử dụng
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
        return name;
    }
}

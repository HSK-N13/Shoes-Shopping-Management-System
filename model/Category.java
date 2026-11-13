package model;

/**
 * Danh mục sản phẩm (ví dụ: Giày thể thao, Giày chạy bộ).
 */
public class Category {
    /** Mã danh mục. */
    private Long id;
    /** Tên danh mục. */
    private String name;
    /** Trạng thái sử dụng. */
    private boolean active = true;

    /**
     * Tạo đối tượng rỗng.
     */
    public Category() {
    }

    /**
     * Lấy mã danh mục.
     *
     * @return mã danh mục
     */
    public Long getId() {
        return id;
    }

    /**
     * Gán mã danh mục.
     *
     * @param id mã danh mục
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Lấy tên danh mục.
     *
     * @return tên danh mục
     */
    public String getName() {
        return name;
    }

    /**
     * Gán tên danh mục.
     *
     * @param name tên danh mục
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

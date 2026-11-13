package model;

/**
 * Mẫu giày, không gắn size/màu cụ thể; các size/màu nằm ở ProductVariant.
 */
public class Product {
    /** Mã sản phẩm. */
    private Long id;
    /** Mã danh mục. */
    private Long categoryId;
    /** Mã thương hiệu. */
    private Long brandId;
    /** Tên mẫu giày. */
    private String name;
    /** Mô tả. */
    private String description;
    /** Trạng thái kinh doanh. */
    private boolean active = true;
    /** Tên danh mục (chỉ để hiển thị). */
    private String categoryName;
    /** Tên thương hiệu (chỉ để hiển thị). */
    private String brandName;

    /**
     * Tạo đối tượng rỗng.
     */
    public Product() {
    }

    /**
     * Lấy mã sản phẩm.
     *
     * @return mã sản phẩm
     */
    public Long getId() {
        return id;
    }

    /**
     * Gán mã sản phẩm.
     *
     * @param id mã sản phẩm
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Lấy mã danh mục.
     *
     * @return mã danh mục
     */
    public Long getCategoryId() {
        return categoryId;
    }

    /**
     * Gán mã danh mục.
     *
     * @param categoryId mã danh mục
     */
    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    /**
     * Lấy mã thương hiệu.
     *
     * @return mã thương hiệu
     */
    public Long getBrandId() {
        return brandId;
    }

    /**
     * Gán mã thương hiệu.
     *
     * @param brandId mã thương hiệu
     */
    public void setBrandId(Long brandId) {
        this.brandId = brandId;
    }

    /**
     * Lấy tên mẫu giày.
     *
     * @return tên mẫu giày
     */
    public String getName() {
        return name;
    }

    /**
     * Gán tên mẫu giày.
     *
     * @param name tên mẫu giày
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Lấy mô tả.
     *
     * @return mô tả
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gán mô tả.
     *
     * @param description mô tả
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Lấy trạng thái kinh doanh.
     *
     * @return trạng thái kinh doanh
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Gán trạng thái kinh doanh.
     *
     * @param active trạng thái kinh doanh
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Lấy tên danh mục (chỉ để hiển thị).
     *
     * @return tên danh mục (chỉ để hiển thị)
     */
    public String getCategoryName() {
        return categoryName;
    }

    /**
     * Gán tên danh mục (chỉ để hiển thị).
     *
     * @param categoryName tên danh mục (chỉ để hiển thị)
     */
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    /**
     * Lấy tên thương hiệu (chỉ để hiển thị).
     *
     * @return tên thương hiệu (chỉ để hiển thị)
     */
    public String getBrandName() {
        return brandName;
    }

    /**
     * Gán tên thương hiệu (chỉ để hiển thị).
     *
     * @param brandName tên thương hiệu (chỉ để hiển thị)
     */
    public void setBrandName(String brandName) {
        this.brandName = brandName;
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

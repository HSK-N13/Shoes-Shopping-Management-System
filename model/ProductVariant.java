package model;

import java.math.BigDecimal;

/**
 * Biến thể size + màu của một mẫu giày; là đơn vị thực sự để bán và quản lý kho.
 */
public class ProductVariant {
    /** Mã biến thể. */
    private Long id;
    /** Mã mẫu giày. */
    private Long productId;
    /** Mã SKU (duy nhất, BR01). */
    private String sku;
    /** Size. */
    private String size;
    /** Màu. */
    private String color;
    /** Giá bán hiện tại. */
    private BigDecimal sellingPrice;
    /** Giá vốn = giá nhập gần nhất (BR07). */
    private BigDecimal costPrice;
    /** Số lượng tồn hiện tại. */
    private int stockQuantity;
    /** Ngưỡng cảnh báo tồn tối thiểu. */
    private int minStock;
    /** Trạng thái kinh doanh. */
    private boolean active = true;
    /** Tên mẫu giày (chỉ để hiển thị). */
    private String productName;
    /** Tên thương hiệu (chỉ để hiển thị). */
    private String brandName;

    /**
     * Tạo đối tượng rỗng.
     */
    public ProductVariant() {
    }

    /**
     * Lấy mã biến thể.
     *
     * @return mã biến thể
     */
    public Long getId() {
        return id;
    }

    /**
     * Gán mã biến thể.
     *
     * @param id mã biến thể
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Lấy mã mẫu giày.
     *
     * @return mã mẫu giày
     */
    public Long getProductId() {
        return productId;
    }

    /**
     * Gán mã mẫu giày.
     *
     * @param productId mã mẫu giày
     */
    public void setProductId(Long productId) {
        this.productId = productId;
    }

    /**
     * Lấy mã SKU (duy nhất, BR01).
     *
     * @return mã SKU (duy nhất, BR01)
     */
    public String getSku() {
        return sku;
    }

    /**
     * Gán mã SKU (duy nhất, BR01).
     *
     * @param sku mã SKU (duy nhất, BR01)
     */
    public void setSku(String sku) {
        this.sku = sku;
    }

    /**
     * Lấy size.
     *
     * @return size
     */
    public String getSize() {
        return size;
    }

    /**
     * Gán size.
     *
     * @param size size
     */
    public void setSize(String size) {
        this.size = size;
    }

    /**
     * Lấy màu.
     *
     * @return màu
     */
    public String getColor() {
        return color;
    }

    /**
     * Gán màu.
     *
     * @param color màu
     */
    public void setColor(String color) {
        this.color = color;
    }

    /**
     * Lấy giá bán hiện tại.
     *
     * @return giá bán hiện tại
     */
    public BigDecimal getSellingPrice() {
        return sellingPrice;
    }

    /**
     * Gán giá bán hiện tại.
     *
     * @param sellingPrice giá bán hiện tại
     */
    public void setSellingPrice(BigDecimal sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    /**
     * Lấy giá vốn = giá nhập gần nhất (BR07).
     *
     * @return giá vốn = giá nhập gần nhất (BR07)
     */
    public BigDecimal getCostPrice() {
        return costPrice;
    }

    /**
     * Gán giá vốn = giá nhập gần nhất (BR07).
     *
     * @param costPrice giá vốn = giá nhập gần nhất (BR07)
     */
    public void setCostPrice(BigDecimal costPrice) {
        this.costPrice = costPrice;
    }

    /**
     * Lấy số lượng tồn hiện tại.
     *
     * @return số lượng tồn hiện tại
     */
    public int getStockQuantity() {
        return stockQuantity;
    }

    /**
     * Gán số lượng tồn hiện tại.
     *
     * @param stockQuantity số lượng tồn hiện tại
     */
    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    /**
     * Lấy ngưỡng cảnh báo tồn tối thiểu.
     *
     * @return ngưỡng cảnh báo tồn tối thiểu
     */
    public int getMinStock() {
        return minStock;
    }

    /**
     * Gán ngưỡng cảnh báo tồn tối thiểu.
     *
     * @param minStock ngưỡng cảnh báo tồn tối thiểu
     */
    public void setMinStock(int minStock) {
        this.minStock = minStock;
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
     * Lấy tên mẫu giày (chỉ để hiển thị).
     *
     * @return tên mẫu giày (chỉ để hiển thị)
     */
    public String getProductName() {
        return productName;
    }

    /**
     * Gán tên mẫu giày (chỉ để hiển thị).
     *
     * @param productName tên mẫu giày (chỉ để hiển thị)
     */
    public void setProductName(String productName) {
        this.productName = productName;
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
     * Kiểm tra biến thể có đang dưới ngưỡng tồn tối thiểu không.
     *
     * @return true nếu tồn kho nhỏ hơn ngưỡng tối thiểu
     */
    public boolean isLowStock() {
        return stockQuantity < minStock;
    }

    /**
     * Tên đầy đủ để hiển thị: tên mẫu - size - màu.
     *
     * @return chuỗi mô tả biến thể
     */
    public String getDisplayName() {
        return (productName == null ? "" : productName + " - ") + "Size " + size + " - " + color;
    }

    /**
     * Chuỗi hiển thị trong ComboBox/JList.
     *
     * @return chuỗi mô tả ngắn gọn
     */
    @Override
    public String toString() {
        return sku + " | " + getDisplayName();
    }
}

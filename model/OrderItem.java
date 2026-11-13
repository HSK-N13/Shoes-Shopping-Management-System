package model;

import java.math.BigDecimal;

/**
 * Dòng hàng của đơn; lưu ảnh chụp giá bán và giá vốn tại thời điểm bán (BR06).
 */
public class OrderItem {
    /** Mã dòng. */
    private Long id;
    /** Mã đơn. */
    private Long orderId;
    /** Mã biến thể. */
    private Long variantId;
    /** Số lượng. */
    private int quantity;
    /** Đơn giá bán (ảnh chụp). */
    private BigDecimal unitPrice;
    /** Giá vốn (ảnh chụp). */
    private BigDecimal unitCost;
    /** Giảm giá trên dòng. */
    private BigDecimal discountAmount = BigDecimal.ZERO;
    /** SKU biến thể (chỉ để hiển thị). */
    private String sku;
    /** Tên biến thể (chỉ để hiển thị). */
    private String variantName;

    /**
     * Tạo đối tượng rỗng.
     */
    public OrderItem() {
    }

    /**
     * Lấy mã dòng.
     *
     * @return mã dòng
     */
    public Long getId() {
        return id;
    }

    /**
     * Gán mã dòng.
     *
     * @param id mã dòng
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Lấy mã đơn.
     *
     * @return mã đơn
     */
    public Long getOrderId() {
        return orderId;
    }

    /**
     * Gán mã đơn.
     *
     * @param orderId mã đơn
     */
    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    /**
     * Lấy mã biến thể.
     *
     * @return mã biến thể
     */
    public Long getVariantId() {
        return variantId;
    }

    /**
     * Gán mã biến thể.
     *
     * @param variantId mã biến thể
     */
    public void setVariantId(Long variantId) {
        this.variantId = variantId;
    }

    /**
     * Lấy số lượng.
     *
     * @return số lượng
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Gán số lượng.
     *
     * @param quantity số lượng
     */
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    /**
     * Lấy đơn giá bán (ảnh chụp).
     *
     * @return đơn giá bán (ảnh chụp)
     */
    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    /**
     * Gán đơn giá bán (ảnh chụp).
     *
     * @param unitPrice đơn giá bán (ảnh chụp)
     */
    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    /**
     * Lấy giá vốn (ảnh chụp).
     *
     * @return giá vốn (ảnh chụp)
     */
    public BigDecimal getUnitCost() {
        return unitCost;
    }

    /**
     * Gán giá vốn (ảnh chụp).
     *
     * @param unitCost giá vốn (ảnh chụp)
     */
    public void setUnitCost(BigDecimal unitCost) {
        this.unitCost = unitCost;
    }

    /**
     * Lấy giảm giá trên dòng.
     *
     * @return giảm giá trên dòng
     */
    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    /**
     * Gán giảm giá trên dòng.
     *
     * @param discountAmount giảm giá trên dòng
     */
    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    /**
     * Lấy SKU biến thể (chỉ để hiển thị).
     *
     * @return SKU biến thể (chỉ để hiển thị)
     */
    public String getSku() {
        return sku;
    }

    /**
     * Gán SKU biến thể (chỉ để hiển thị).
     *
     * @param sku SKU biến thể (chỉ để hiển thị)
     */
    public void setSku(String sku) {
        this.sku = sku;
    }

    /**
     * Lấy tên biến thể (chỉ để hiển thị).
     *
     * @return tên biến thể (chỉ để hiển thị)
     */
    public String getVariantName() {
        return variantName;
    }

    /**
     * Gán tên biến thể (chỉ để hiển thị).
     *
     * @param variantName tên biến thể (chỉ để hiển thị)
     */
    public void setVariantName(String variantName) {
        this.variantName = variantName;
    }

    /**
     * Thành tiền trước giảm giá = số lượng x đơn giá.
     *
     * @return thành tiền gộp của dòng
     */
    public BigDecimal getGrossAmount() {
        return unitPrice == null ? BigDecimal.ZERO : unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    /**
     * Thành tiền sau giảm giá dòng = số lượng x đơn giá - giảm dòng (BR09).
     *
     * @return thành tiền thuần của dòng
     */
    public BigDecimal getLineTotal() {
        return getGrossAmount().subtract(discountAmount == null ? BigDecimal.ZERO : discountAmount);
    }
}

package model;

import java.math.BigDecimal;

/**
 * Dòng chi tiết của phiếu nhập: biến thể, số lượng, giá nhập.
 */
public class PurchaseItem {
    /** Mã dòng. */
    private Long id;
    /** Mã phiếu nhập. */
    private Long receiptId;
    /** Mã biến thể. */
    private Long variantId;
    /** Số lượng nhập. */
    private int quantity;
    /** Giá nhập một đơn vị. */
    private BigDecimal unitCost;
    /** SKU biến thể (chỉ để hiển thị). */
    private String sku;
    /** Tên biến thể (chỉ để hiển thị). */
    private String variantName;

    /**
     * Tạo đối tượng rỗng.
     */
    public PurchaseItem() {
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
     * Lấy mã phiếu nhập.
     *
     * @return mã phiếu nhập
     */
    public Long getReceiptId() {
        return receiptId;
    }

    /**
     * Gán mã phiếu nhập.
     *
     * @param receiptId mã phiếu nhập
     */
    public void setReceiptId(Long receiptId) {
        this.receiptId = receiptId;
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
     * Lấy số lượng nhập.
     *
     * @return số lượng nhập
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Gán số lượng nhập.
     *
     * @param quantity số lượng nhập
     */
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    /**
     * Lấy giá nhập một đơn vị.
     *
     * @return giá nhập một đơn vị
     */
    public BigDecimal getUnitCost() {
        return unitCost;
    }

    /**
     * Gán giá nhập một đơn vị.
     *
     * @param unitCost giá nhập một đơn vị
     */
    public void setUnitCost(BigDecimal unitCost) {
        this.unitCost = unitCost;
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
     * Thành tiền của dòng = số lượng x giá nhập.
     *
     * @return thành tiền
     */
    public BigDecimal getLineTotal() {
        return unitCost == null ? BigDecimal.ZERO : unitCost.multiply(BigDecimal.valueOf(quantity));
    }
}

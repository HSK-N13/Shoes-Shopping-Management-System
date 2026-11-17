package model;

import java.time.LocalDateTime;

/**
 * Một dòng lịch sử biến động tồn kho (BR03).
 */
public class StockMovement {
    /** Mã biến động. */
    private Long id;
    /** Mã biến thể. */
    private Long variantId;
    /** Loại biến động. */
    private MovementType type;
    /** Số lượng thay đổi (+ tăng, - giảm). */
    private int quantityChange;
    /** Loại chứng từ tham chiếu. */
    private String refType;
    /** Mã chứng từ tham chiếu. */
    private Long refId;
    /** Thời điểm phát sinh. */
    private LocalDateTime createdAt;
    /** SKU biến thể (chỉ để hiển thị). */
    private String sku;
    /** Tên biến thể (chỉ để hiển thị). */
    private String variantName;

    /**
     * Tạo đối tượng rỗng.
     */
    public StockMovement() {
    }

    /**
     * Lấy mã biến động.
     *
     * @return mã biến động
     */
    public Long getId() {
        return id;
    }

    /**
     * Gán mã biến động.
     *
     * @param id mã biến động
     */
    public void setId(Long id) {
        this.id = id;
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
     * Lấy loại biến động.
     *
     * @return loại biến động
     */
    public MovementType getType() {
        return type;
    }

    /**
     * Gán loại biến động.
     *
     * @param type loại biến động
     */
    public void setType(MovementType type) {
        this.type = type;
    }

    /**
     * Lấy số lượng thay đổi (+ tăng, - giảm).
     *
     * @return số lượng thay đổi (+ tăng, - giảm)
     */
    public int getQuantityChange() {
        return quantityChange;
    }

    /**
     * Gán số lượng thay đổi (+ tăng, - giảm).
     *
     * @param quantityChange số lượng thay đổi (+ tăng, - giảm)
     */
    public void setQuantityChange(int quantityChange) {
        this.quantityChange = quantityChange;
    }

    /**
     * Lấy loại chứng từ tham chiếu.
     *
     * @return loại chứng từ tham chiếu
     */
    public String getRefType() {
        return refType;
    }

    /**
     * Gán loại chứng từ tham chiếu.
     *
     * @param refType loại chứng từ tham chiếu
     */
    public void setRefType(String refType) {
        this.refType = refType;
    }

    /**
     * Lấy mã chứng từ tham chiếu.
     *
     * @return mã chứng từ tham chiếu
     */
    public Long getRefId() {
        return refId;
    }

    /**
     * Gán mã chứng từ tham chiếu.
     *
     * @param refId mã chứng từ tham chiếu
     */
    public void setRefId(Long refId) {
        this.refId = refId;
    }

    /**
     * Lấy thời điểm phát sinh.
     *
     * @return thời điểm phát sinh
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Gán thời điểm phát sinh.
     *
     * @param createdAt thời điểm phát sinh
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
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
}

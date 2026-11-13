package dto;

import java.math.BigDecimal;

/**
 * Một dòng thống kê sản phẩm bán chạy.
 */
public class ProductSalesRow {
    /** Mã SKU. */
    private String sku;
    /** Tên biến thể. */
    private String variantName;
    /** Số lượng đã bán. */
    private int quantitySold;
    /** Doanh thu của biến thể (sau giảm giá dòng). */
    private BigDecimal revenue;

    /**
     * Tạo đối tượng rỗng.
     */
    public ProductSalesRow() {
    }

    /**
     * Lấy mã SKU.
     *
     * @return mã SKU
     */
    public String getSku() {
        return sku;
    }

    /**
     * Gán mã SKU.
     *
     * @param sku mã SKU
     */
    public void setSku(String sku) {
        this.sku = sku;
    }

    /**
     * Lấy tên biến thể.
     *
     * @return tên biến thể
     */
    public String getVariantName() {
        return variantName;
    }

    /**
     * Gán tên biến thể.
     *
     * @param variantName tên biến thể
     */
    public void setVariantName(String variantName) {
        this.variantName = variantName;
    }

    /**
     * Lấy số lượng đã bán.
     *
     * @return số lượng đã bán
     */
    public int getQuantitySold() {
        return quantitySold;
    }

    /**
     * Gán số lượng đã bán.
     *
     * @param quantitySold số lượng đã bán
     */
    public void setQuantitySold(int quantitySold) {
        this.quantitySold = quantitySold;
    }

    /**
     * Lấy doanh thu của biến thể (sau giảm giá dòng).
     *
     * @return doanh thu của biến thể (sau giảm giá dòng)
     */
    public BigDecimal getRevenue() {
        return revenue;
    }

    /**
     * Gán doanh thu của biến thể (sau giảm giá dòng).
     *
     * @param revenue doanh thu của biến thể (sau giảm giá dòng)
     */
    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue;
    }
}

package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Phiếu nhập hàng; chỉ tăng tồn khi chuyển sang CONFIRMED (BR04).
 */
public class PurchaseReceipt {
    /** Mã phiếu nhập. */
    private Long id;
    /** Mã nhà cung cấp. */
    private Long supplierId;
    /** Mã người lập phiếu. */
    private Long userId;
    /** Thời điểm lập phiếu. */
    private LocalDateTime createdAt;
    /** Trạng thái phiếu. */
    private ReceiptStatus status;
    /** Tổng tiền nhập. */
    private BigDecimal totalAmount;
    /** Danh sách dòng chi tiết. */
    private List<PurchaseItem> items = new ArrayList<>();
    /** Tên nhà cung cấp (chỉ để hiển thị). */
    private String supplierName;
    /** Tên người lập (chỉ để hiển thị). */
    private String userName;

    /**
     * Tạo đối tượng rỗng.
     */
    public PurchaseReceipt() {
    }

    /**
     * Lấy mã phiếu nhập.
     *
     * @return mã phiếu nhập
     */
    public Long getId() {
        return id;
    }

    /**
     * Gán mã phiếu nhập.
     *
     * @param id mã phiếu nhập
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Lấy mã nhà cung cấp.
     *
     * @return mã nhà cung cấp
     */
    public Long getSupplierId() {
        return supplierId;
    }

    /**
     * Gán mã nhà cung cấp.
     *
     * @param supplierId mã nhà cung cấp
     */
    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    /**
     * Lấy mã người lập phiếu.
     *
     * @return mã người lập phiếu
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * Gán mã người lập phiếu.
     *
     * @param userId mã người lập phiếu
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * Lấy thời điểm lập phiếu.
     *
     * @return thời điểm lập phiếu
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Gán thời điểm lập phiếu.
     *
     * @param createdAt thời điểm lập phiếu
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Lấy trạng thái phiếu.
     *
     * @return trạng thái phiếu
     */
    public ReceiptStatus getStatus() {
        return status;
    }

    /**
     * Gán trạng thái phiếu.
     *
     * @param status trạng thái phiếu
     */
    public void setStatus(ReceiptStatus status) {
        this.status = status;
    }

    /**
     * Lấy tổng tiền nhập.
     *
     * @return tổng tiền nhập
     */
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    /**
     * Gán tổng tiền nhập.
     *
     * @param totalAmount tổng tiền nhập
     */
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    /**
     * Lấy danh sách dòng chi tiết.
     *
     * @return danh sách dòng chi tiết
     */
    public List<PurchaseItem> getItems() {
        return items;
    }

    /**
     * Gán danh sách dòng chi tiết.
     *
     * @param items danh sách dòng chi tiết
     */
    public void setItems(List<PurchaseItem> items) {
        this.items = items;
    }

    /**
     * Lấy tên nhà cung cấp (chỉ để hiển thị).
     *
     * @return tên nhà cung cấp (chỉ để hiển thị)
     */
    public String getSupplierName() {
        return supplierName;
    }

    /**
     * Gán tên nhà cung cấp (chỉ để hiển thị).
     *
     * @param supplierName tên nhà cung cấp (chỉ để hiển thị)
     */
    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    /**
     * Lấy tên người lập (chỉ để hiển thị).
     *
     * @return tên người lập (chỉ để hiển thị)
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Gán tên người lập (chỉ để hiển thị).
     *
     * @param userName tên người lập (chỉ để hiển thị)
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }
}

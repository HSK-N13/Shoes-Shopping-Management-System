package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Thanh toán của đơn; mỗi đơn đúng một thanh toán, số tiền bằng tổng tiền đơn (BR10).
 */
public class Payment {
    /** Mã thanh toán. */
    private Long id;
    /** Mã đơn. */
    private Long orderId;
    /** Số tiền. */
    private BigDecimal amount;
    /** Phương thức. */
    private PaymentMethod method;
    /** Thời điểm thanh toán. */
    private LocalDateTime paidAt;

    /**
     * Tạo đối tượng rỗng.
     */
    public Payment() {
    }

    /**
     * Lấy mã thanh toán.
     *
     * @return mã thanh toán
     */
    public Long getId() {
        return id;
    }

    /**
     * Gán mã thanh toán.
     *
     * @param id mã thanh toán
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
     * Lấy số tiền.
     *
     * @return số tiền
     */
    public BigDecimal getAmount() {
        return amount;
    }

    /**
     * Gán số tiền.
     *
     * @param amount số tiền
     */
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    /**
     * Lấy phương thức.
     *
     * @return phương thức
     */
    public PaymentMethod getMethod() {
        return method;
    }

    /**
     * Gán phương thức.
     *
     * @param method phương thức
     */
    public void setMethod(PaymentMethod method) {
        this.method = method;
    }

    /**
     * Lấy thời điểm thanh toán.
     *
     * @return thời điểm thanh toán
     */
    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    /**
     * Gán thời điểm thanh toán.
     *
     * @param paidAt thời điểm thanh toán
     */
    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }
}

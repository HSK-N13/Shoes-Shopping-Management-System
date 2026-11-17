package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Đơn bán hàng tại quầy. Tổng tiền luôn được tính lại từ các dòng (BR09).
 */
public class Order {
    /** Mã đơn. */
    private Long id;
    /** Mã khách hàng (có thể null). */
    private Long customerId;
    /** Mã nhân viên lập đơn. */
    private Long userId;
    /** Thời điểm tạo đơn. */
    private LocalDateTime createdAt;
    /** Trạng thái đơn. */
    private OrderStatus status;
    /** Giảm giá cấp đơn. */
    private BigDecimal discountAmount = BigDecimal.ZERO;
    /** Tổng tiền phải trả. */
    private BigDecimal totalAmount = BigDecimal.ZERO;
    /** Danh sách dòng hàng. */
    private List<OrderItem> items = new ArrayList<>();
    /** Tên khách hàng (chỉ để hiển thị). */
    private String customerName;
    /** Tên nhân viên (chỉ để hiển thị). */
    private String userName;
    /** Phương thức thanh toán (chỉ để hiển thị). */
    private PaymentMethod paymentMethod;

    /**
     * Tạo đối tượng rỗng.
     */
    public Order() {
    }

    /**
     * Lấy mã đơn.
     *
     * @return mã đơn
     */
    public Long getId() {
        return id;
    }

    /**
     * Gán mã đơn.
     *
     * @param id mã đơn
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Lấy mã khách hàng (có thể null).
     *
     * @return mã khách hàng (có thể null)
     */
    public Long getCustomerId() {
        return customerId;
    }

    /**
     * Gán mã khách hàng (có thể null).
     *
     * @param customerId mã khách hàng (có thể null)
     */
    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    /**
     * Lấy mã nhân viên lập đơn.
     *
     * @return mã nhân viên lập đơn
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * Gán mã nhân viên lập đơn.
     *
     * @param userId mã nhân viên lập đơn
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * Lấy thời điểm tạo đơn.
     *
     * @return thời điểm tạo đơn
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Gán thời điểm tạo đơn.
     *
     * @param createdAt thời điểm tạo đơn
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Lấy trạng thái đơn.
     *
     * @return trạng thái đơn
     */
    public OrderStatus getStatus() {
        return status;
    }

    /**
     * Gán trạng thái đơn.
     *
     * @param status trạng thái đơn
     */
    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    /**
     * Lấy giảm giá cấp đơn.
     *
     * @return giảm giá cấp đơn
     */
    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    /**
     * Gán giảm giá cấp đơn.
     *
     * @param discountAmount giảm giá cấp đơn
     */
    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    /**
     * Lấy tổng tiền phải trả.
     *
     * @return tổng tiền phải trả
     */
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    /**
     * Gán tổng tiền phải trả.
     *
     * @param totalAmount tổng tiền phải trả
     */
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    /**
     * Lấy danh sách dòng hàng.
     *
     * @return danh sách dòng hàng
     */
    public List<OrderItem> getItems() {
        return items;
    }

    /**
     * Gán danh sách dòng hàng.
     *
     * @param items danh sách dòng hàng
     */
    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    /**
     * Lấy tên khách hàng (chỉ để hiển thị).
     *
     * @return tên khách hàng (chỉ để hiển thị)
     */
    public String getCustomerName() {
        return customerName;
    }

    /**
     * Gán tên khách hàng (chỉ để hiển thị).
     *
     * @param customerName tên khách hàng (chỉ để hiển thị)
     */
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    /**
     * Lấy tên nhân viên (chỉ để hiển thị).
     *
     * @return tên nhân viên (chỉ để hiển thị)
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Gán tên nhân viên (chỉ để hiển thị).
     *
     * @param userName tên nhân viên (chỉ để hiển thị)
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Lấy phương thức thanh toán (chỉ để hiển thị).
     *
     * @return phương thức thanh toán (chỉ để hiển thị)
     */
    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    /**
     * Gán phương thức thanh toán (chỉ để hiển thị).
     *
     * @param paymentMethod phương thức thanh toán (chỉ để hiển thị)
     */
    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}

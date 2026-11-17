package model;

/**
 * Trạng thái đơn hàng: DRAFT -> PAID/CANCELLED, PAID -> CANCELLED (chỉ Admin).
 */
public enum OrderStatus {
    /** Nháp. */
    DRAFT("Nháp"),
    /** Đã thanh toán. */
    PAID("Đã thanh toán"),
    /** Đã hủy. */
    CANCELLED("Đã hủy");

    /** Nhãn tiếng Việt hiển thị trên giao diện. */
    private final String label;

    /**
     * Khởi tạo hằng enum với nhãn hiển thị.
     *
     * @param label nhãn tiếng Việt
     */
    OrderStatus(String label) {
        this.label = label;
    }

    /**
     * Lấy nhãn tiếng Việt.
     *
     * @return nhãn hiển thị
     */
    public String getLabel() {
        return label;
    }

    /**
     * Hiển thị nhãn tiếng Việt trong ComboBox.
     *
     * @return nhãn hiển thị
     */
    @Override
    public String toString() {
        return label;
    }
}

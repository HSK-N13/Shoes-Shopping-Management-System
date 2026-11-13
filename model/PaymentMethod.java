package model;

/**
 * Phương thức thanh toán (BR10).
 */
public enum PaymentMethod {
    /** Tiền mặt. */
    CASH("Tiền mặt"),
    /** Chuyển khoản. */
    TRANSFER("Chuyển khoản");

    /** Nhãn tiếng Việt hiển thị trên giao diện. */
    private final String label;

    /**
     * Khởi tạo hằng enum với nhãn hiển thị.
     *
     * @param label nhãn tiếng Việt
     */
    PaymentMethod(String label) {
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

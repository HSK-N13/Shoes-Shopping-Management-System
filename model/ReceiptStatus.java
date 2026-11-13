package model;

/**
 * Trạng thái phiếu nhập: DRAFT -> CONFIRMED/CANCELLED (BR04).
 */
public enum ReceiptStatus {
    /** Nháp. */
    DRAFT("Nháp"),
    /** Đã xác nhận. */
    CONFIRMED("Đã xác nhận"),
    /** Đã hủy. */
    CANCELLED("Đã hủy");

    /** Nhãn tiếng Việt hiển thị trên giao diện. */
    private final String label;

    /**
     * Khởi tạo hằng enum với nhãn hiển thị.
     *
     * @param label nhãn tiếng Việt
     */
    ReceiptStatus(String label) {
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

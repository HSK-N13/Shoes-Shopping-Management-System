package model;

/**
 * Loại biến động tồn kho (BR03).
 */
public enum MovementType {
    /** Nhập hàng. */
    PURCHASE("Nhập hàng"),
    /** Bán hàng. */
    SALE("Bán hàng"),
    /** Kiểm kê điều chỉnh. */
    ADJUSTMENT("Kiểm kê điều chỉnh"),
    /** Hủy đơn hoàn kho. */
    CANCEL("Hủy đơn hoàn kho");

    /** Nhãn tiếng Việt hiển thị trên giao diện. */
    private final String label;

    /**
     * Khởi tạo hằng enum với nhãn hiển thị.
     *
     * @param label nhãn tiếng Việt
     */
    MovementType(String label) {
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

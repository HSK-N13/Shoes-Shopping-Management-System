package utils;

/**
 * Ngoại lệ nghiệp vụ: vi phạm quy tắc BR01..BR12 hoặc dữ liệu nhập không hợp lệ.
 * Thông báo được viết bằng tiếng Việt để Controller hiển thị thẳng cho người dùng.
 */
public class BusinessException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * Tạo ngoại lệ nghiệp vụ với thông báo cho người dùng.
     *
     * @param message nội dung lỗi
     */
    public BusinessException(String message) {
        super(message);
    }

    /**
     * Tạo ngoại lệ nghiệp vụ bọc một lỗi gốc (thường là SQLException).
     *
     * @param message nội dung lỗi
     * @param cause   lỗi gốc
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}

package services;

import java.math.BigDecimal;
import java.sql.SQLException;

import utils.BusinessException;

/**
 * Lớp cha của các Service: chuyển SQLException thành BusinessException để
 * Controller chỉ cần bắt một loại lỗi, và cung cấp các hàm kiểm tra dữ liệu chung.
 */
public abstract class BaseService {

    /**
     * Một lời gọi DAO có thể ném SQLException.
     *
     * @param <T> kiểu kết quả
     */
    @FunctionalInterface
    protected interface SqlCall<T> {
        /**
         * Thực hiện lời gọi.
         *
         * @return kết quả
         * @throws SQLException lỗi database
         */
        T call() throws SQLException;
    }

    /**
     * Gọi DAO và bọc lỗi SQL thành lỗi nghiệp vụ có thông báo tiếng Việt.
     *
     * @param <T>  kiểu kết quả
     * @param call lời gọi DAO
     * @return kết quả của lời gọi
     * @throws BusinessException nếu database báo lỗi
     */
    protected <T> T db(SqlCall<T> call) {
        try {
            return call.call();
        } catch (SQLException e) {
            throw new BusinessException("Lỗi cơ sở dữ liệu: " + e.getMessage(), e);
        }
    }

    /**
     * Bắt buộc chuỗi không rỗng.
     *
     * @param value     giá trị cần kiểm tra
     * @param fieldName tên trường để báo lỗi
     * @return chuỗi đã cắt khoảng trắng hai đầu
     * @throws BusinessException nếu rỗng
     */
    protected String required(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new BusinessException(fieldName + " không được để trống.");
        }
        return value.trim();
    }

    /**
     * Chuẩn hóa chuỗi tùy chọn: cắt khoảng trắng, rỗng thì trả về null.
     *
     * @param value giá trị nhập
     * @return chuỗi đã chuẩn hóa hoặc null
     */
    protected String optional(String value) {
        return (value == null || value.trim().isEmpty()) ? null : value.trim();
    }

    /**
     * Kiểm tra số điện thoại (tùy chọn): chỉ gồm 9-11 chữ số.
     *
     * @param phone số điện thoại
     * @return số điện thoại đã chuẩn hóa hoặc null
     * @throws BusinessException nếu sai định dạng
     */
    protected String phone(String phone) {
        String p = optional(phone);
        if (p != null && !p.matches("\\d{9,11}")) {
            throw new BusinessException("Số điện thoại phải gồm 9-11 chữ số.");
        }
        return p;
    }

    /**
     * Bắt buộc số tiền không âm.
     *
     * @param value     số tiền
     * @param fieldName tên trường để báo lỗi
     * @return số tiền
     * @throws BusinessException nếu null hoặc âm
     */
    protected BigDecimal nonNegative(BigDecimal value, String fieldName) {
        if (value == null || value.signum() < 0) {
            throw new BusinessException(fieldName + " không được âm.");
        }
        return value;
    }

    /**
     * Đổi null thành 0.
     *
     * @param v số tiền có thể null
     * @return số tiền hoặc 0
     */
    protected static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}

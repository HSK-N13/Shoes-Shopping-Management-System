package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;

import connectDB.ConnectDB;

/**
 * Lớp cha của các DAO: cung cấp kết nối dùng chung và các hàm tiện ích
 * gán tham số có thể null, đọc cột có thể null.
 * <p>
 * Quy ước: DAO KHÔNG đóng Connection (vì dùng chung để chạy giao dịch),
 * chỉ đóng PreparedStatement/ResultSet. Lỗi SQL được ném ra cho Service xử lý.
 */
public abstract class BaseDAO {

    /**
     * Lấy kết nối dùng chung từ ConnectDB.
     *
     * @return kết nối JDBC
     * @throws SQLException nếu không kết nối được database
     */
    protected Connection conn() throws SQLException {
        return ConnectDB.getConnection();
    }

    /**
     * Gán tham số kiểu Long có thể null.
     *
     * @param ps    câu lệnh
     * @param index vị trí tham số
     * @param value giá trị (null sẽ gán NULL)
     * @throws SQLException lỗi JDBC
     */
    protected void setLong(PreparedStatement ps, int index, Long value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.BIGINT);
        } else {
            ps.setLong(index, value);
        }
    }

    /**
     * Gán tham số kiểu LocalDateTime có thể null.
     *
     * @param ps    câu lệnh
     * @param index vị trí tham số
     * @param value giá trị (null sẽ gán NULL)
     * @throws SQLException lỗi JDBC
     */
    protected void setTimestamp(PreparedStatement ps, int index, LocalDateTime value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.TIMESTAMP);
        } else {
            ps.setTimestamp(index, Timestamp.valueOf(value));
        }
    }

    /**
     * Đọc cột BIGINT có thể null.
     *
     * @param rs     kết quả truy vấn
     * @param column tên cột
     * @return giá trị hoặc null
     * @throws SQLException lỗi JDBC
     */
    protected Long getLong(ResultSet rs, String column) throws SQLException {
        long v = rs.getLong(column);
        return rs.wasNull() ? null : v;
    }

    /**
     * Đọc cột DATETIME có thể null.
     *
     * @param rs     kết quả truy vấn
     * @param column tên cột
     * @return giá trị hoặc null
     * @throws SQLException lỗi JDBC
     */
    protected LocalDateTime getDateTime(ResultSet rs, String column) throws SQLException {
        Timestamp t = rs.getTimestamp(column);
        return t == null ? null : t.toLocalDateTime();
    }

    /**
     * Chuyển từ khóa tìm kiếm thành mẫu LIKE, null nếu rỗng.
     *
     * @param keyword từ khóa người dùng nhập
     * @return chuỗi "%từ khóa%" hoặc null
     */
    protected String like(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return null;
        }
        return "%" + keyword.trim() + "%";
    }

    /**
     * Thực hiện câu lệnh INSERT và trả về khóa tự tăng vừa sinh.
     *
     * @param ps câu lệnh đã tạo với RETURN_GENERATED_KEYS
     * @return id vừa sinh
     * @throws SQLException nếu không lấy được khóa
     */
    protected long executeInsert(PreparedStatement ps) throws SQLException {
        ps.executeUpdate();
        try (ResultSet rs = ps.getGeneratedKeys()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        }
        throw new SQLException("Không lấy được mã vừa thêm.");
    }
}

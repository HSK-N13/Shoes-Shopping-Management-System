package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import model.Payment;
import model.PaymentMethod;

/**
 * Truy cập bảng payments (thanh toán của đơn, BR10).
 */
public class PaymentDAO extends BaseDAO {

    /**
     * Ghi nhận thanh toán cho đơn.
     *
     * @param p thanh toán cần lưu
     * @return chính thanh toán đã có id
     * @throws SQLException lỗi database
     */
    public Payment insert(Payment p) throws SQLException {
        String sql = "INSERT INTO payments (order_id, amount, method, paid_at) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, p.getOrderId());
            ps.setBigDecimal(2, p.getAmount());
            ps.setString(3, p.getMethod().name());
            ps.setTimestamp(4, Timestamp.valueOf(p.getPaidAt() != null ? p.getPaidAt() : LocalDateTime.now()));
            p.setId(executeInsert(ps));
        }
        return p;
    }

    /**
     * Kiểm tra đơn đã có thanh toán chưa (mỗi đơn chỉ một thanh toán, BR10).
     *
     * @param orderId mã đơn
     * @return true nếu đã có
     * @throws SQLException lỗi database
     */
    public boolean existsForOrder(Long orderId) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement("SELECT COUNT(*) FROM payments WHERE order_id = ?")) {
            ps.setLong(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Lấy thanh toán của một đơn.
     *
     * @param orderId mã đơn
     * @return thanh toán hoặc null nếu chưa thanh toán
     * @throws SQLException lỗi database
     */
    public Payment findByOrderId(Long orderId) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement("SELECT * FROM payments WHERE order_id = ?")) {
            ps.setLong(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Payment p = new Payment();
                p.setId(rs.getLong("id"));
                p.setOrderId(rs.getLong("order_id"));
                p.setAmount(rs.getBigDecimal("amount"));
                p.setMethod(PaymentMethod.valueOf(rs.getString("method")));
                p.setPaidAt(getDateTime(rs, "paid_at"));
                return p;
            }
        }
    }
}

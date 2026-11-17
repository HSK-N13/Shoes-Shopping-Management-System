package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;

import model.Order;
import model.OrderStatus;
import model.PaymentMethod;

/**
 * Truy cập bảng orders (đơn bán hàng).
 */
public class OrderDAO extends BaseDAO {

    private static final String SELECT_BASE = "SELECT o.*, c.full_name AS customer_name, "
            + "u.full_name AS user_name, pm.method AS payment_method "
            + "FROM orders o "
            + "LEFT JOIN customers c ON o.customer_id = c.id "
            + "JOIN users u ON o.user_id = u.id "
            + "LEFT JOIN payments pm ON pm.order_id = o.id ";

    /**
     * Thêm đơn mới (thường ở trạng thái DRAFT) và gán id vừa sinh.
     *
     * @param order đơn cần thêm
     * @return chính đơn đã có id
     * @throws SQLException lỗi database
     */
    public Order insert(Order order) throws SQLException {
        String sql = "INSERT INTO orders (customer_id, user_id, created_at, status, discount_amount, total_amount) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setLong(ps, 1, order.getCustomerId());
            ps.setLong(2, order.getUserId());
            ps.setTimestamp(3, Timestamp.valueOf(
                    order.getCreatedAt() != null ? order.getCreatedAt() : LocalDateTime.now()));
            ps.setString(4, order.getStatus().name());
            ps.setBigDecimal(5, order.getDiscountAmount());
            ps.setBigDecimal(6, order.getTotalAmount());
            order.setId(executeInsert(ps));
        }
        return order;
    }

    /**
     * Cập nhật khách hàng, giảm giá và tổng tiền của đơn còn ở trạng thái DRAFT.
     *
     * @param order đơn với dữ liệu mới
     * @return true nếu cập nhật được (false nếu đơn không còn là DRAFT)
     * @throws SQLException lỗi database
     */
    public boolean updateDraft(Order order) throws SQLException {
        String sql = "UPDATE orders SET customer_id = ?, discount_amount = ?, total_amount = ? "
                + "WHERE id = ? AND status = 'DRAFT'";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            setLong(ps, 1, order.getCustomerId());
            ps.setBigDecimal(2, order.getDiscountAmount());
            ps.setBigDecimal(3, order.getTotalAmount());
            ps.setLong(4, order.getId());
            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Chuyển trạng thái đơn có điều kiện: chỉ đổi khi trạng thái hiện tại đúng bằng
     * {@code from}, tránh hai máy cùng xác nhận/hủy một đơn.
     *
     * @param id   mã đơn
     * @param from trạng thái hiện tại bắt buộc
     * @param to   trạng thái mới
     * @return true nếu đã chuyển
     * @throws SQLException lỗi database
     */
    public boolean changeStatus(Long id, OrderStatus from, OrderStatus to) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement("UPDATE orders SET status = ? WHERE id = ? AND status = ?")) {
            ps.setString(1, to.name());
            ps.setLong(2, id);
            ps.setString(3, from.name());
            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Tìm đơn theo mã (không kèm dòng hàng).
     *
     * @param id mã đơn
     * @return đơn hàng hoặc null
     * @throws SQLException lỗi database
     */
    public Order findById(Long id) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement(SELECT_BASE + "WHERE o.id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    /**
     * Tìm đơn theo khoảng thời gian, trạng thái và từ khóa (mã đơn, tên/SĐT khách).
     *
     * @param from    từ thời điểm (bao gồm), null = không giới hạn
     * @param to      đến thời điểm (không bao gồm), null = không giới hạn
     * @param status  trạng thái, null = tất cả
     * @param keyword từ khóa, null = không lọc
     * @return danh sách đơn, mới nhất trước
     * @throws SQLException lỗi database
     */
    public ArrayList<Order> search(LocalDateTime from, LocalDateTime to, OrderStatus status, String keyword)
            throws SQLException {
        String sql = SELECT_BASE
                + "WHERE (? IS NULL OR o.created_at >= ?) AND (? IS NULL OR o.created_at < ?) "
                + "AND (? IS NULL OR o.status = ?) "
                + "AND (? IS NULL OR CAST(o.id AS VARCHAR(20)) = ? OR c.full_name LIKE ? OR c.phone LIKE ?) "
                + "ORDER BY o.created_at DESC, o.id DESC";
        ArrayList<Order> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            setTimestamp(ps, 1, from);
            setTimestamp(ps, 2, from);
            setTimestamp(ps, 3, to);
            setTimestamp(ps, 4, to);
            String st = status == null ? null : status.name();
            ps.setString(5, st);
            ps.setString(6, st);
            String kw = (keyword == null || keyword.trim().isEmpty()) ? null : keyword.trim();
            ps.setString(7, kw);
            ps.setString(8, kw);
            ps.setString(9, like(kw));
            ps.setString(10, like(kw));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    /**
     * Chuyển một dòng kết quả thành đối tượng Order.
     *
     * @param rs kết quả truy vấn đang trỏ tới dòng cần đọc
     * @return đơn hàng
     * @throws SQLException lỗi đọc cột
     */
    private Order map(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setId(rs.getLong("id"));
        o.setCustomerId(getLong(rs, "customer_id"));
        o.setUserId(rs.getLong("user_id"));
        o.setCreatedAt(getDateTime(rs, "created_at"));
        o.setStatus(OrderStatus.valueOf(rs.getString("status")));
        o.setDiscountAmount(rs.getBigDecimal("discount_amount"));
        o.setTotalAmount(rs.getBigDecimal("total_amount"));
        o.setCustomerName(rs.getString("customer_name"));
        o.setUserName(rs.getString("user_name"));
        String method = rs.getString("payment_method");
        o.setPaymentMethod(method == null ? null : PaymentMethod.valueOf(method));
        return o;
    }
}

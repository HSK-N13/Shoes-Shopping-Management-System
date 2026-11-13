package dao;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import model.OrderItem;

/**
 * Truy cập bảng order_items (dòng hàng của đơn).
 */
public class OrderItemDAO extends BaseDAO {

    /**
     * Lưu danh sách dòng hàng cho một đơn.
     *
     * @param orderId mã đơn
     * @param items   các dòng hàng
     * @throws SQLException lỗi database
     */
    public void insertAll(Long orderId, List<OrderItem> items) throws SQLException {
        String sql = "INSERT INTO order_items (order_id, variant_id, quantity, unit_price, unit_cost, discount_amount) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (OrderItem it : items) {
                it.setOrderId(orderId);
                ps.setLong(1, orderId);
                ps.setLong(2, it.getVariantId());
                ps.setInt(3, it.getQuantity());
                ps.setBigDecimal(4, it.getUnitPrice());
                ps.setBigDecimal(5, it.getUnitCost() == null ? BigDecimal.ZERO : it.getUnitCost());
                ps.setBigDecimal(6, it.getDiscountAmount() == null ? BigDecimal.ZERO : it.getDiscountAmount());
                it.setId(executeInsert(ps));
            }
        }
    }

    /**
     * Xóa toàn bộ dòng hàng của một đơn (dùng khi lưu lại đơn nháp).
     *
     * @param orderId mã đơn
     * @throws SQLException lỗi database
     */
    public void deleteByOrderId(Long orderId) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement("DELETE FROM order_items WHERE order_id = ?")) {
            ps.setLong(1, orderId);
            ps.executeUpdate();
        }
    }

    /**
     * Chụp lại giá bán và giá vốn vào dòng hàng tại thời điểm bán (BR06).
     *
     * @param itemId    mã dòng hàng
     * @param unitPrice giá bán tại thời điểm xác nhận
     * @param unitCost  giá vốn tại thời điểm xác nhận
     * @throws SQLException lỗi database
     */
    public void updateSnapshot(Long itemId, BigDecimal unitPrice, BigDecimal unitCost) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement(
                "UPDATE order_items SET unit_price = ?, unit_cost = ? WHERE id = ?")) {
            ps.setBigDecimal(1, unitPrice);
            ps.setBigDecimal(2, unitCost);
            ps.setLong(3, itemId);
            ps.executeUpdate();
        }
    }

    /**
     * Lấy các dòng hàng của một đơn kèm SKU và tên biến thể.
     *
     * @param orderId mã đơn
     * @return danh sách dòng hàng
     * @throws SQLException lỗi database
     */
    public ArrayList<OrderItem> findByOrderId(Long orderId) throws SQLException {
        String sql = "SELECT oi.*, v.sku, p.name AS product_name, v.size, v.color "
                + "FROM order_items oi "
                + "JOIN product_variants v ON oi.variant_id = v.id "
                + "JOIN products p ON v.product_id = p.id "
                + "WHERE oi.order_id = ? ORDER BY oi.id";
        ArrayList<OrderItem> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderItem it = new OrderItem();
                    it.setId(rs.getLong("id"));
                    it.setOrderId(rs.getLong("order_id"));
                    it.setVariantId(rs.getLong("variant_id"));
                    it.setQuantity(rs.getInt("quantity"));
                    it.setUnitPrice(rs.getBigDecimal("unit_price"));
                    it.setUnitCost(rs.getBigDecimal("unit_cost"));
                    BigDecimal d = rs.getBigDecimal("discount_amount");
                    it.setDiscountAmount(d == null ? BigDecimal.ZERO : d);
                    it.setSku(rs.getString("sku"));
                    it.setVariantName(rs.getString("product_name") + " - Size " + rs.getString("size")
                            + " - " + rs.getString("color"));
                    list.add(it);
                }
            }
        }
        return list;
    }
}

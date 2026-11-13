package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import model.MovementType;
import model.StockMovement;

/**
 * Truy cập bảng stock_movements (lịch sử biến động tồn kho, BR03).
 */
public class StockMovementDAO extends BaseDAO {

    private static final String SELECT_BASE = "SELECT sm.*, v.sku, p.name AS product_name, v.size, v.color "
            + "FROM stock_movements sm "
            + "JOIN product_variants v ON sm.variant_id = v.id "
            + "JOIN products p ON v.product_id = p.id ";

    /**
     * Ghi nhận một dòng biến động kho mới.
     *
     * @param movement biến động cần lưu
     * @return chính biến động đã có id
     * @throws SQLException lỗi database
     */
    public StockMovement save(StockMovement movement) throws SQLException {
        String sql = "INSERT INTO stock_movements (variant_id, type, quantity_change, ref_type, ref_id, created_at) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, movement.getVariantId());
            ps.setString(2, movement.getType().name());
            ps.setInt(3, movement.getQuantityChange());
            ps.setString(4, movement.getRefType());
            setLong(ps, 5, movement.getRefId());
            ps.setTimestamp(6, Timestamp.valueOf(
                    movement.getCreatedAt() != null ? movement.getCreatedAt() : LocalDateTime.now()));
            movement.setId(executeInsert(ps));
        }
        return movement;
    }

    /**
     * Tạo nhanh và lưu một dòng biến động kho.
     *
     * @param variantId mã biến thể
     * @param type      loại biến động
     * @param change    số lượng thay đổi (+/-)
     * @param refType   loại chứng từ (ORDER, PURCHASE_RECEIPT, STOCKTAKE)
     * @param refId     mã chứng từ (có thể null)
     * @return biến động đã lưu
     * @throws SQLException lỗi database
     */
    public StockMovement record(Long variantId, MovementType type, int change, String refType, Long refId)
            throws SQLException {
        StockMovement m = new StockMovement();
        m.setVariantId(variantId);
        m.setType(type);
        m.setQuantityChange(change);
        m.setRefType(refType);
        m.setRefId(refId);
        m.setCreatedAt(LocalDateTime.now());
        return save(m);
    }

    /**
     * Lấy lịch sử biến động của một biến thể, mới nhất trước.
     *
     * @param variantId mã biến thể
     * @return danh sách biến động
     * @throws SQLException lỗi database
     */
    public List<StockMovement> findByVariantId(Long variantId) throws SQLException {
        return search(variantId, null, null, null);
    }

    /**
     * Lấy lịch sử biến động theo loại, mới nhất trước.
     *
     * @param type loại biến động
     * @return danh sách biến động
     * @throws SQLException lỗi database
     */
    public ArrayList<StockMovement> findByType(MovementType type) throws SQLException {
        return search(null, type, null, null);
    }

    /**
     * Tìm biến động kho theo biến thể, loại và khoảng thời gian (tham số null = không lọc).
     *
     * @param variantId mã biến thể
     * @param type      loại biến động
     * @param from      từ thời điểm (bao gồm)
     * @param to        đến thời điểm (không bao gồm)
     * @return danh sách biến động, mới nhất trước
     * @throws SQLException lỗi database
     */
    public ArrayList<StockMovement> search(Long variantId, MovementType type, LocalDateTime from, LocalDateTime to)
            throws SQLException {
        String sql = SELECT_BASE
                + "WHERE (? IS NULL OR sm.variant_id = ?) AND (? IS NULL OR sm.type = ?) "
                + "AND (? IS NULL OR sm.created_at >= ?) AND (? IS NULL OR sm.created_at < ?) "
                + "ORDER BY sm.created_at DESC, sm.id DESC";
        ArrayList<StockMovement> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            setLong(ps, 1, variantId);
            setLong(ps, 2, variantId);
            String t = type == null ? null : type.name();
            ps.setString(3, t);
            ps.setString(4, t);
            setTimestamp(ps, 5, from);
            setTimestamp(ps, 6, from);
            setTimestamp(ps, 7, to);
            setTimestamp(ps, 8, to);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToStockMovement(rs));
                }
            }
        }
        return list;
    }

    /**
     * Đối soát: tìm các biến thể có tồn kho hiện tại khác tổng quantity_change
     * trong lịch sử (mục 5 của đặc tả).
     *
     * @return map SKU -> mảng [tồn hiện tại, tổng biến động]
     * @throws SQLException lỗi database
     */
    public Map<String, int[]> findMismatches() throws SQLException {
        String sql = "SELECT v.sku, ISNULL(v.stock_quantity,0) AS stock, ISNULL(SUM(sm.quantity_change),0) AS total "
                + "FROM product_variants v LEFT JOIN stock_movements sm ON sm.variant_id = v.id "
                + "GROUP BY v.id, v.sku, v.stock_quantity "
                + "HAVING ISNULL(v.stock_quantity,0) <> ISNULL(SUM(sm.quantity_change),0) ORDER BY v.sku";
        Map<String, int[]> map = new LinkedHashMap<>();
        try (PreparedStatement ps = conn().prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString("sku"), new int[] { rs.getInt("stock"), rs.getInt("total") });
            }
        }
        return map;
    }

    /**
     * Chuyển một dòng kết quả thành đối tượng StockMovement.
     *
     * @param rs kết quả truy vấn đang trỏ tới dòng cần đọc
     * @return biến động kho
     * @throws SQLException lỗi đọc cột
     */
    private StockMovement mapResultSetToStockMovement(ResultSet rs) throws SQLException {
        StockMovement movement = new StockMovement();
        movement.setId(rs.getLong("id"));
        movement.setVariantId(rs.getLong("variant_id"));
        movement.setType(MovementType.valueOf(rs.getString("type")));
        movement.setQuantityChange(rs.getInt("quantity_change"));
        movement.setRefType(rs.getString("ref_type"));
        movement.setRefId(getLong(rs, "ref_id"));
        movement.setCreatedAt(getDateTime(rs, "created_at"));
        movement.setSku(rs.getString("sku"));
        movement.setVariantName(rs.getString("product_name") + " - Size " + rs.getString("size")
                + " - " + rs.getString("color"));
        return movement;
    }
}

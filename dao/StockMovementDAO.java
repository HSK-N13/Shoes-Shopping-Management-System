package dao;

import model.StockMovement;
import model.MovementType;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import connectDB.ConnectDB;

public class StockMovementDAO {

    /**
     * Ghi nhận lịch sử biến động kho mới vào CSDL
     */
    public StockMovement save(StockMovement movement) throws SQLException {
        ConnectDB.getInstance().connect();
        Connection conn = ConnectDB.getConnection();
        String sql = "INSERT INTO stock_movement (variant_id, type, quantity_change, ref_type, ref_id, created_at) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, movement.getVariantId());
            ps.setString(2, movement.getType().name());
            ps.setInt(3, movement.getQuantityChange());
            ps.setString(4, movement.getRefType());

            if (movement.getRefId() != null) {
                ps.setLong(5, movement.getRefId());
            } else {
                ps.setNull(5, Types.BIGINT);
            }

            ps.setTimestamp(6, Timestamp.valueOf(movement.getCreatedAt() != null ? movement.getCreatedAt() : LocalDateTime.now()));

            ps.executeUpdate();

           
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    movement.setId(rs.getLong(1));
                }
            }
        }
        return movement;
    }

    /**
     * Tìm kiếm lịch sử biến động kho theo variantId
     */
    public List<StockMovement> findByVariantId(Connection conn, Long variantId) throws SQLException {
        List<StockMovement> list = new ArrayList<>();
        String sql = "SELECT * FROM stock_movement WHERE variant_id = ? ORDER BY created_at DESC";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, variantId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    StockMovement movement = mapResultSetToStockMovement(rs);
                    list.add(movement);
                }
            }
        }
        return list;
    }

    /**
     * Helper map dữ liệu từ ResultSet sang Object StockMovement
     */
    private StockMovement mapResultSetToStockMovement(ResultSet rs) throws SQLException {
        StockMovement movement = new StockMovement();
        movement.setId(rs.getLong("id"));
        movement.setVariantId(rs.getLong("variant_id"));
        movement.setType(MovementType.valueOf(rs.getString("type")));
        movement.setQuantityChange(rs.getInt("quantity_change"));
        movement.setRefType(rs.getString("ref_type"));
        
        long refId = rs.getLong("ref_id");
        if (!rs.wasNull()) {
            movement.setRefId(refId);
        }

        Timestamp timestamp = rs.getTimestamp("created_at");
        if (timestamp != null) {
            movement.setCreatedAt(timestamp.toLocalDateTime());
        }

        return movement;
    }
    
    public ArrayList<StockMovement> findByType(MovementType type) throws SQLException {
        ArrayList<StockMovement> list = new ArrayList<>();
        ConnectDB.getInstance().connect();
        
        String sql = "SELECT * FROM stock_movement WHERE type = ? ORDER BY created_at DESC";

        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, type.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    StockMovement movement = mapResultSetToStockMovement(rs);
                    list.add(movement);
                }
            }
        }
        return list;
    }
}
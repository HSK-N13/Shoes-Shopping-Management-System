package dao;

import model.PurchaseItem;

import java.sql.*;
import java.util.List;

import connectDB.ConnectDB;

public class PurchaseItemDAO {

    /**
     * Lưu một chi tiết mặt hàng nhập kho
     */
    public PurchaseItem save(PurchaseItem item){
        ConnectDB.getInstance().connect();
        Connection conn = ConnectDB.getConnection();
        String sql = "INSERT INTO purchase_item (receipt_id, variant_id, quantity, unit_cost) VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, item.getReceiptId());
            ps.setLong(2, item.getVariantId());
            ps.setInt(3, item.getQuantity());
            ps.setBigDecimal(4, item.getUnitCost());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    item.setId(rs.getLong(1));
                }
            }
        } catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return item;
    }

//    /**
//     * Lưu danh sách nhiều mặt hàng chi tiết cùng lúc (dùng Batch để tối ưu hiệu năng)
//     */
//    public void saveAll(Connection conn, List<PurchaseItem> items) throws SQLException {
//        String sql = "INSERT INTO purchase_item (receipt_id, variant_id, quantity, unit_cost) VALUES (?, ?, ?, ?)";
//
//        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
//            for (PurchaseItem item : items) {
//                ps.setLong(1, item.getReceiptId());
//                ps.setLong(2, item.getVariantId());
//                ps.setInt(3, item.getQuantity());
//                ps.setBigDecimal(4, item.getUnitCost());
//                ps.addBatch();
//            }
//            ps.executeBatch();
//        }
//    }
}
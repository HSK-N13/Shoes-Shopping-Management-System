package dao;

import java.sql.*;

import connectDB.ConnectDB;

public class ProductVariantDAO {

    /**
     * Lấy số lượng tồn kho hiện tại của một biến thể sản phẩm theo ID
     */
    public int findStockQuantityById(Long variantId){
        ConnectDB.getInstance().connect();
        Connection conn = ConnectDB.getConnection();
        String sql = "SELECT stock_quantity FROM product_variant WHERE id = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, variantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("stock_quantity");
                }
            }
        } catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return 0;
    }

    /**
     * Cập nhật số lượng tồn kho mới cho biến thể sản phẩm
     */
    public void updateStock(Long variantId, int newQuantity) throws SQLException {
        ConnectDB.getInstance().connect();
        Connection conn = ConnectDB.getConnection();
        String sql = "UPDATE product_variant SET stock_quantity = ? WHERE id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setInt(1, newQuantity);
            ps.setLong(2, variantId);
            ps.executeUpdate();
        }
    }
}
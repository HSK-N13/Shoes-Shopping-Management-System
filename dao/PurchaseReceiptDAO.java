package dao;

import connectDB.ConnectDB;
import model.PurchaseReceipt;

import java.sql.*;
import java.time.LocalDateTime;

public class PurchaseReceiptDAO {

    /**
     * Lưu phiếu nhập kho trực tiếp bằng Connection từ connectDB
     */
    public PurchaseReceipt save(PurchaseReceipt receipt) {
	    	try {
	        ConnectDB.getInstance().connect();
	        Connection conn = ConnectDB.getConnection();
	        String sql = "INSERT INTO purchase_receipt (supplier_id, user_id, status, total_amount, cre	ated_at) VALUES (?, ?, ?, ?, ?)";
	        PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
	            ps.setLong(1, receipt.getSupplierId());
	            ps.setLong(2, receipt.getUserId());
	            ps.setString(3, receipt.getStatus().name());
	            ps.setBigDecimal(4, receipt.getTotalAmount());
	            ps.setTimestamp(5, Timestamp.valueOf(receipt.getCreatedAt() != null ? receipt.getCreatedAt() : LocalDateTime.now()));
	
	            ps.executeUpdate();
	            try (ResultSet rs = ps.getGeneratedKeys()) {
	                if (rs.next()) {
	                    receipt.setId(rs.getLong(1));
	                }
	            }
	    	}catch (SQLException e) {
				e.printStackTrace();
			}
	        return receipt;
	    }
}
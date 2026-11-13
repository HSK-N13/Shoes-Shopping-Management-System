package dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import connectDB.connectDB;
import model.ProductVariant;

public class ProductVariantDAO {
	public ArrayList<ProductVariant> getVariantsByProductId(Long productId){
		return getVariantsByProductId(productId, null);
	}
	public ArrayList<ProductVariant> getVariantsByProductId(
            Long productId, Boolean active) {

        ArrayList<ProductVariant> list = new ArrayList<>();

        String sql = "SELECT * FROM product_variants "
                + "WHERE product_id = ? "
                + "AND (? IS NULL OR active = ?) "
                + "ORDER BY id";

        try {
            Connection con = connectDB.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setLong(1, productId);

            if (active == null) {
                ps.setNull(2, java.sql.Types.BIT);
                ps.setNull(3, java.sql.Types.BIT);
            } else {
                ps.setBoolean(2, active);
                ps.setBoolean(3, active);
            }

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(mapVariant(rs));
            }

            rs.close();
            ps.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }
	public ProductVariant getVariantById(Long id) {
        String sql = "SELECT * FROM product_variants WHERE id = ?";

        try {
            Connection con = connectDB.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setLong(1, id);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                ProductVariant pv = mapVariant(rs);

                rs.close();
                ps.close();

                return pv;
            }

            rs.close();
            ps.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
	 public ProductVariant getVariantBySku(String sku) {
	        String sql = "SELECT * FROM product_variants WHERE sku = ?";

	        try {
	            Connection con = connectDB.getConnection();
	            PreparedStatement ps = con.prepareStatement(sql);
	            ps.setString(1, sku);

	            ResultSet rs = ps.executeQuery();

	            if (rs.next()) {
	                ProductVariant pv = mapVariant(rs);

	                rs.close();
	                ps.close();

	                return pv;
	            }

	            rs.close();
	            ps.close();

	        } catch (SQLException e) {
	            e.printStackTrace();
	        }

	        return null;
	    }
	 
	 public boolean insertVariant(ProductVariant pv) {
	        if (!validVariant(pv)) {
	            return false;
	        }

	        String sql = "INSERT INTO product_variants "
	                + "(product_id, sku, size, color, selling_price, "
	                + "cost_price, stock_quantity, min_stock, active) "
	                + "VALUES (?, ?, ?, ?, ?, ?, 0, ?, ?)";

	        try {
	            Connection con = connectDB.getConnection();
	            PreparedStatement ps = con.prepareStatement(sql);

	            ps.setLong(1, pv.getProductId());
	            ps.setString(2, pv.getSku());
	            ps.setString(3, pv.getSize());
	            ps.setString(4, pv.getColor());
	            ps.setBigDecimal(5, pv.getSellingPrice());
	            ps.setBigDecimal(6, pv.getCostPrice());
	            ps.setInt(7, pv.getMinStock());
	            ps.setBoolean(8, pv.isActive());

	            int result = ps.executeUpdate();
	            ps.close();

	            return result > 0;

	        } catch (SQLException e) {
	            e.printStackTrace();
	        }

	        return false;
	    }
	 public boolean updateVariant(ProductVariant pv) {
	        if (!validVariant(pv)) {
	            return false;
	        }

	        String sql = "UPDATE product_variants SET "
	                + "sku = ?, size = ?, color = ?, "
	                + "selling_price = ?, cost_price = ?, "
	                + "min_stock = ?, active = ? "
	                + "WHERE id = ?";

	        try {
	            Connection con = connectDB.getConnection();
	            PreparedStatement ps = con.prepareStatement(sql);

	            ps.setString(1, pv.getSku());
	            ps.setString(2, pv.getSize());
	            ps.setString(3, pv.getColor());
	            ps.setBigDecimal(4, pv.getSellingPrice());
	            ps.setBigDecimal(5, pv.getCostPrice());
	            ps.setInt(6, pv.getMinStock());
	            ps.setBoolean(7, pv.isActive());
	            ps.setLong(8, pv.getId());

	            int result = ps.executeUpdate();
	            ps.close();

	            return result > 0;

	        } catch (SQLException e) {
	            e.printStackTrace();
	        }

	        return false;
	    }
	 public boolean updateVariantStatus(Long id, boolean active) {
	        String sql = "UPDATE product_variants SET active = ? WHERE id = ?";

	        try {
	            Connection con = connectDB.getConnection();
	            PreparedStatement ps = con.prepareStatement(sql);

	            ps.setBoolean(1, active);
	            ps.setLong(2, id);

	            int result = ps.executeUpdate();
	            ps.close();

	            return result > 0;

	        } catch (SQLException e) {
	            e.printStackTrace();
	        }

	        return false;
	    }

	 
	private ProductVariant mapVariant(ResultSet rs) throws SQLException{
		// TODO Auto-generated method stub
		ProductVariant pv = new ProductVariant();

        pv.setId(rs.getLong("id"));
        pv.setProductId(rs.getLong("product_id"));
        pv.setSku(rs.getString("sku"));
        pv.setSize(rs.getString("size"));
        pv.setColor(rs.getString("color"));
        pv.setSellingPrice(rs.getBigDecimal("selling_price"));
        pv.setCostPrice(rs.getBigDecimal("cost_price"));
        pv.setStockQuantity(rs.getInt("stock_quantity"));
        pv.setMinStock(rs.getInt("min_stock"));
        pv.setActive(rs.getBoolean("active"));

        return pv;
	}
	private boolean validVariant(ProductVariant pv) {
        if (pv == null) {
            return false;
        }

        if (pv.getProductId() == null
                || pv.getSku() == null || pv.getSku().trim().isEmpty()
                || pv.getSize() == null || pv.getSize().trim().isEmpty()
                || pv.getColor() == null || pv.getColor().trim().isEmpty()
                || pv.getSellingPrice() == null
                || pv.getCostPrice() == null) {
            return false;
        }

        if (pv.getSellingPrice().compareTo(BigDecimal.ZERO) < 0
                || pv.getCostPrice().compareTo(BigDecimal.ZERO) < 0
                || pv.getMinStock() < 0) {
            return false;
        }

        return true;
    }
}

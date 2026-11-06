package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import connectDB.connectDB;
import model.Product;

public class ProductDAO {
	public ArrayList<Product> getAllProduct(){
		return searchProducts(null, null, null, null);
	}

	private ArrayList<Product> searchProducts(String keyword, Long categoryId, Long brandId, Boolean active) {
		// TODO Auto-generated method stub
		ArrayList<Product> list = new ArrayList<>();
        String sql = "SELECT DISTINCT p.* "
                + "FROM products p "
                + "LEFT JOIN brands b ON p.brand_id = b.id "
                + "LEFT JOIN product_variants pv ON p.id = pv.product_id "
                + "WHERE (? IS NULL OR p.name LIKE ?) "
                + "AND (? IS NULL OR p.category_id = ?) "
                + "AND (? IS NULL OR p.brand_id = ?) "
                + "AND (? IS NULL OR p.active = ?) "
                + "AND (? IS NULL OR b.name LIKE ? "
                + "OR pv.sku LIKE ? OR pv.size LIKE ? OR pv.color LIKE ?)";
        try {
        	Connection con = connectDB.getConnection();
        	PreparedStatement ps = con.prepareStatement(sql);
        	
        	String kw = (keyword == null || keyword.trim().isEmpty()) ? null : keyword.trim();
        	ps.setString(1, kw);
        	ps.setString(2, kw == null ? null : "%" +  kw + "%");
        	
        	if(categoryId == null) {
        		ps.setNull(3, java.sql.Types.BIGINT);
        		ps.setNull(4, java.sql.Types.BIGINT);
        	} else {
        		ps.setLong(3, categoryId);
        		ps.setLong(4, categoryId);
        	}
        	if (brandId == null) {
                ps.setNull(5, java.sql.Types.BIGINT);
                ps.setNull(6, java.sql.Types.BIGINT);
            } else {
                ps.setLong(5, brandId);
                ps.setLong(6, brandId);
            }

            if (active == null) {
                ps.setNull(7, java.sql.Types.BIT);
                ps.setNull(8, java.sql.Types.BIT);
            } else {
                ps.setBoolean(7, active);
                ps.setBoolean(8, active);
            }
            ps.setString(9, kw);
            ps.setString(10, kw == null ? null : "%" + kw + "%");
            ps.setString(11, kw == null ? null : "%" + kw + "%");
            ps.setString(12, kw == null ? null : "%" + kw + "%");
            ps.setString(13, kw == null ? null : "%" + kw + "%");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(mapProduct(rs));
            }

            rs.close();
            ps.close();
        }catch(SQLException e) {
        	e.printStackTrace();
        }
		return list;
	}
	 public Product getProductById(Long id) {
	        String sql = "SELECT * FROM products WHERE id = ?";

	        try {
	            Connection con = connectDB.getConnection();
	            PreparedStatement ps = con.prepareStatement(sql);
	            ps.setLong(1, id);

	            ResultSet rs = ps.executeQuery();

	            if (rs.next()) {
	                Product p = mapProduct(rs);
	                rs.close();
	                ps.close();
	                return p;
	            }

	            rs.close();
	            ps.close();
	        }catch(SQLException e) {
	        	e.printStackTrace();
	        }
	        return null;
	 }
	
	 public boolean insertProduct(Product p) {
	        String sql = "INSERT INTO products "
	                + "(category_id, brand_id, name, description, active) "
	                + "VALUES (?, ?, ?, ?, ?)";

	        try {
	            Connection con = connectDB.getConnection();
	            PreparedStatement ps = con.prepareStatement(sql);

	            ps.setLong(1, p.getCategoryId());
	            ps.setLong(2, p.getBrandId());
	            ps.setString(3, p.getName());
	            ps.setString(4, p.getDescription());
	            ps.setBoolean(5, p.isActive());

	            int result = ps.executeUpdate();
	            ps.close();

	            return result > 0;

	        }catch(SQLException e) {
	        	e.printStackTrace();
	        }
	        return false;
	 }
	 public boolean updateProduct(Product p) {
	        String sql = "UPDATE products SET "
	                + "category_id = ?, brand_id = ?, name = ?, "
	                + "description = ?, active = ? "
	                + "WHERE id = ?";

	        try {
	            Connection con = connectDB.getConnection();
	            PreparedStatement ps = con.prepareStatement(sql);

	            ps.setLong(1, p.getCategoryId());
	            ps.setLong(2, p.getBrandId());
	            ps.setString(3, p.getName());
	            ps.setString(4, p.getDescription());
	            ps.setBoolean(5, p.isActive());
	            ps.setLong(6, p.getId());

	            int result = ps.executeUpdate();
	            ps.close();

	            return result > 0;

	        } catch (SQLException e) {
	            e.printStackTrace();
	        }

	        return false;
	    }
	 
	 public boolean updateProductStatus(Long id, boolean active) {
	        String sql = "UPDATE products SET active = ? WHERE id = ?";

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
	 
	
	private Product mapProduct(ResultSet rs) throws SQLException {
		// TODO Auto-generated method stub
		Product p = new Product();

        p.setId(rs.getLong("id"));

        long categoryId = rs.getLong("category_id");
        if (!rs.wasNull()) {
            p.setCategoryId(categoryId);
        }

        long brandId = rs.getLong("brand_id");
        if (!rs.wasNull()) {
            p.setBrandId(brandId);
        }

        p.setName(rs.getString("name"));
        p.setDescription(rs.getString("description"));
        p.setActive(rs.getBoolean("active"));

		return p;
	}
}

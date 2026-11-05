package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import connectDB.connectDB;
import model.Category;

public class CategoryDAO {
	public List<Category> getAllCategory(){
		List<Category> list = new ArrayList<>();
		String sql = "SELECT  * FROM categories";
		try {
			Connection con = connectDB.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            
            while(rs.next()) {
            	Category c = new Category();
            	c.setId(rs.getLong("id"));
            	c.setName(rs.getString("name"));
            	c.setActive(rs.getBoolean("active"));
            	
            	list.add(c);
            }
            rs.close();
            ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}
	public Category getCategoryById(Long id) {
		String sql = "SELECT * FROM categories WHERE id = ?";
		try {
			Connection con = connectDB.getConnection();
			PreparedStatement ps = con.prepareStatement(sql);
			ps.setLong(1, id);
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				Category c = new Category();
				c.setId(rs.getLong("id"));
				c.setName(rs.getString("name"));
				c.setActive(rs.getBoolean("active"));
				
				rs.close();
				ps.close();
				
				return c;
			}
			rs.close();
			ps.close();
		}catch(SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	public boolean insertCategory(Category c) {
		String sql = "INSERT INTO categories (name, active) VALUES (?, ?)";
		try {
			Connection con = connectDB.getConnection();
			PreparedStatement ps = con.prepareStatement(sql);
			ps.setString(1, c.getName());
			ps.setBoolean(2, c.isActive());
			
			int result = ps.executeUpdate();
			ps.close();
			return result > 0;	
		}catch(SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	public boolean updateCategory(Category c) {
		String sql = "UPDATE categories SET name = ?, active = ? WHERE id = ?";
		try {
			Connection con = connectDB.getConnection();
			PreparedStatement ps = con.prepareStatement(sql);
			ps.setString(1, c.getName());
			ps.setBoolean(2, c.isActive());
			ps.setLong(3, c.getId());
			
			int result = ps.executeUpdate();
			ps.close();
			return result > 0;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	public boolean updateCategoryStatus(Long id, boolean active) {
		String sql = "UPDATE categories SET active = ? WHERE id = ?";
		try {
			Connection con = connectDB.getConnection();
			PreparedStatement ps = con.prepareStatement(sql);
			
			ps.setBoolean(1, active);
			ps.setLong(2, id);
			
			int result = ps.executeUpdate();
			ps.close();
			return result > 0;
		}catch(SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
}

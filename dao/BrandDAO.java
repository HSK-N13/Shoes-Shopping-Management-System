package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import connectDB.connectDB;
import model.Brand;

public class BrandDAO {
	public ArrayList<Brand> getAllBrand(){
		ArrayList<Brand> list = new ArrayList<>();
		String sql = "SELECT * FROM brands";
		
		try {
			Connection con = connectDB.getConnection();
			PreparedStatement ps = con.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			
			while(rs.next()) {
				Brand b = new Brand();
				b.setId(rs.getLong("id"));
				b.setName(rs.getString("name"));
				b.setActive(rs.getBoolean("active"));
				list.add(b);
			}
			rs.close();
			ps.close();
		}catch(SQLException e) {
			e.printStackTrace();
		}
		return list;
	}
	public Brand getBrandById(Long id) {
		String sql = "SELECT * FROM brands WHERE id = ?";
		try {
			Connection con = connectDB.getConnection();
			PreparedStatement ps = con.prepareStatement(sql);
			ps.setLong(1, id);
			
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				Brand b = new Brand();
				b.setId(rs.getLong("id"));
				b.setName(rs.getString("name"));
				b.setActive(rs.getBoolean("active"));
				
				rs.close();
				ps.close();
				return b;
			}
			rs.close();
			ps.close();
			
		}catch(SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	public boolean insertBrand(Brand b) {
		String sql = "INSERT INTO brands (name, active) VALUES (?, ?)";
		try {
			Connection con = connectDB.getConnection();
			PreparedStatement ps = con.prepareStatement(sql);
			
			ps.setString(1, b.getName());
			ps.setBoolean(2, b.isActive());
			//ps.setLong(3, b.getId());
			
			int result = ps.executeUpdate();
			ps.close();
			return result > 0;
		}catch(SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	public boolean updateBrand(Brand b) {
		 String sql = "UPDATE brands SET name = ?, active = ? WHERE id = ?";
		 
		 try {
			 Connection con = connectDB.getConnection();
			 PreparedStatement ps = con.prepareStatement(sql);
			 
			 ps.setString(1, b.getName());
			 ps.setBoolean(2, b.isActive());
			 ps.setLong(3, b.getId());
			 
			 int result = ps.executeUpdate();
			 ps.close();
			 
			 return result > 0;
			 
		 }catch(SQLException e) {
			 e.printStackTrace();
		 }
		return false;
		
	}
	public boolean updateBrandStatus(Long id, boolean active) {
		String sql = "UPDATE brands SET active = ? WHERE id = ?";
		try {
			Connection con = connectDB.getConnection();
			PreparedStatement ps = con.prepareStatement(sql);
			
			ps.setBoolean(1, active);
			ps.setLong(2, id);
			
			int result = ps.executeUpdate();
			ps.close();
			
			return result > 0;
		} catch(SQLException e) {
			e.printStackTrace();
		}
		return false;
		
	}
}

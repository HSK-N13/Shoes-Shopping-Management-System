package dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import connectDB.ConnectDB;
import model.Supplier;

public class SupplierDAO {

	
	public ArrayList<Supplier> getAllSuppliers() {
	    ArrayList<Supplier> listSup = new ArrayList<>();
	    String sql = "SELECT * FROM suppliers";

	    try {
	        ConnectDB.getInstance().connect();
	        Connection conn = ConnectDB.getConnection();
	        Statement stmt = conn.createStatement();
	        ResultSet rs = stmt.executeQuery(sql);

	        while (rs.next()) {
	            Supplier sup = new Supplier();
	            sup.setId(rs.getLong("id"));
	            sup.setName(rs.getString("name"));
	            sup.setPhone(rs.getString("phone"));
	            
	            listSup.add(sup);
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return listSup;
	}
	
}

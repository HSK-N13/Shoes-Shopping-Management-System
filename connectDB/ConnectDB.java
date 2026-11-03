package connectDB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectDB {
	public static Connection con = null;
    private static ConnectDB instance = new ConnectDB();
    public static ConnectDB getInstance() {
        return instance;
    }

    public void connect() {
        String url = "jdbc:sqlserver://localhost:1433;databaseName=QuanLyBanHang";
        String user = "sa";
        String password = "sapassword";
        try {
			con = DriverManager.getConnection(url, user, password);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public static Connection getConnection() {
        return con;
    }

    public void disconnect() {
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}

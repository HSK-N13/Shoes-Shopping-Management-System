package connectDB;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Quản lý kết nối JDBC tới SQL Server.
 * <p>
 * Ứng dụng desktop dùng chung một kết nối duy nhất (singleton). Các DAO
 * luôn lấy kết nối qua {@link #getConnection()} và KHÔNG được đóng kết nối này.
 * Nhờ dùng chung một kết nối, Service có thể gom nhiều lệnh DAO vào cùng
 * một giao dịch bằng setAutoCommit(false) / commit() / rollback().
 * <p>
 * Thông tin kết nối mặc định có thể ghi đè bằng file {@code db.properties}
 * đặt ở thư mục chạy chương trình, gồm các khóa: db.url, db.user, db.password.
 */
public class ConnectDB {
    /** Kết nối dùng chung toàn ứng dụng. */
    public static Connection con = null;

    private static final String DEFAULT_URL =
            "jdbc:sqlserver://localhost:1433;databaseName=QuanLyBanHang;encrypt=false";
    private static final String DEFAULT_USER = "sa";
    private static final String DEFAULT_PASSWORD = "sapassword";

    private static ConnectDB instance = new ConnectDB();
    /** Lỗi của lần kết nối gần nhất (để báo nguyên nhân cho người dùng). */
    private static String lastError;

    /**
     * Lấy thể hiện duy nhất của lớp quản lý kết nối.
     *
     * @return đối tượng ConnectDB dùng chung
     */
    public static ConnectDB getInstance() {
        return instance;
    }

    /**
     * Mở kết nối tới database nếu chưa có hoặc kết nối cũ đã bị đóng.
     * Gọi nhiều lần không tạo thêm kết nối mới.
     */
    public void connect() {
        try {
            if (con != null && !con.isClosed()) {
                return;
            }
            Properties cfg = loadConfig();
            con = DriverManager.getConnection(
                    cfg.getProperty("db.url", DEFAULT_URL),
                    cfg.getProperty("db.user", DEFAULT_USER),
                    cfg.getProperty("db.password", DEFAULT_PASSWORD));
            lastError = null;
        } catch (SQLException e) {
            con = null;
            lastError = e.getMessage();
            e.printStackTrace();
        }
    }

    /**
     * Trả về kết nối dùng chung, tự kết nối lại nếu kết nối chưa mở.
     *
     * @return kết nối JDBC đang mở
     * @throws SQLException nếu không thể kết nối tới database
     */
    public static Connection getConnection() throws SQLException {
        if (con == null || con.isClosed()) {
            instance.connect();
        }
        if (con == null) {
            throw new SQLException("Không thể kết nối tới cơ sở dữ liệu. "
                    + "Hãy kiểm tra SQL Server và file db.properties."
                    + (lastError == null ? "" : "\nChi tiết: " + lastError));
        }
        return con;
    }

    /**
     * Đóng kết nối dùng chung (gọi khi thoát ứng dụng).
     */
    public void disconnect() {
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            con = null;
        }
    }

    /**
     * Đọc file cấu hình db.properties nếu có; không có thì trả về cấu hình rỗng
     * để dùng giá trị mặc định.
     *
     * @return cấu hình kết nối
     */
    private Properties loadConfig() {
        Properties p = new Properties();
        try (InputStream in = new FileInputStream("db.properties")) {
            p.load(in);
        } catch (IOException ignored) {
            // Không có file cấu hình -> dùng giá trị mặc định
        }
        return p;
    }
}

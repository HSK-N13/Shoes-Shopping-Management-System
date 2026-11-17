package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import model.Customer;

/**
 * Truy cập bảng customers (khách hàng).
 */
public class CustomerDAO extends BaseDAO {

    /**
     * Lấy toàn bộ khách hàng.
     *
     * @return danh sách khách hàng
     * @throws SQLException lỗi database
     */
    public ArrayList<Customer> getAllCustomers() throws SQLException {
        return search(null);
    }

    /**
     * Tìm khách hàng theo tên hoặc số điện thoại.
     *
     * @param keyword từ khóa (null/rỗng = lấy tất cả)
     * @return danh sách khách hàng phù hợp
     * @throws SQLException lỗi database
     */
    public ArrayList<Customer> search(String keyword) throws SQLException {
        String sql = "SELECT * FROM customers WHERE (? IS NULL OR full_name LIKE ? OR phone LIKE ?) ORDER BY full_name";
        ArrayList<Customer> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            String kw = like(keyword);
            ps.setString(1, kw);
            ps.setString(2, kw);
            ps.setString(3, kw);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    /**
     * Tìm khách hàng theo mã.
     *
     * @param id mã khách hàng
     * @return khách hàng hoặc null
     * @throws SQLException lỗi database
     */
    public Customer findById(Long id) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement("SELECT * FROM customers WHERE id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    /**
     * Tìm khách hàng theo đúng số điện thoại.
     *
     * @param phone số điện thoại
     * @return khách hàng hoặc null
     * @throws SQLException lỗi database
     */
    public Customer findByPhone(String phone) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement("SELECT * FROM customers WHERE phone = ?")) {
            ps.setString(1, phone);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    /**
     * Thêm khách hàng mới.
     *
     * @param s khách hàng cần thêm
     * @return true nếu thành công
     * @throws SQLException lỗi database
     */
    public boolean insert(Customer s) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement(
                "INSERT INTO customers (full_name, phone) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.getFullName());
            ps.setString(2, s.getPhone());
            s.setId(executeInsert(ps));
            return true;
        }
    }

    /**
     * Cập nhật thông tin khách hàng.
     *
     * @param s khách hàng với dữ liệu mới
     * @return true nếu có dòng được cập nhật
     * @throws SQLException lỗi database
     */
    public boolean update(Customer s) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement("UPDATE customers SET full_name = ?, phone = ? WHERE id = ?")) {
            ps.setString(1, s.getFullName());
            ps.setString(2, s.getPhone());
            ps.setLong(3, s.getId());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Kiểm tra khách hàng đã có đơn hàng chưa (để chặn xóa).
     *
     * @param id mã khách hàng
     * @return true nếu đã có đơn hàng
     * @throws SQLException lỗi database
     */
    public boolean hasOrders(Long id) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement("SELECT COUNT(*) FROM orders WHERE customer_id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Xóa khách hàng (chỉ khi chưa có đơn hàng).
     *
     * @param id mã khách hàng
     * @return true nếu đã xóa
     * @throws SQLException lỗi database
     */
    public boolean delete(Long id) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement("DELETE FROM customers WHERE id = ?")) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Chuyển một dòng kết quả thành đối tượng Customer.
     *
     * @param rs kết quả truy vấn đang trỏ tới dòng cần đọc
     * @return khách hàng
     * @throws SQLException lỗi đọc cột
     */
    private Customer map(ResultSet rs) throws SQLException {
        Customer s = new Customer();
        s.setId(rs.getLong("id"));
        s.setFullName(rs.getString("full_name"));
        s.setPhone(rs.getString("phone"));
        return s;
    }
}

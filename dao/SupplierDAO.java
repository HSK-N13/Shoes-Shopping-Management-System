package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import model.Supplier;

/**
 * Truy cập bảng suppliers (nhà cung cấp).
 */
public class SupplierDAO extends BaseDAO {

    /**
     * Lấy toàn bộ nhà cung cấp.
     *
     * @return danh sách nhà cung cấp
     * @throws SQLException lỗi database
     */
    public ArrayList<Supplier> getAllSuppliers() throws SQLException {
        return search(null);
    }

    /**
     * Tìm nhà cung cấp theo tên hoặc số điện thoại.
     *
     * @param keyword từ khóa (null/rỗng = lấy tất cả)
     * @return danh sách nhà cung cấp phù hợp
     * @throws SQLException lỗi database
     */
    public ArrayList<Supplier> search(String keyword) throws SQLException {
        String sql = "SELECT * FROM suppliers WHERE (? IS NULL OR name LIKE ? OR phone LIKE ?) ORDER BY name";
        ArrayList<Supplier> list = new ArrayList<>();
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
     * Tìm nhà cung cấp theo mã.
     *
     * @param id mã nhà cung cấp
     * @return nhà cung cấp hoặc null
     * @throws SQLException lỗi database
     */
    public Supplier findById(Long id) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement("SELECT * FROM suppliers WHERE id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    /**
     * Thêm nhà cung cấp mới.
     *
     * @param s nhà cung cấp cần thêm
     * @return true nếu thành công
     * @throws SQLException lỗi database
     */
    public boolean insert(Supplier s) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement(
                "INSERT INTO suppliers (name, phone) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.getName());
            ps.setString(2, s.getPhone());
            s.setId(executeInsert(ps));
            return true;
        }
    }

    /**
     * Cập nhật thông tin nhà cung cấp.
     *
     * @param s nhà cung cấp với dữ liệu mới
     * @return true nếu có dòng được cập nhật
     * @throws SQLException lỗi database
     */
    public boolean update(Supplier s) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement("UPDATE suppliers SET name = ?, phone = ? WHERE id = ?")) {
            ps.setString(1, s.getName());
            ps.setString(2, s.getPhone());
            ps.setLong(3, s.getId());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Kiểm tra nhà cung cấp đã có phiếu nhập chưa (để chặn xóa).
     *
     * @param id mã nhà cung cấp
     * @return true nếu đã có phiếu nhập
     * @throws SQLException lỗi database
     */
    public boolean hasReceipts(Long id) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement("SELECT COUNT(*) FROM purchase_receipts WHERE supplier_id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Xóa nhà cung cấp (chỉ khi chưa có phiếu nhập).
     *
     * @param id mã nhà cung cấp
     * @return true nếu đã xóa
     * @throws SQLException lỗi database
     */
    public boolean delete(Long id) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement("DELETE FROM suppliers WHERE id = ?")) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Chuyển một dòng kết quả thành đối tượng Supplier.
     *
     * @param rs kết quả truy vấn đang trỏ tới dòng cần đọc
     * @return nhà cung cấp
     * @throws SQLException lỗi đọc cột
     */
    private Supplier map(ResultSet rs) throws SQLException {
        Supplier s = new Supplier();
        s.setId(rs.getLong("id"));
        s.setName(rs.getString("name"));
        s.setPhone(rs.getString("phone"));
        return s;
    }
}

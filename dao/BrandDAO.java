package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import model.Brand;

/**
 * Truy cập bảng brands (thương hiệu).
 */
public class BrandDAO extends BaseDAO {

    /**
     * Lấy toàn bộ thương hiệu, sắp theo tên.
     *
     * @return danh sách thương hiệu
     * @throws SQLException lỗi database
     */
    public ArrayList<Brand> getAllBrand() throws SQLException {
        return query("SELECT * FROM brands ORDER BY name", null);
    }

    /**
     * Lấy các thương hiệu đang sử dụng (active = 1) để chọn khi thêm sản phẩm.
     *
     * @return danh sách thương hiệu đang hoạt động
     * @throws SQLException lỗi database
     */
    public ArrayList<Brand> getActiveBrands() throws SQLException {
        return query("SELECT * FROM brands WHERE active = 1 ORDER BY name", null);
    }

    /**
     * Tìm thương hiệu theo mã.
     *
     * @param id mã thương hiệu
     * @return thương hiệu hoặc null nếu không có
     * @throws SQLException lỗi database
     */
    public Brand getBrandById(Long id) throws SQLException {
        ArrayList<Brand> list = query("SELECT * FROM brands WHERE id = ?", id);
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * Kiểm tra tên thương hiệu đã tồn tại chưa (không phân biệt hoa thường).
     *
     * @param name      tên cần kiểm tra
     * @param excludeId mã bỏ qua khi kiểm tra (dùng lúc sửa), null nếu thêm mới
     * @return true nếu đã có thương hiệu khác trùng tên
     * @throws SQLException lỗi database
     */
    public boolean existsByName(String name, Long excludeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM brands WHERE LOWER(name) = LOWER(?) AND (? IS NULL OR id <> ?)";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, name.trim());
            setLong(ps, 2, excludeId);
            setLong(ps, 3, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Thêm thương hiệu mới và gán lại id vừa sinh vào đối tượng.
     *
     * @param b thương hiệu cần thêm
     * @return true nếu thêm thành công
     * @throws SQLException lỗi database
     */
    public boolean insertBrand(Brand b) throws SQLException {
        String sql = "INSERT INTO brands (name, active) VALUES (?, ?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, b.getName());
            ps.setBoolean(2, b.isActive());
            b.setId(executeInsert(ps));
            return true;
        }
    }

    /**
     * Cập nhật tên và trạng thái thương hiệu.
     *
     * @param b thương hiệu với dữ liệu mới
     * @return true nếu có dòng được cập nhật
     * @throws SQLException lỗi database
     */
    public boolean updateBrand(Brand b) throws SQLException {
        String sql = "UPDATE brands SET name = ?, active = ? WHERE id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, b.getName());
            ps.setBoolean(2, b.isActive());
            ps.setLong(3, b.getId());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Đổi trạng thái sử dụng của thương hiệu.
     *
     * @param id     mã thương hiệu
     * @param active trạng thái mới
     * @return true nếu có dòng được cập nhật
     * @throws SQLException lỗi database
     */
    public boolean updateBrandStatus(Long id, boolean active) throws SQLException {
        String sql = "UPDATE brands SET active = ? WHERE id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setBoolean(1, active);
            ps.setLong(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Đếm số sản phẩm đang thuộc thương hiệu (để chặn xóa).
     *
     * @param id mã thương hiệu
     * @return số sản phẩm
     * @throws SQLException lỗi database
     */
    public int countProducts(Long id) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement("SELECT COUNT(*) FROM products WHERE brand_id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /**
     * Xóa hẳn thương hiệu (chỉ dùng khi chưa có sản phẩm nào).
     *
     * @param id mã thương hiệu
     * @return true nếu đã xóa
     * @throws SQLException lỗi database
     */
    public boolean deleteBrand(Long id) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement("DELETE FROM brands WHERE id = ?")) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Chạy câu truy vấn trả về danh sách thương hiệu, có thể kèm một tham số id.
     *
     * @param sql câu SELECT
     * @param id  tham số (null nếu câu lệnh không có tham số)
     * @return danh sách thương hiệu
     * @throws SQLException lỗi database
     */
    private ArrayList<Brand> query(String sql, Long id) throws SQLException {
        ArrayList<Brand> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            if (id != null) {
                ps.setLong(1, id);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Brand b = new Brand();
                    b.setId(rs.getLong("id"));
                    b.setName(rs.getString("name"));
                    b.setActive(rs.getBoolean("active"));
                    list.add(b);
                }
            }
        }
        return list;
    }
}

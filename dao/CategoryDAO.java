package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import model.Category;

/**
 * Truy cập bảng categories (danh mục).
 */
public class CategoryDAO extends BaseDAO {

    /**
     * Lấy toàn bộ danh mục, sắp theo tên.
     *
     * @return danh sách danh mục
     * @throws SQLException lỗi database
     */
    public ArrayList<Category> getAllCategory() throws SQLException {
        return query("SELECT * FROM categories ORDER BY name", null);
    }

    /**
     * Lấy các danh mục đang sử dụng (active = 1) để chọn khi thêm sản phẩm.
     *
     * @return danh sách danh mục đang hoạt động
     * @throws SQLException lỗi database
     */
    public ArrayList<Category> getActiveCategories() throws SQLException {
        return query("SELECT * FROM categories WHERE active = 1 ORDER BY name", null);
    }

    /**
     * Tìm danh mục theo mã.
     *
     * @param id mã danh mục
     * @return danh mục hoặc null nếu không có
     * @throws SQLException lỗi database
     */
    public Category getCategoryById(Long id) throws SQLException {
        ArrayList<Category> list = query("SELECT * FROM categories WHERE id = ?", id);
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * Kiểm tra tên danh mục đã tồn tại chưa (không phân biệt hoa thường).
     *
     * @param name      tên cần kiểm tra
     * @param excludeId mã bỏ qua khi kiểm tra (dùng lúc sửa), null nếu thêm mới
     * @return true nếu đã có danh mục khác trùng tên
     * @throws SQLException lỗi database
     */
    public boolean existsByName(String name, Long excludeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM categories WHERE LOWER(name) = LOWER(?) AND (? IS NULL OR id <> ?)";
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
     * Thêm danh mục mới và gán lại id vừa sinh vào đối tượng.
     *
     * @param c danh mục cần thêm
     * @return true nếu thêm thành công
     * @throws SQLException lỗi database
     */
    public boolean insertCategory(Category c) throws SQLException {
        String sql = "INSERT INTO categories (name, active) VALUES (?, ?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getName());
            ps.setBoolean(2, c.isActive());
            c.setId(executeInsert(ps));
            return true;
        }
    }

    /**
     * Cập nhật tên và trạng thái danh mục.
     *
     * @param c danh mục với dữ liệu mới
     * @return true nếu có dòng được cập nhật
     * @throws SQLException lỗi database
     */
    public boolean updateCategory(Category c) throws SQLException {
        String sql = "UPDATE categories SET name = ?, active = ? WHERE id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, c.getName());
            ps.setBoolean(2, c.isActive());
            ps.setLong(3, c.getId());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Đổi trạng thái sử dụng của danh mục.
     *
     * @param id     mã danh mục
     * @param active trạng thái mới
     * @return true nếu có dòng được cập nhật
     * @throws SQLException lỗi database
     */
    public boolean updateCategoryStatus(Long id, boolean active) throws SQLException {
        String sql = "UPDATE categories SET active = ? WHERE id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setBoolean(1, active);
            ps.setLong(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Đếm số sản phẩm đang thuộc danh mục (để chặn xóa).
     *
     * @param id mã danh mục
     * @return số sản phẩm
     * @throws SQLException lỗi database
     */
    public int countProducts(Long id) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement("SELECT COUNT(*) FROM products WHERE category_id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /**
     * Xóa hẳn danh mục (chỉ dùng khi chưa có sản phẩm nào).
     *
     * @param id mã danh mục
     * @return true nếu đã xóa
     * @throws SQLException lỗi database
     */
    public boolean deleteCategory(Long id) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement("DELETE FROM categories WHERE id = ?")) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Chạy câu truy vấn trả về danh sách danh mục, có thể kèm một tham số id.
     *
     * @param sql câu SELECT
     * @param id  tham số (null nếu câu lệnh không có tham số)
     * @return danh sách danh mục
     * @throws SQLException lỗi database
     */
    private ArrayList<Category> query(String sql, Long id) throws SQLException {
        ArrayList<Category> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            if (id != null) {
                ps.setLong(1, id);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Category c = new Category();
                    c.setId(rs.getLong("id"));
                    c.setName(rs.getString("name"));
                    c.setActive(rs.getBoolean("active"));
                    list.add(c);
                }
            }
        }
        return list;
    }
}

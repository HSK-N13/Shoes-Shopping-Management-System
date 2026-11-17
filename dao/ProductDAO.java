package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;

import model.Product;

/**
 * Truy cập bảng products (mẫu giày).
 */
public class ProductDAO extends BaseDAO {

    private static final String SELECT_BASE = "SELECT p.*, c.name AS category_name, b.name AS brand_name "
            + "FROM products p "
            + "LEFT JOIN categories c ON p.category_id = c.id "
            + "LEFT JOIN brands b ON p.brand_id = b.id ";

    /**
     * Lấy toàn bộ sản phẩm.
     *
     * @return danh sách sản phẩm
     * @throws SQLException lỗi database
     */
    public ArrayList<Product> getAllProduct() throws SQLException {
        return searchProducts(null, null, null, null);
    }

    /**
     * Tìm sản phẩm theo từ khóa (tên sản phẩm, thương hiệu, SKU, size, màu của biến thể)
     * và lọc theo danh mục, thương hiệu, trạng thái. Tham số null nghĩa là không lọc.
     *
     * @param keyword    từ khóa
     * @param categoryId mã danh mục
     * @param brandId    mã thương hiệu
     * @param active     trạng thái kinh doanh
     * @return danh sách sản phẩm phù hợp
     * @throws SQLException lỗi database
     */
    public ArrayList<Product> searchProducts(String keyword, Long categoryId, Long brandId, Boolean active)
            throws SQLException {
        String sql = SELECT_BASE
                + "WHERE (? IS NULL OR p.name LIKE ? OR b.name LIKE ? OR EXISTS ("
                + "      SELECT 1 FROM product_variants pv WHERE pv.product_id = p.id "
                + "      AND (pv.sku LIKE ? OR pv.size LIKE ? OR pv.color LIKE ?))) "
                + "AND (? IS NULL OR p.category_id = ?) "
                + "AND (? IS NULL OR p.brand_id = ?) "
                + "AND (? IS NULL OR p.active = ?) "
                + "ORDER BY p.name";
        ArrayList<Product> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            String kw = like(keyword);
            for (int i = 1; i <= 6; i++) {
                ps.setString(i, kw);
            }
            setLong(ps, 7, categoryId);
            setLong(ps, 8, categoryId);
            setLong(ps, 9, brandId);
            setLong(ps, 10, brandId);
            if (active == null) {
                ps.setNull(11, Types.BIT);
                ps.setNull(12, Types.BIT);
            } else {
                ps.setBoolean(11, active);
                ps.setBoolean(12, active);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapProduct(rs));
                }
            }
        }
        return list;
    }

    /**
     * Tìm sản phẩm theo mã.
     *
     * @param id mã sản phẩm
     * @return sản phẩm hoặc null
     * @throws SQLException lỗi database
     */
    public Product getProductById(Long id) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement(SELECT_BASE + "WHERE p.id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapProduct(rs) : null;
            }
        }
    }

    /**
     * Thêm sản phẩm mới và gán id vừa sinh vào đối tượng.
     *
     * @param p sản phẩm cần thêm
     * @return true nếu thành công
     * @throws SQLException lỗi database
     */
    public boolean insertProduct(Product p) throws SQLException {
        String sql = "INSERT INTO products (category_id, brand_id, name, description, active) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setLong(ps, 1, p.getCategoryId());
            setLong(ps, 2, p.getBrandId());
            ps.setString(3, p.getName());
            ps.setString(4, p.getDescription());
            ps.setBoolean(5, p.isActive());
            p.setId(executeInsert(ps));
            return true;
        }
    }

    /**
     * Cập nhật thông tin sản phẩm.
     *
     * @param p sản phẩm với dữ liệu mới
     * @return true nếu có dòng được cập nhật
     * @throws SQLException lỗi database
     */
    public boolean updateProduct(Product p) throws SQLException {
        String sql = "UPDATE products SET category_id = ?, brand_id = ?, name = ?, description = ?, active = ? "
                + "WHERE id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            setLong(ps, 1, p.getCategoryId());
            setLong(ps, 2, p.getBrandId());
            ps.setString(3, p.getName());
            ps.setString(4, p.getDescription());
            ps.setBoolean(5, p.isActive());
            ps.setLong(6, p.getId());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Đổi trạng thái kinh doanh của sản phẩm.
     *
     * @param id     mã sản phẩm
     * @param active true = đang bán, false = ngừng kinh doanh
     * @return true nếu có dòng được cập nhật
     * @throws SQLException lỗi database
     */
    public boolean updateProductStatus(Long id, boolean active) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement("UPDATE products SET active = ? WHERE id = ?")) {
            ps.setBoolean(1, active);
            ps.setLong(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Kiểm tra sản phẩm đã phát sinh giao dịch chưa: có biến thể nằm trong
     * đơn hàng hoặc phiếu nhập, hoặc có lịch sử tồn kho (BR02).
     *
     * @param id mã sản phẩm
     * @return true nếu đã có giao dịch
     * @throws SQLException lỗi database
     */
    public boolean hasTransactions(Long id) throws SQLException {
        String sql = "SELECT CASE WHEN EXISTS (SELECT 1 FROM order_items oi JOIN product_variants v ON oi.variant_id = v.id WHERE v.product_id = ?) "
                + "OR EXISTS (SELECT 1 FROM purchase_items pi JOIN product_variants v ON pi.variant_id = v.id WHERE v.product_id = ?) "
                + "OR EXISTS (SELECT 1 FROM stock_movements sm JOIN product_variants v ON sm.variant_id = v.id WHERE v.product_id = ?) "
                + "THEN 1 ELSE 0 END";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.setLong(2, id);
            ps.setLong(3, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) == 1;
            }
        }
    }

    /**
     * Xóa hẳn sản phẩm cùng các biến thể (chỉ gọi khi chưa có giao dịch, BR02).
     *
     * @param id mã sản phẩm
     * @return true nếu đã xóa
     * @throws SQLException lỗi database
     */
    public boolean deleteProduct(Long id) throws SQLException {
        try (PreparedStatement ps1 = conn().prepareStatement("DELETE FROM product_variants WHERE product_id = ?");
                PreparedStatement ps2 = conn().prepareStatement("DELETE FROM products WHERE id = ?")) {
            ps1.setLong(1, id);
            ps1.executeUpdate();
            ps2.setLong(1, id);
            return ps2.executeUpdate() > 0;
        }
    }

    /**
     * Chuyển một dòng kết quả thành đối tượng Product.
     *
     * @param rs kết quả truy vấn đang trỏ tới dòng cần đọc
     * @return sản phẩm
     * @throws SQLException lỗi đọc cột
     */
    private Product mapProduct(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getLong("id"));
        p.setCategoryId(getLong(rs, "category_id"));
        p.setBrandId(getLong(rs, "brand_id"));
        p.setName(rs.getString("name"));
        p.setDescription(rs.getString("description"));
        p.setActive(rs.getBoolean("active"));
        p.setCategoryName(rs.getString("category_name"));
        p.setBrandName(rs.getString("brand_name"));
        return p;
    }
}

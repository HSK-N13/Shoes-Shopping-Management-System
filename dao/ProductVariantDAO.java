package dao;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;

import model.ProductVariant;

/**
 * Truy cập bảng product_variants (biến thể size/màu) và các thao tác tồn kho.
 */
public class ProductVariantDAO extends BaseDAO {

    private static final String SELECT_BASE = "SELECT v.*, p.name AS product_name, b.name AS brand_name "
            + "FROM product_variants v "
            + "JOIN products p ON v.product_id = p.id "
            + "LEFT JOIN brands b ON p.brand_id = b.id ";

    /**
     * Lấy toàn bộ biến thể của một sản phẩm.
     *
     * @param productId mã sản phẩm
     * @return danh sách biến thể
     * @throws SQLException lỗi database
     */
    public ArrayList<ProductVariant> getVariantsByProductId(Long productId) throws SQLException {
        return getVariantsByProductId(productId, null);
    }

    /**
     * Lấy biến thể của một sản phẩm, có thể lọc theo trạng thái.
     *
     * @param productId mã sản phẩm
     * @param active    trạng thái cần lọc (null = tất cả)
     * @return danh sách biến thể
     * @throws SQLException lỗi database
     */
    public ArrayList<ProductVariant> getVariantsByProductId(Long productId, Boolean active) throws SQLException {
        String sql = SELECT_BASE + "WHERE v.product_id = ? AND (? IS NULL OR v.active = ?) ORDER BY v.size, v.color";
        ArrayList<ProductVariant> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, productId);
            if (active == null) {
                ps.setNull(2, Types.BIT);
                ps.setNull(3, Types.BIT);
            } else {
                ps.setBoolean(2, active);
                ps.setBoolean(3, active);
            }
            readList(ps, list);
        }
        return list;
    }

    /**
     * Tìm biến thể để bán hoặc nhập hàng theo tên sản phẩm/SKU/thương hiệu,
     * size và màu (mục 2.3). Tham số rỗng/null nghĩa là không lọc.
     *
     * @param keyword    từ khóa tên sản phẩm, SKU hoặc thương hiệu
     * @param size       size cần tìm
     * @param color      màu cần tìm
     * @param onlyActive true để chỉ lấy biến thể và sản phẩm đang kinh doanh
     * @return danh sách biến thể phù hợp
     * @throws SQLException lỗi database
     */
    public ArrayList<ProductVariant> search(String keyword, String size, String color, boolean onlyActive)
            throws SQLException {
        String sql = SELECT_BASE
                + "WHERE (? IS NULL OR p.name LIKE ? OR v.sku LIKE ? OR b.name LIKE ?) "
                + "AND (? IS NULL OR v.size = ?) "
                + "AND (? IS NULL OR v.color LIKE ?) "
                + (onlyActive ? "AND v.active = 1 AND p.active = 1 " : "")
                + "ORDER BY p.name, v.size, v.color";
        ArrayList<ProductVariant> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            String kw = like(keyword);
            for (int i = 1; i <= 4; i++) {
                ps.setString(i, kw);
            }
            String s = (size == null || size.trim().isEmpty()) ? null : size.trim();
            ps.setString(5, s);
            ps.setString(6, s);
            String c = like(color);
            ps.setString(7, c);
            ps.setString(8, c);
            readList(ps, list);
        }
        return list;
    }

    /**
     * Lấy toàn bộ biến thể (dùng cho kiểm kê và báo cáo tồn kho).
     *
     * @return danh sách biến thể
     * @throws SQLException lỗi database
     */
    public ArrayList<ProductVariant> getAll() throws SQLException {
        ArrayList<ProductVariant> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(SELECT_BASE + "ORDER BY p.name, v.size, v.color")) {
            readList(ps, list);
        }
        return list;
    }

    /**
     * Lấy biến thể có tồn kho dưới ngưỡng tối thiểu (chỉ tính hàng đang kinh doanh).
     *
     * @return danh sách biến thể cần nhập thêm
     * @throws SQLException lỗi database
     */
    public ArrayList<ProductVariant> getLowStock() throws SQLException {
        ArrayList<ProductVariant> list = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE v.active = 1 AND ISNULL(v.stock_quantity, 0) < ISNULL(v.min_stock, 0) "
                + "ORDER BY (ISNULL(v.min_stock,0) - ISNULL(v.stock_quantity,0)) DESC";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            readList(ps, list);
        }
        return list;
    }

    /**
     * Tìm biến thể theo mã.
     *
     * @param id mã biến thể
     * @return biến thể hoặc null
     * @throws SQLException lỗi database
     */
    public ProductVariant getVariantById(Long id) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement(SELECT_BASE + "WHERE v.id = ?")) {
            ps.setLong(1, id);
            ArrayList<ProductVariant> list = new ArrayList<>();
            readList(ps, list);
            return list.isEmpty() ? null : list.get(0);
        }
    }

    /**
     * Đọc biến thể và khóa dòng đó tới hết giao dịch (UPDLOCK) để đọc tồn/giá
     * chính xác khi xác nhận đơn hoặc kiểm kê. Phải gọi bên trong giao dịch.
     *
     * @param id mã biến thể
     * @return biến thể hoặc null
     * @throws SQLException lỗi database
     */
    public ProductVariant getVariantForUpdate(Long id) throws SQLException {
        String sql = "SELECT v.*, p.name AS product_name, b.name AS brand_name "
                + "FROM product_variants v WITH (UPDLOCK, ROWLOCK) "
                + "JOIN products p ON v.product_id = p.id "
                + "LEFT JOIN brands b ON p.brand_id = b.id WHERE v.id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, id);
            ArrayList<ProductVariant> list = new ArrayList<>();
            readList(ps, list);
            return list.isEmpty() ? null : list.get(0);
        }
    }

    /**
     * Tìm biến thể theo SKU.
     *
     * @param sku mã SKU
     * @return biến thể hoặc null
     * @throws SQLException lỗi database
     */
    public ProductVariant getVariantBySku(String sku) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement(SELECT_BASE + "WHERE v.sku = ?")) {
            ps.setString(1, sku);
            ArrayList<ProductVariant> list = new ArrayList<>();
            readList(ps, list);
            return list.isEmpty() ? null : list.get(0);
        }
    }

    /**
     * Kiểm tra SKU đã được dùng bởi biến thể khác chưa (BR01).
     *
     * @param sku       mã SKU
     * @param excludeId mã biến thể bỏ qua (khi sửa), null khi thêm mới
     * @return true nếu trùng
     * @throws SQLException lỗi database
     */
    public boolean existsSku(String sku, Long excludeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM product_variants WHERE sku = ? AND (? IS NULL OR id <> ?)";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, sku);
            setLong(ps, 2, excludeId);
            setLong(ps, 3, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Kiểm tra tổ hợp size + màu đã tồn tại trong cùng sản phẩm chưa (BR01).
     *
     * @param productId mã sản phẩm
     * @param size      size
     * @param color     màu
     * @param excludeId mã biến thể bỏ qua (khi sửa), null khi thêm mới
     * @return true nếu trùng
     * @throws SQLException lỗi database
     */
    public boolean existsSizeColor(Long productId, String size, String color, Long excludeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM product_variants WHERE product_id = ? "
                + "AND LOWER(size) = LOWER(?) AND LOWER(color) = LOWER(?) AND (? IS NULL OR id <> ?)";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, productId);
            ps.setString(2, size);
            ps.setString(3, color);
            setLong(ps, 4, excludeId);
            setLong(ps, 5, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Thêm biến thể mới với tồn kho ban đầu bằng 0 (tồn chỉ tăng qua nhập hàng/kiểm kê, BR03).
     *
     * @param pv biến thể cần thêm
     * @return true nếu thành công
     * @throws SQLException lỗi database
     */
    public boolean insertVariant(ProductVariant pv) throws SQLException {
        String sql = "INSERT INTO product_variants (product_id, sku, size, color, selling_price, cost_price, "
                + "stock_quantity, min_stock, active) VALUES (?, ?, ?, ?, ?, ?, 0, ?, ?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, pv.getProductId());
            ps.setString(2, pv.getSku());
            ps.setString(3, pv.getSize());
            ps.setString(4, pv.getColor());
            ps.setBigDecimal(5, pv.getSellingPrice());
            ps.setBigDecimal(6, pv.getCostPrice() == null ? BigDecimal.ZERO : pv.getCostPrice());
            ps.setInt(7, pv.getMinStock());
            ps.setBoolean(8, pv.isActive());
            pv.setId(executeInsert(ps));
            pv.setStockQuantity(0);
            return true;
        }
    }

    /**
     * Cập nhật thông tin biến thể (không đổi tồn kho và giá vốn; hai giá trị này
     * chỉ thay đổi qua nhập hàng/bán hàng/kiểm kê).
     *
     * @param pv biến thể với dữ liệu mới
     * @return true nếu có dòng được cập nhật
     * @throws SQLException lỗi database
     */
    public boolean updateVariant(ProductVariant pv) throws SQLException {
        String sql = "UPDATE product_variants SET sku = ?, size = ?, color = ?, selling_price = ?, "
                + "min_stock = ?, active = ? WHERE id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, pv.getSku());
            ps.setString(2, pv.getSize());
            ps.setString(3, pv.getColor());
            ps.setBigDecimal(4, pv.getSellingPrice());
            ps.setInt(5, pv.getMinStock());
            ps.setBoolean(6, pv.isActive());
            ps.setLong(7, pv.getId());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Đổi trạng thái kinh doanh của biến thể.
     *
     * @param id     mã biến thể
     * @param active trạng thái mới
     * @return true nếu có dòng được cập nhật
     * @throws SQLException lỗi database
     */
    public boolean updateVariantStatus(Long id, boolean active) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement("UPDATE product_variants SET active = ? WHERE id = ?")) {
            ps.setBoolean(1, active);
            ps.setLong(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Ngừng kinh doanh tất cả biến thể của một sản phẩm.
     *
     * @param productId mã sản phẩm
     * @throws SQLException lỗi database
     */
    public void deactivateByProduct(Long productId) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement("UPDATE product_variants SET active = 0 WHERE product_id = ?")) {
            ps.setLong(1, productId);
            ps.executeUpdate();
        }
    }

    /**
     * Kiểm tra biến thể đã nằm trong đơn/phiếu nhập hoặc có lịch sử tồn kho chưa (BR02).
     *
     * @param id mã biến thể
     * @return true nếu đã có giao dịch
     * @throws SQLException lỗi database
     */
    public boolean hasTransactions(Long id) throws SQLException {
        String sql = "SELECT CASE WHEN EXISTS (SELECT 1 FROM order_items WHERE variant_id = ?) "
                + "OR EXISTS (SELECT 1 FROM purchase_items WHERE variant_id = ?) "
                + "OR EXISTS (SELECT 1 FROM stock_movements WHERE variant_id = ?) THEN 1 ELSE 0 END";
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
     * Xóa hẳn biến thể (chỉ gọi khi chưa có giao dịch, BR02).
     *
     * @param id mã biến thể
     * @return true nếu đã xóa
     * @throws SQLException lỗi database
     */
    public boolean deleteVariant(Long id) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement("DELETE FROM product_variants WHERE id = ?")) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Lấy số lượng tồn kho hiện tại của một biến thể.
     *
     * @param variantId mã biến thể
     * @return số lượng tồn (0 nếu không tìm thấy)
     * @throws SQLException lỗi database
     */
    public int findStockQuantityById(Long variantId) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement("SELECT stock_quantity FROM product_variants WHERE id = ?")) {
            ps.setLong(1, variantId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /**
     * Tăng tồn kho (nhập hàng, hoàn kho khi hủy đơn).
     *
     * @param variantId mã biến thể
     * @param quantity  số lượng tăng (dương)
     * @return true nếu cập nhật được
     * @throws SQLException lỗi database
     */
    public boolean increaseStock(Long variantId, int quantity) throws SQLException {
        String sql = "UPDATE product_variants SET stock_quantity = ISNULL(stock_quantity, 0) + ? WHERE id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setLong(2, variantId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Trừ kho có điều kiện để chống bán vượt tồn khi nhiều máy cùng dùng (BR03, BR05):
     * UPDATE ... SET stock = stock - ? WHERE id = ? AND stock &gt;= ?.
     *
     * @param variantId mã biến thể
     * @param quantity  số lượng cần trừ (dương)
     * @return true nếu trừ được; false nếu không đủ hàng (0 dòng cập nhật)
     * @throws SQLException lỗi database
     */
    public boolean decreaseStockIfEnough(Long variantId, int quantity) throws SQLException {
        String sql = "UPDATE product_variants SET stock_quantity = stock_quantity - ? "
                + "WHERE id = ? AND stock_quantity >= ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setLong(2, variantId);
            ps.setInt(3, quantity);
            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Cập nhật số lượng tồn kho mới cho biến thể (dùng khi kiểm kê).
     *
     * @param variantId   mã biến thể
     * @param newQuantity số lượng tồn mới (không âm)
     * @throws SQLException lỗi database
     */
    public void updateStock(Long variantId, int newQuantity) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement("UPDATE product_variants SET stock_quantity = ? WHERE id = ?")) {
            ps.setInt(1, newQuantity);
            ps.setLong(2, variantId);
            ps.executeUpdate();
        }
    }

    /**
     * Cập nhật giá vốn = giá nhập gần nhất khi xác nhận phiếu nhập (BR07).
     *
     * @param variantId mã biến thể
     * @param costPrice giá vốn mới
     * @throws SQLException lỗi database
     */
    public void updateCostPrice(Long variantId, BigDecimal costPrice) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement("UPDATE product_variants SET cost_price = ? WHERE id = ?")) {
            ps.setBigDecimal(1, costPrice);
            ps.setLong(2, variantId);
            ps.executeUpdate();
        }
    }

    /**
     * Chạy câu truy vấn và đọc toàn bộ kết quả vào danh sách.
     *
     * @param ps   câu lệnh đã gán tham số
     * @param list danh sách nhận kết quả
     * @throws SQLException lỗi database
     */
    private void readList(PreparedStatement ps, ArrayList<ProductVariant> list) throws SQLException {
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapVariant(rs));
            }
        }
    }

    /**
     * Chuyển một dòng kết quả thành đối tượng ProductVariant.
     *
     * @param rs kết quả truy vấn đang trỏ tới dòng cần đọc
     * @return biến thể
     * @throws SQLException lỗi đọc cột
     */
    private ProductVariant mapVariant(ResultSet rs) throws SQLException {
        ProductVariant pv = new ProductVariant();
        pv.setId(rs.getLong("id"));
        pv.setProductId(rs.getLong("product_id"));
        pv.setSku(rs.getString("sku"));
        pv.setSize(rs.getString("size"));
        pv.setColor(rs.getString("color"));
        pv.setSellingPrice(rs.getBigDecimal("selling_price"));
        pv.setCostPrice(rs.getBigDecimal("cost_price"));
        pv.setStockQuantity(rs.getInt("stock_quantity"));
        pv.setMinStock(rs.getInt("min_stock"));
        pv.setActive(rs.getBoolean("active"));
        pv.setProductName(rs.getString("product_name"));
        pv.setBrandName(rs.getString("brand_name"));
        return pv;
    }
}

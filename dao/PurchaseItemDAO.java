package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import model.PurchaseItem;

/**
 * Truy cập bảng purchase_items (dòng chi tiết phiếu nhập).
 */
public class PurchaseItemDAO extends BaseDAO {

    /**
     * Lưu một dòng chi tiết phiếu nhập và gán id vừa sinh.
     *
     * @param item dòng chi tiết (đã có receiptId)
     * @return chính dòng chi tiết đã có id
     * @throws SQLException lỗi database
     */
    public PurchaseItem save(PurchaseItem item) throws SQLException {
        String sql = "INSERT INTO purchase_items (receipt_id, variant_id, quantity, unit_cost) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, item.getReceiptId());
            ps.setLong(2, item.getVariantId());
            ps.setInt(3, item.getQuantity());
            ps.setBigDecimal(4, item.getUnitCost());
            item.setId(executeInsert(ps));
        }
        return item;
    }

    /**
     * Lưu danh sách dòng chi tiết cho một phiếu nhập.
     *
     * @param receiptId mã phiếu nhập
     * @param items     các dòng chi tiết
     * @throws SQLException lỗi database
     */
    public void saveAll(Long receiptId, List<PurchaseItem> items) throws SQLException {
        for (PurchaseItem item : items) {
            item.setReceiptId(receiptId);
            save(item);
        }
    }

    /**
     * Xóa toàn bộ dòng chi tiết của một phiếu (dùng khi sửa phiếu DRAFT).
     *
     * @param receiptId mã phiếu nhập
     * @throws SQLException lỗi database
     */
    public void deleteByReceiptId(Long receiptId) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement("DELETE FROM purchase_items WHERE receipt_id = ?")) {
            ps.setLong(1, receiptId);
            ps.executeUpdate();
        }
    }

    /**
     * Lấy các dòng chi tiết của một phiếu nhập kèm SKU và tên biến thể.
     *
     * @param receiptId mã phiếu nhập
     * @return danh sách dòng chi tiết
     * @throws SQLException lỗi database
     */
    public ArrayList<PurchaseItem> findByReceiptId(Long receiptId) throws SQLException {
        String sql = "SELECT pi.*, v.sku, p.name AS product_name, v.size, v.color "
                + "FROM purchase_items pi "
                + "JOIN product_variants v ON pi.variant_id = v.id "
                + "JOIN products p ON v.product_id = p.id "
                + "WHERE pi.receipt_id = ? ORDER BY pi.id";
        ArrayList<PurchaseItem> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, receiptId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PurchaseItem it = new PurchaseItem();
                    it.setId(rs.getLong("id"));
                    it.setReceiptId(rs.getLong("receipt_id"));
                    it.setVariantId(rs.getLong("variant_id"));
                    it.setQuantity(rs.getInt("quantity"));
                    it.setUnitCost(rs.getBigDecimal("unit_cost"));
                    it.setSku(rs.getString("sku"));
                    it.setVariantName(rs.getString("product_name") + " - Size " + rs.getString("size")
                            + " - " + rs.getString("color"));
                    list.add(it);
                }
            }
        }
        return list;
    }
}

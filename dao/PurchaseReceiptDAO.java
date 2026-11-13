package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;

import model.PurchaseReceipt;
import model.ReceiptStatus;

/**
 * Truy cập bảng purchase_receipts (phiếu nhập hàng).
 */
public class PurchaseReceiptDAO extends BaseDAO {

    private static final String SELECT_BASE = "SELECT r.*, s.name AS supplier_name, u.full_name AS user_name "
            + "FROM purchase_receipts r "
            + "JOIN suppliers s ON r.supplier_id = s.id "
            + "JOIN users u ON r.user_id = u.id ";

    /**
     * Lưu phiếu nhập mới và gán id vừa sinh vào đối tượng.
     *
     * @param receipt phiếu nhập cần lưu
     * @return chính phiếu nhập đã có id
     * @throws SQLException lỗi database
     */
    public PurchaseReceipt save(PurchaseReceipt receipt) throws SQLException {
        String sql = "INSERT INTO purchase_receipts (supplier_id, user_id, status, total_amount, created_at) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, receipt.getSupplierId());
            ps.setLong(2, receipt.getUserId());
            ps.setString(3, receipt.getStatus().name());
            ps.setBigDecimal(4, receipt.getTotalAmount());
            ps.setTimestamp(5, Timestamp.valueOf(
                    receipt.getCreatedAt() != null ? receipt.getCreatedAt() : LocalDateTime.now()));
            receipt.setId(executeInsert(ps));
        }
        return receipt;
    }

    /**
     * Cập nhật nhà cung cấp và tổng tiền của phiếu còn ở trạng thái DRAFT (BR04).
     *
     * @param receipt phiếu nhập với dữ liệu mới
     * @return true nếu cập nhật được (false nếu phiếu không còn là DRAFT)
     * @throws SQLException lỗi database
     */
    public boolean updateDraft(PurchaseReceipt receipt) throws SQLException {
        String sql = "UPDATE purchase_receipts SET supplier_id = ?, total_amount = ? WHERE id = ? AND status = 'DRAFT'";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, receipt.getSupplierId());
            ps.setBigDecimal(2, receipt.getTotalAmount());
            ps.setLong(3, receipt.getId());
            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Chuyển trạng thái phiếu có điều kiện: chỉ đổi khi trạng thái hiện tại đúng
     * bằng {@code from}. Nhờ vậy phiếu chỉ được xác nhận một lần (BR04).
     *
     * @param id   mã phiếu
     * @param from trạng thái hiện tại bắt buộc
     * @param to   trạng thái mới
     * @return true nếu đã chuyển; false nếu phiếu không ở trạng thái {@code from}
     * @throws SQLException lỗi database
     */
    public boolean changeStatus(Long id, ReceiptStatus from, ReceiptStatus to) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement(
                "UPDATE purchase_receipts SET status = ? WHERE id = ? AND status = ?")) {
            ps.setString(1, to.name());
            ps.setLong(2, id);
            ps.setString(3, from.name());
            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Tìm phiếu nhập theo mã (không kèm dòng chi tiết).
     *
     * @param id mã phiếu
     * @return phiếu nhập hoặc null
     * @throws SQLException lỗi database
     */
    public PurchaseReceipt findById(Long id) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement(SELECT_BASE + "WHERE r.id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    /**
     * Tìm phiếu nhập theo khoảng ngày và trạng thái.
     *
     * @param from   từ thời điểm (bao gồm), null = không giới hạn
     * @param to     đến thời điểm (không bao gồm), null = không giới hạn
     * @param status trạng thái cần lọc, null = tất cả
     * @return danh sách phiếu nhập, mới nhất trước
     * @throws SQLException lỗi database
     */
    public ArrayList<PurchaseReceipt> search(LocalDateTime from, LocalDateTime to, ReceiptStatus status)
            throws SQLException {
        String sql = SELECT_BASE
                + "WHERE (? IS NULL OR r.created_at >= ?) AND (? IS NULL OR r.created_at < ?) "
                + "AND (? IS NULL OR r.status = ?) ORDER BY r.created_at DESC, r.id DESC";
        ArrayList<PurchaseReceipt> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            setTimestamp(ps, 1, from);
            setTimestamp(ps, 2, from);
            setTimestamp(ps, 3, to);
            setTimestamp(ps, 4, to);
            String st = status == null ? null : status.name();
            ps.setString(5, st);
            ps.setString(6, st);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    /**
     * Chuyển một dòng kết quả thành đối tượng PurchaseReceipt.
     *
     * @param rs kết quả truy vấn đang trỏ tới dòng cần đọc
     * @return phiếu nhập
     * @throws SQLException lỗi đọc cột
     */
    private PurchaseReceipt map(ResultSet rs) throws SQLException {
        PurchaseReceipt r = new PurchaseReceipt();
        r.setId(rs.getLong("id"));
        r.setSupplierId(rs.getLong("supplier_id"));
        r.setUserId(rs.getLong("user_id"));
        r.setCreatedAt(getDateTime(rs, "created_at"));
        r.setStatus(ReceiptStatus.valueOf(rs.getString("status")));
        r.setTotalAmount(rs.getBigDecimal("total_amount"));
        r.setSupplierName(rs.getString("supplier_name"));
        r.setUserName(rs.getString("user_name"));
        return r;
    }
}

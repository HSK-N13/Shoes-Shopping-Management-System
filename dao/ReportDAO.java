package dao;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;

import dto.ProductSalesRow;
import dto.RevenueRow;

/**
 * Các truy vấn thống kê cho báo cáo (mục 2.4). Chỉ tính đơn đã thanh toán (PAID)
 * và phiếu nhập đã xác nhận (CONFIRMED).
 */
public class ReportDAO extends BaseDAO {

    /** Nhóm doanh thu theo ngày. */
    public static final int BY_DAY = 10;
    /** Nhóm doanh thu theo tháng. */
    public static final int BY_MONTH = 7;
    /** Nhóm doanh thu theo năm. */
    public static final int BY_YEAR = 4;

    /**
     * Doanh thu, số đơn và giá vốn theo từng kỳ trong khoảng thời gian.
     * Giá vốn lấy từ ảnh chụp unit_cost trong order_items (BR06).
     *
     * @param from      từ thời điểm (bao gồm)
     * @param to        đến thời điểm (không bao gồm)
     * @param groupSize độ dài nhãn kỳ: {@link #BY_DAY}, {@link #BY_MONTH} hoặc {@link #BY_YEAR}
     * @return các dòng doanh thu theo kỳ, tăng dần theo thời gian
     * @throws SQLException lỗi database
     */
    public ArrayList<RevenueRow> revenueByPeriod(LocalDateTime from, LocalDateTime to, int groupSize)
            throws SQLException {
        // CONVERT(varchar(n), date, 120) cho "yyyy-mm-dd hh:mi:ss"; cắt n ký tự đầu để nhóm theo ngày/tháng/năm
        String period = "CONVERT(VARCHAR(" + groupSize + "), o.created_at, 120)";
        String sql = "SELECT " + period + " AS period, COUNT(*) AS order_count, "
                + "SUM(o.total_amount) AS revenue, SUM(c.cost) AS cost "
                + "FROM orders o "
                + "CROSS APPLY (SELECT ISNULL(SUM(oi.quantity * oi.unit_cost), 0) AS cost "
                + "             FROM order_items oi WHERE oi.order_id = o.id) c "
                + "WHERE o.status = 'PAID' AND o.created_at >= ? AND o.created_at < ? "
                + "GROUP BY " + period + " ORDER BY period";
        ArrayList<RevenueRow> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            setTimestamp(ps, 1, from);
            setTimestamp(ps, 2, to);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    RevenueRow r = new RevenueRow();
                    r.setPeriod(rs.getString("period"));
                    r.setOrderCount(rs.getInt("order_count"));
                    r.setRevenue(nz(rs.getBigDecimal("revenue")));
                    r.setCost(nz(rs.getBigDecimal("cost")));
                    list.add(r);
                }
            }
        }
        return list;
    }

    /**
     * Sản phẩm (biến thể) bán chạy nhất trong khoảng thời gian.
     *
     * @param from  từ thời điểm (bao gồm)
     * @param to    đến thời điểm (không bao gồm)
     * @param limit số dòng tối đa
     * @return danh sách bán chạy, nhiều nhất trước
     * @throws SQLException lỗi database
     */
    public ArrayList<ProductSalesRow> bestSellers(LocalDateTime from, LocalDateTime to, int limit)
            throws SQLException {
        String sql = "SELECT TOP (?) v.sku, p.name AS product_name, v.size, v.color, "
                + "SUM(oi.quantity) AS qty, "
                + "SUM(oi.quantity * oi.unit_price - ISNULL(oi.discount_amount, 0)) AS revenue "
                + "FROM order_items oi "
                + "JOIN orders o ON oi.order_id = o.id "
                + "JOIN product_variants v ON oi.variant_id = v.id "
                + "JOIN products p ON v.product_id = p.id "
                + "WHERE o.status = 'PAID' AND o.created_at >= ? AND o.created_at < ? "
                + "GROUP BY v.sku, p.name, v.size, v.color "
                + "ORDER BY qty DESC, revenue DESC";
        ArrayList<ProductSalesRow> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, limit);
            setTimestamp(ps, 2, from);
            setTimestamp(ps, 3, to);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ProductSalesRow r = new ProductSalesRow();
                    r.setSku(rs.getString("sku"));
                    r.setVariantName(rs.getString("product_name") + " - Size " + rs.getString("size")
                            + " - " + rs.getString("color"));
                    r.setQuantitySold(rs.getInt("qty"));
                    r.setRevenue(nz(rs.getBigDecimal("revenue")));
                    list.add(r);
                }
            }
        }
        return list;
    }

    /**
     * Tổng giá trị nhập hàng của các phiếu đã xác nhận trong khoảng thời gian.
     *
     * @param from từ thời điểm (bao gồm)
     * @param to   đến thời điểm (không bao gồm)
     * @return tổng giá trị nhập
     * @throws SQLException lỗi database
     */
    public BigDecimal totalPurchaseValue(LocalDateTime from, LocalDateTime to) throws SQLException {
        String sql = "SELECT SUM(pi.quantity * pi.unit_cost) FROM purchase_items pi "
                + "JOIN purchase_receipts r ON pi.receipt_id = r.id "
                + "WHERE r.status = 'CONFIRMED' AND r.created_at >= ? AND r.created_at < ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            setTimestamp(ps, 1, from);
            setTimestamp(ps, 2, to);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? nz(rs.getBigDecimal(1)) : BigDecimal.ZERO;
            }
        }
    }

    /**
     * Đếm số phiếu nhập đã xác nhận trong khoảng thời gian.
     *
     * @param from từ thời điểm (bao gồm)
     * @param to   đến thời điểm (không bao gồm)
     * @return số phiếu
     * @throws SQLException lỗi database
     */
    public int countConfirmedReceipts(LocalDateTime from, LocalDateTime to) throws SQLException {
        String sql = "SELECT COUNT(*) FROM purchase_receipts WHERE status = 'CONFIRMED' "
                + "AND created_at >= ? AND created_at < ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            setTimestamp(ps, 1, from);
            setTimestamp(ps, 2, to);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /**
     * Tổng giá trị tồn kho hiện tại theo giá vốn.
     *
     * @return tổng (tồn x giá vốn) của mọi biến thể
     * @throws SQLException lỗi database
     */
    public BigDecimal inventoryValue() throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT SUM(ISNULL(stock_quantity,0) * cost_price) FROM product_variants");
                ResultSet rs = ps.executeQuery()) {
            return rs.next() ? nz(rs.getBigDecimal(1)) : BigDecimal.ZERO;
        }
    }

    /**
     * Đổi null thành 0.
     *
     * @param v giá trị có thể null
     * @return giá trị hoặc 0
     */
    private BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}

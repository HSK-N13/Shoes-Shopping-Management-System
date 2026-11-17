package dto;

import java.math.BigDecimal;

/**
 * Một dòng báo cáo doanh thu theo kỳ (ngày/tháng/năm).
 */
public class RevenueRow {
    /** Nhãn kỳ báo cáo (ví dụ 2026-11-03, 2026-11, 2026). */
    private String period;
    /** Số đơn đã thanh toán. */
    private int orderCount;
    /** Doanh thu thuần (tổng tiền đơn sau giảm giá). */
    private BigDecimal revenue;
    /** Tổng giá vốn = tổng (giá vốn x số lượng). */
    private BigDecimal cost;

    /**
     * Tạo đối tượng rỗng.
     */
    public RevenueRow() {
    }

    /**
     * Lấy nhãn kỳ báo cáo (ví dụ 2026-11-03, 2026-11, 2026).
     *
     * @return nhãn kỳ báo cáo (ví dụ 2026-11-03, 2026-11, 2026)
     */
    public String getPeriod() {
        return period;
    }

    /**
     * Gán nhãn kỳ báo cáo (ví dụ 2026-11-03, 2026-11, 2026).
     *
     * @param period nhãn kỳ báo cáo (ví dụ 2026-11-03, 2026-11, 2026)
     */
    public void setPeriod(String period) {
        this.period = period;
    }

    /**
     * Lấy số đơn đã thanh toán.
     *
     * @return số đơn đã thanh toán
     */
    public int getOrderCount() {
        return orderCount;
    }

    /**
     * Gán số đơn đã thanh toán.
     *
     * @param orderCount số đơn đã thanh toán
     */
    public void setOrderCount(int orderCount) {
        this.orderCount = orderCount;
    }

    /**
     * Lấy doanh thu thuần (tổng tiền đơn sau giảm giá).
     *
     * @return doanh thu thuần (tổng tiền đơn sau giảm giá)
     */
    public BigDecimal getRevenue() {
        return revenue;
    }

    /**
     * Gán doanh thu thuần (tổng tiền đơn sau giảm giá).
     *
     * @param revenue doanh thu thuần (tổng tiền đơn sau giảm giá)
     */
    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue;
    }

    /**
     * Lấy tổng giá vốn = tổng (giá vốn x số lượng).
     *
     * @return tổng giá vốn = tổng (giá vốn x số lượng)
     */
    public BigDecimal getCost() {
        return cost;
    }

    /**
     * Gán tổng giá vốn = tổng (giá vốn x số lượng).
     *
     * @param cost tổng giá vốn = tổng (giá vốn x số lượng)
     */
    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    /**
     * Lợi nhuận gộp = doanh thu thuần - tổng giá vốn (BR07).
     *
     * @return lợi nhuận gộp
     */
    public BigDecimal getGrossProfit() {
        BigDecimal r = revenue == null ? BigDecimal.ZERO : revenue;
        BigDecimal c = cost == null ? BigDecimal.ZERO : cost;
        return r.subtract(c);
    }
}

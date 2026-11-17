package controller;

import java.awt.Component;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import dto.ProductSalesRow;
import dto.RevenueRow;
import model.ProductVariant;
import services.ReportService;
import utils.Formatter;
import utils.Session;
import view.ReportPanel;
import view.UiKit;

/**
 * Xử lý màn hình báo cáo: lấy số liệu từ ReportService và hiển thị lên các tab.
 */
public class ReportController extends BaseController {
    private final ReportPanel view;
    private final ReportService reportService = new ReportService();

    /**
     * Gắn controller vào màn hình và đăng ký sự kiện.
     *
     * @param view màn hình báo cáo
     */
    public ReportController(ReportPanel view) {
        this.view = view;
        view.setAdminMode(Session.isAdmin());
        view.btnView.addActionListener(e -> handle(this::load));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Component parent() {
        return view;
    }

    /**
     * Tải lại toàn bộ báo cáo (gọi khi mở tab).
     */
    public void refresh() {
        handle(this::load);
    }

    /**
     * Lấy số liệu cho mọi tab theo khoảng ngày đang chọn.
     */
    private void load() {
        LocalDate from = UiKit.dateOf(view.spnFrom);
        LocalDate to = UiKit.dateOf(view.spnTo);
        boolean admin = Session.isAdmin();

        List<RevenueRow> revenue = reportService.revenue(from, to,
                (ReportService.Period) view.cboPeriod.getSelectedItem());
        List<Object[]> rows = new ArrayList<>();
        int orders = 0;
        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalProfit = BigDecimal.ZERO;
        for (RevenueRow r : revenue) {
            rows.add(new Object[] { r.getPeriod(), r.getOrderCount(), Formatter.money(r.getRevenue()),
                    admin ? Formatter.money(r.getCost()) : "", admin ? Formatter.money(r.getGrossProfit()) : "" });
            orders += r.getOrderCount();
            totalRevenue = totalRevenue.add(r.getRevenue());
            if (admin) {
                totalProfit = totalProfit.add(r.getGrossProfit());
            }
        }
        UiKit.fill(view.tblRevenue, rows);
        view.lblOrders.setText(String.valueOf(orders));
        view.lblRevenue.setText(Formatter.money(totalRevenue) + " đ");
        view.lblProfit.setText(Formatter.money(totalProfit) + " đ");

        rows = new ArrayList<>();
        int rank = 1;
        for (ProductSalesRow r : reportService.bestSellers(from, to, (Integer) view.spnTop.getValue())) {
            rows.add(new Object[] { rank++, r.getSku(), r.getVariantName(), r.getQuantitySold(),
                    Formatter.money(r.getRevenue()) });
        }
        UiKit.fill(view.tblBest, rows);

        rows = new ArrayList<>();
        for (ProductVariant v : reportService.inventory()) {
            rows.add(new Object[] { v.getSku(), v.getProductName(), v.getBrandName(), v.getSize(), v.getColor(),
                    v.getStockQuantity(), v.getMinStock(), v.isActive() ? "Đang bán" : "Ngừng KD" });
        }
        UiKit.fill(view.tblInventory, rows);

        rows = new ArrayList<>();
        for (ProductVariant v : reportService.lowStock()) {
            rows.add(new Object[] { v.getSku(), v.getProductName(), v.getSize(), v.getColor(), v.getStockQuantity(),
                    v.getMinStock(), v.getMinStock() - v.getStockQuantity() });
        }
        UiKit.fill(view.tblLowStock, rows);

        view.lblPurchaseValue.setText(Formatter.money(reportService.totalPurchaseValue(from, to)) + " đ");
        view.lblReceiptCount.setText(String.valueOf(reportService.confirmedReceiptCount(from, to)));
        if (admin) {
            view.lblInventoryValue.setText(Formatter.money(reportService.inventoryValue()) + " đ");
        }
    }
}

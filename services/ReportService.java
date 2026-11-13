package services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import dao.ProductVariantDAO;
import dao.ReportDAO;
import dto.ProductSalesRow;
import dto.RevenueRow;
import model.ProductVariant;
import utils.BusinessException;
import utils.Session;

/**
 * Báo cáo (mục 2.4). Nhân viên xem doanh thu và tồn kho; chỉ Admin xem giá vốn
 * và lợi nhuận gộp (BR12).
 */
public class ReportService extends BaseService {

    /**
     * Cách nhóm doanh thu.
     */
    public enum Period {
        /** Theo ngày. */
        DAY("Theo ngày", ReportDAO.BY_DAY),
        /** Theo tháng. */
        MONTH("Theo tháng", ReportDAO.BY_MONTH),
        /** Theo năm. */
        YEAR("Theo năm", ReportDAO.BY_YEAR);

        private final String label;
        private final int groupSize;

        /**
         * Khởi tạo kiểu nhóm.
         *
         * @param label     nhãn hiển thị
         * @param groupSize độ dài nhãn kỳ trong SQL
         */
        Period(String label, int groupSize) {
            this.label = label;
            this.groupSize = groupSize;
        }

        /**
         * Hiển thị nhãn tiếng Việt trong ComboBox.
         *
         * @return nhãn
         */
        @Override
        public String toString() {
            return label;
        }
    }

    private final ReportDAO reportDAO = new ReportDAO();
    private final ProductVariantDAO variantDAO = new ProductVariantDAO();

    /**
     * Doanh thu theo kỳ trong khoảng ngày [from, to] (tính cả hai đầu).
     * Với nhân viên, cột giá vốn được ẩn (đặt null) theo BR12.
     *
     * @param from   từ ngày
     * @param to     đến ngày
     * @param period cách nhóm
     * @return các dòng doanh thu
     */
    public List<RevenueRow> revenue(LocalDate from, LocalDate to, Period period) {
        Session.requireLogin();
        checkRange(from, to);
        List<RevenueRow> rows = db(() -> reportDAO.revenueByPeriod(start(from), end(to), period.groupSize));
        if (!Session.isAdmin()) {
            for (RevenueRow r : rows) {
                r.setCost(null);
            }
        }
        return rows;
    }

    /**
     * Sản phẩm bán chạy trong khoảng ngày.
     *
     * @param from  từ ngày
     * @param to    đến ngày
     * @param limit số dòng tối đa
     * @return danh sách bán chạy
     */
    public List<ProductSalesRow> bestSellers(LocalDate from, LocalDate to, int limit) {
        Session.requireLogin();
        checkRange(from, to);
        return db(() -> reportDAO.bestSellers(start(from), end(to), limit));
    }

    /**
     * Danh sách tồn kho toàn bộ biến thể.
     *
     * @return danh sách biến thể kèm tồn
     */
    public List<ProductVariant> inventory() {
        Session.requireLogin();
        return db(variantDAO::getAll);
    }

    /**
     * Danh sách hàng dưới ngưỡng tồn tối thiểu.
     *
     * @return danh sách biến thể cần nhập thêm
     */
    public List<ProductVariant> lowStock() {
        Session.requireLogin();
        return db(variantDAO::getLowStock);
    }

    /**
     * Tổng giá trị nhập của các phiếu đã xác nhận trong khoảng ngày.
     *
     * @param from từ ngày
     * @param to   đến ngày
     * @return tổng giá trị nhập
     */
    public BigDecimal totalPurchaseValue(LocalDate from, LocalDate to) {
        Session.requireLogin();
        checkRange(from, to);
        return db(() -> reportDAO.totalPurchaseValue(start(from), end(to)));
    }

    /**
     * Số phiếu nhập đã xác nhận trong khoảng ngày.
     *
     * @param from từ ngày
     * @param to   đến ngày
     * @return số phiếu
     */
    public int confirmedReceiptCount(LocalDate from, LocalDate to) {
        Session.requireLogin();
        checkRange(from, to);
        return db(() -> reportDAO.countConfirmedReceipts(start(from), end(to)));
    }

    /**
     * Tổng giá trị tồn kho theo giá vốn (chỉ Admin vì lộ giá vốn).
     *
     * @return giá trị tồn kho
     */
    public BigDecimal inventoryValue() {
        Session.requireAdmin();
        return db(reportDAO::inventoryValue);
    }

    /**
     * Kiểm tra khoảng ngày hợp lệ.
     *
     * @param from từ ngày
     * @param to   đến ngày
     * @throws BusinessException nếu thiếu hoặc từ ngày sau đến ngày
     */
    private void checkRange(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new BusinessException("Phải chọn khoảng ngày.");
        }
        if (from.isAfter(to)) {
            throw new BusinessException("Từ ngày phải trước hoặc bằng đến ngày.");
        }
    }

    /**
     * Đầu ngày.
     *
     * @param d ngày
     * @return 00:00 của ngày đó
     */
    private LocalDateTime start(LocalDate d) {
        return d.atStartOfDay();
    }

    /**
     * Mốc kết thúc (không bao gồm) = 00:00 của ngày hôm sau.
     *
     * @param d ngày
     * @return 00:00 ngày kế tiếp
     */
    private LocalDateTime end(LocalDate d) {
        return d.plusDays(1).atStartOfDay();
    }
}

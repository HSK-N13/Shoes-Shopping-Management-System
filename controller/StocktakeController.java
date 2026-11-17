package controller;

import java.awt.Component;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import model.MovementType;
import model.ProductVariant;
import model.StockMovement;
import services.InventoryService;
import services.ProductService;
import utils.BusinessException;
import utils.Formatter;
import view.StocktakePanel;
import view.UiKit;

/**
 * Xử lý màn hình kiểm kê: nhập số lượng thực tế, tạo điều chỉnh chênh lệch,
 * đối soát và xem lịch sử biến động tồn.
 */
public class StocktakeController extends BaseController {
    private final StocktakePanel view;
    private final InventoryService inventoryService = new InventoryService();
    private final ProductService productService = new ProductService();
    private boolean loading;

    /**
     * Gắn controller vào màn hình và đăng ký sự kiện.
     *
     * @param view màn hình kiểm kê
     */
    public StocktakeController(StocktakePanel view) {
        this.view = view;
        view.btnReload.addActionListener(e -> handle(this::reload));
        view.btnApply.addActionListener(e -> handle(this::apply));
        view.btnReconcile.addActionListener(e -> handle(this::reconcile));
        view.btnHistory.addActionListener(e -> handle(this::loadHistory));
        view.txtSku.addActionListener(e -> handle(this::loadHistory));
        view.chkOnlyDiff.addActionListener(e -> applyFilter());
        view.txtFilter.getDocument().addDocumentListener(new DocumentListener() {
            /**
             * Lọc lại khi gõ thêm ký tự.
             *
             * @param e sự kiện
             */
            @Override
            public void insertUpdate(DocumentEvent e) {
                applyFilter();
            }

            /**
             * Lọc lại khi xóa ký tự.
             *
             * @param e sự kiện
             */
            @Override
            public void removeUpdate(DocumentEvent e) {
                applyFilter();
            }

            /**
             * Lọc lại khi thuộc tính văn bản thay đổi.
             *
             * @param e sự kiện
             */
            @Override
            public void changedUpdate(DocumentEvent e) {
                applyFilter();
            }
        });
        view.tblStock.getModel().addTableModelListener(e -> {
            if (!loading && e.getColumn() == StocktakePanel.COL_ACTUAL) {
                updateDiff(e.getFirstRow());
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Component parent() {
        return view;
    }

    /**
     * Nạp lại tồn kho khi mở tab.
     */
    public void refresh() {
        handle(this::reload);
    }

    /**
     * Nạp toàn bộ biến thể với tồn hệ thống; cột Thực tế để trống.
     */
    private void reload() {
        List<ProductVariant> list = inventoryService.getAllVariants();
        List<Object[]> rows = new ArrayList<>();
        for (ProductVariant v : list) {
            rows.add(new Object[] { v.getId(), v.getSku(), v.getProductName(), v.getSize(), v.getColor(),
                    v.getStockQuantity(), "", "" });
        }
        loading = true;
        try {
            UiKit.fill(view.tblStock, rows);
        } finally {
            loading = false;
        }
        applyFilter();
    }

    /**
     * Tính lại cột chênh lệch của một dòng khi người dùng nhập số thực tế.
     *
     * @param modelRow chỉ số dòng trong model
     */
    private void updateDiff(int modelRow) {
        DefaultTableModel m = (DefaultTableModel) view.tblStock.getModel();
        if (modelRow < 0 || modelRow >= m.getRowCount()) {
            return;
        }
        Object actual = m.getValueAt(modelRow, StocktakePanel.COL_ACTUAL);
        String diff = "";
        if (actual != null && !actual.toString().trim().isEmpty()) {
            try {
                int a = Integer.parseInt(actual.toString().trim());
                int d = a - ((Integer) m.getValueAt(modelRow, 5));
                diff = d > 0 ? "+" + d : String.valueOf(d);
            } catch (NumberFormatException ex) {
                diff = "Sai số";
            }
        }
        loading = true;
        try {
            m.setValueAt(diff, modelRow, 7);
        } finally {
            loading = false;
        }
    }

    /**
     * Gom các dòng đã nhập số thực tế và gửi cho Service tạo điều chỉnh (một giao dịch).
     */
    private void apply() {
        if (view.tblStock.isEditing()) {
            view.tblStock.getCellEditor().stopCellEditing();
        }
        TableModel m = view.tblStock.getModel();
        Map<Long, Integer> actual = new LinkedHashMap<>();
        StringBuilder preview = new StringBuilder();
        for (int i = 0; i < m.getRowCount(); i++) {
            Object val = m.getValueAt(i, StocktakePanel.COL_ACTUAL);
            if (val == null || val.toString().trim().isEmpty()) {
                continue;
            }
            int qty = Formatter.parseInt(val.toString(), "Số lượng thực tế của " + m.getValueAt(i, 1));
            if (qty < 0) {
                throw new BusinessException("Số lượng thực tế của " + m.getValueAt(i, 1) + " không được âm.");
            }
            actual.put((Long) m.getValueAt(i, 0), qty);
            int diff = qty - (Integer) m.getValueAt(i, 5);
            if (diff != 0) {
                preview.append(m.getValueAt(i, 1)).append(": ").append(diff > 0 ? "+" : "").append(diff).append('\n');
            }
        }
        if (actual.isEmpty()) {
            throw new BusinessException("Chưa nhập số lượng thực tế cho dòng nào.");
        }
        if (preview.length() == 0) {
            UiKit.info(view, "Số lượng thực tế khớp với tồn hệ thống, không cần điều chỉnh.");
            return;
        }
        if (!UiKit.confirm(view, "Tạo điều chỉnh cho các biến thể sau?\n" + preview)) {
            return;
        }
        int n = inventoryService.applyStocktake(actual);
        UiKit.info(view, "Đã điều chỉnh tồn kho cho " + n + " biến thể.");
        reload();
    }

    /**
     * Đối soát tồn hiện tại với tổng lịch sử biến động và báo các biến thể bị lệch.
     */
    private void reconcile() {
        Map<String, int[]> diff = inventoryService.reconcile();
        if (diff.isEmpty()) {
            UiKit.info(view, "Tồn kho khớp với lịch sử biến động cho mọi biến thể.");
            return;
        }
        StringBuilder sb = new StringBuilder("Các biến thể có tồn khác tổng lịch sử biến động:\n");
        for (Map.Entry<String, int[]> e : diff.entrySet()) {
            sb.append(e.getKey()).append(": tồn ").append(e.getValue()[0]).append(", lịch sử ")
                    .append(e.getValue()[1]).append('\n');
        }
        sb.append("\nHãy kiểm kê để đưa tồn về đúng số thực tế.");
        UiKit.info(view, sb.toString());
    }

    /**
     * Lọc bảng kiểm kê theo từ khóa và tùy chọn chỉ hiện dòng chênh lệch.
     */
    private void applyFilter() {
        @SuppressWarnings("unchecked")
        TableRowSorter<TableModel> sorter = (TableRowSorter<TableModel>) view.tblStock.getRowSorter();
        String kw = view.txtFilter.getText().trim().toLowerCase();
        boolean onlyDiff = view.chkOnlyDiff.isSelected();
        sorter.setRowFilter(new RowFilter<TableModel, Integer>() {
            /**
             * Quyết định một dòng có được hiển thị hay không.
             *
             * @param entry dòng cần xét
             * @return true nếu hiển thị
             */
            @Override
            public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
                String text = (entry.getStringValue(1) + " " + entry.getStringValue(2)).toLowerCase();
                if (!kw.isEmpty() && !text.contains(kw)) {
                    return false;
                }
                if (onlyDiff) {
                    String d = entry.getStringValue(7);
                    return !d.isEmpty() && !"0".equals(d);
                }
                return true;
            }
        });
    }

    /**
     * Nạp lịch sử biến động theo loại, SKU và khoảng ngày.
     */
    private void loadHistory() {
        LocalDate from = UiKit.dateOf(view.spnFrom);
        LocalDate to = UiKit.dateOf(view.spnTo);
        if (from.isAfter(to)) {
            throw new BusinessException("Từ ngày phải trước hoặc bằng đến ngày.");
        }
        Long variantId = null;
        String sku = view.txtSku.getText().trim();
        if (!sku.isEmpty()) {
            List<ProductVariant> found = productService.searchVariants(sku, null, null, false);
            for (ProductVariant v : found) {
                if (v.getSku().equalsIgnoreCase(sku)) {
                    variantId = v.getId();
                }
            }
            if (variantId == null) {
                throw new BusinessException("Không tìm thấy SKU " + sku + ".");
            }
        }
        Object t = view.cboType.getSelectedItem();
        List<StockMovement> list = inventoryService.getMovements(variantId,
                t instanceof MovementType ? (MovementType) t : null, from.atStartOfDay(), to.plusDays(1).atStartOfDay());
        List<Object[]> rows = new ArrayList<>();
        for (StockMovement m : list) {
            String ref = m.getRefType() == null ? "" : m.getRefType() + (m.getRefId() == null ? "" : " #" + m.getRefId());
            rows.add(new Object[] { Formatter.dateTime(m.getCreatedAt()), m.getSku(), m.getVariantName(),
                    m.getType().getLabel(), m.getQuantityChange() > 0 ? "+" + m.getQuantityChange()
                            : String.valueOf(m.getQuantityChange()), ref });
        }
        UiKit.fill(view.tblHistory, rows);
    }
}

package controller;

import java.awt.Component;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import model.ProductVariant;
import model.PurchaseItem;
import model.PurchaseReceipt;
import model.ReceiptStatus;
import model.Supplier;
import services.PartnerService;
import services.ProductService;
import services.PurchaseService;
import utils.BusinessException;
import utils.Formatter;
import view.PurchasePanel;
import view.UiKit;

/**
 * Xử lý màn hình nhập hàng: lập/sửa phiếu nháp, xác nhận, hủy và xem danh sách phiếu.
 */
public class PurchaseController extends BaseController {
    private final PurchasePanel view;
    private final PurchaseService purchaseService = new PurchaseService();
    private final ProductService productService = new ProductService();
    private final PartnerService partnerService = new PartnerService();
    private final List<PurchaseItem> lines = new ArrayList<>();
    private List<ProductVariant> variants = new ArrayList<>();
    private List<PurchaseReceipt> receipts = new ArrayList<>();
    private Long editingReceiptId;

    /**
     * Gắn controller vào màn hình và đăng ký sự kiện.
     *
     * @param view màn hình nhập hàng
     */
    public PurchaseController(PurchasePanel view) {
        this.view = view;
        view.btnSearch.addActionListener(e -> handle(this::searchVariants));
        view.txtKeyword.addActionListener(e -> handle(this::searchVariants));
        view.tblVariants.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                suggestCost();
            }
        });
        view.btnAddLine.addActionListener(e -> handle(this::addLine));
        view.btnRemoveLine.addActionListener(e -> handle(this::removeLine));
        view.btnNew.addActionListener(e -> newReceipt());
        view.btnSaveDraft.addActionListener(e -> handle(() -> save(false)));
        view.btnSaveConfirm.addActionListener(e -> handle(() -> save(true)));
        view.btnFilter.addActionListener(e -> handle(this::loadReceipts));
        view.tblReceipts.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                handle(this::showReceiptItems);
            }
        });
        view.btnEdit.addActionListener(e -> handle(this::editDraft));
        view.btnConfirm.addActionListener(e -> handle(this::confirmSelected));
        view.btnCancelReceipt.addActionListener(e -> handle(this::cancelSelected));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Component parent() {
        return view;
    }

    /**
     * Nạp lại nhà cung cấp, biến thể và danh sách phiếu (gọi khi mở tab).
     */
    public void refresh() {
        handle(() -> {
            Object selected = view.cboSupplier.getSelectedItem();
            view.cboSupplier.removeAllItems();
            for (Supplier s : partnerService.searchSuppliers(null)) {
                view.cboSupplier.addItem(s);
                if (selected instanceof Supplier && ((Supplier) selected).getId().equals(s.getId())) {
                    view.cboSupplier.setSelectedItem(s);
                }
            }
            searchVariants();
            loadReceipts();
        });
    }

    /**
     * Tìm biến thể theo từ khóa (gồm cả biến thể đang ngừng bán để vẫn nhập bổ sung được).
     */
    private void searchVariants() {
        variants = productService.searchVariants(view.txtKeyword.getText(), null, null, false);
        List<Object[]> rows = new ArrayList<>();
        for (ProductVariant v : variants) {
            rows.add(new Object[] { v.getId(), v.getSku(), v.getProductName(), v.getSize(), v.getColor(),
                    v.getStockQuantity(), Formatter.money(v.getCostPrice()) });
        }
        UiKit.fill(view.tblVariants, rows);
    }

    /**
     * Gợi ý giá nhập = giá vốn hiện tại của biến thể vừa chọn.
     */
    private void suggestCost() {
        ProductVariant v = selectedVariant();
        if (v != null) {
            view.txtUnitCost.setText(Formatter.money(v.getCostPrice()));
        }
    }

    /**
     * Thêm (hoặc cộng dồn) một dòng nhập cho biến thể đang chọn.
     */
    private void addLine() {
        ProductVariant v = selectedVariant();
        if (v == null) {
            throw new BusinessException("Hãy chọn biến thể cần nhập.");
        }
        int qty = (Integer) view.spnQuantity.getValue();
        BigDecimal cost = Formatter.parseMoney(view.txtUnitCost.getText());
        if (cost.signum() < 0) {
            throw new BusinessException("Giá nhập không được âm.");
        }
        for (PurchaseItem it : lines) {
            if (it.getVariantId().equals(v.getId())) {
                it.setQuantity(it.getQuantity() + qty);
                it.setUnitCost(cost);
                renderLines();
                return;
            }
        }
        PurchaseItem it = new PurchaseItem();
        it.setVariantId(v.getId());
        it.setQuantity(qty);
        it.setUnitCost(cost);
        it.setSku(v.getSku());
        it.setVariantName(v.getDisplayName());
        lines.add(it);
        view.spnQuantity.setValue(1);
        renderLines();
    }

    /**
     * Xóa dòng nhập đang chọn.
     */
    private void removeLine() {
        int row = UiKit.selectedModelRow(view.tblLines);
        if (row < 0) {
            throw new BusinessException("Hãy chọn dòng cần xóa.");
        }
        Long id = (Long) view.tblLines.getModel().getValueAt(row, 0);
        lines.removeIf(it -> it.getVariantId().equals(id));
        renderLines();
    }

    /**
     * Lưu phiếu nháp; nếu {@code confirm} thì xác nhận nhập kho ngay sau khi lưu.
     *
     * @param confirm true để xác nhận ngay
     */
    private void save(boolean confirm) {
        Supplier s = (Supplier) view.cboSupplier.getSelectedItem();
        Long supplierId = s == null ? null : s.getId();
        PurchaseReceipt r = editingReceiptId == null
                ? purchaseService.createPurchaseReceipt(supplierId, new ArrayList<>(lines))
                : purchaseService.updateDraft(editingReceiptId, supplierId, new ArrayList<>(lines));
        editingReceiptId = r.getId();
        if (confirm) {
            if (!UiKit.confirm(view, "Xác nhận nhập kho phiếu #" + r.getId()
                    + "? Sau khi xác nhận, phiếu không thể sửa hoặc hủy.")) {
                UiKit.info(view, "Đã lưu nháp phiếu #" + r.getId() + ".");
                renderLines();
                loadReceipts();
                return;
            }
            purchaseService.confirmReceipt(r.getId());
            UiKit.info(view, "Đã xác nhận phiếu #" + r.getId() + ". Tồn kho đã được cập nhật.");
            newReceipt();
        } else {
            UiKit.info(view, "Đã lưu nháp phiếu #" + r.getId() + ".");
            renderLines();
        }
        searchVariants();
        loadReceipts();
    }

    /**
     * Làm trống form để lập phiếu mới.
     */
    private void newReceipt() {
        editingReceiptId = null;
        lines.clear();
        view.txtUnitCost.setText("");
        renderLines();
    }

    /**
     * Hiển thị các dòng nhập và tổng tiền.
     */
    private void renderLines() {
        BigDecimal total = BigDecimal.ZERO;
        List<Object[]> rows = new ArrayList<>();
        for (PurchaseItem it : lines) {
            rows.add(new Object[] { it.getVariantId(), it.getSku(), it.getVariantName(), it.getQuantity(),
                    Formatter.money(it.getUnitCost()), Formatter.money(it.getLineTotal()) });
            total = total.add(it.getLineTotal());
        }
        UiKit.fill(view.tblLines, rows);
        view.lblTotal.setText(Formatter.money(total) + " đ");
        view.lblReceiptInfo.setText(editingReceiptId == null ? "Phiếu mới" : "Đang sửa phiếu nháp #" + editingReceiptId);
    }

    /**
     * Nạp danh sách phiếu nhập theo bộ lọc.
     */
    private void loadReceipts() {
        LocalDate from = UiKit.dateOf(view.spnFrom);
        LocalDate to = UiKit.dateOf(view.spnTo);
        if (from.isAfter(to)) {
            throw new BusinessException("Từ ngày phải trước hoặc bằng đến ngày.");
        }
        Object st = view.cboStatus.getSelectedItem();
        receipts = purchaseService.searchReceipts(from.atStartOfDay(), to.plusDays(1).atStartOfDay(),
                st instanceof ReceiptStatus ? (ReceiptStatus) st : null);
        List<Object[]> rows = new ArrayList<>();
        for (PurchaseReceipt r : receipts) {
            rows.add(new Object[] { r.getId(), Formatter.dateTime(r.getCreatedAt()), r.getSupplierName(),
                    r.getUserName(), Formatter.money(r.getTotalAmount()), r.getStatus().getLabel() });
        }
        UiKit.fill(view.tblReceipts, rows);
        UiKit.fill(view.tblReceiptItems, new ArrayList<>());
    }

    /**
     * Hiển thị chi tiết của phiếu đang chọn.
     */
    private void showReceiptItems() {
        Long id = selectedReceiptId();
        if (id == null) {
            return;
        }
        PurchaseReceipt r = purchaseService.getReceiptDetail(id);
        List<Object[]> rows = new ArrayList<>();
        for (PurchaseItem it : r.getItems()) {
            rows.add(new Object[] { it.getSku(), it.getVariantName(), it.getQuantity(),
                    Formatter.money(it.getUnitCost()), Formatter.money(it.getLineTotal()) });
        }
        UiKit.fill(view.tblReceiptItems, rows);
    }

    /**
     * Mở phiếu nháp đang chọn sang tab lập phiếu để sửa (BR04: chỉ phiếu nháp).
     */
    private void editDraft() {
        Long id = requireReceipt();
        PurchaseReceipt r = purchaseService.getReceiptDetail(id);
        if (r.getStatus() != ReceiptStatus.DRAFT) {
            throw new BusinessException("Chỉ sửa được phiếu ở trạng thái Nháp (BR04).");
        }
        editingReceiptId = r.getId();
        lines.clear();
        lines.addAll(r.getItems());
        for (int i = 0; i < view.cboSupplier.getItemCount(); i++) {
            if (view.cboSupplier.getItemAt(i).getId().equals(r.getSupplierId())) {
                view.cboSupplier.setSelectedIndex(i);
            }
        }
        renderLines();
        view.tabs.setSelectedIndex(0);
    }

    /**
     * Xác nhận phiếu đang chọn: tăng tồn kho trong một giao dịch (BR04).
     */
    private void confirmSelected() {
        Long id = requireReceipt();
        if (!UiKit.confirm(view, "Xác nhận nhập kho phiếu #" + id + "? Sau khi xác nhận, phiếu không thể sửa.")) {
            return;
        }
        purchaseService.confirmReceipt(id);
        UiKit.info(view, "Đã xác nhận phiếu #" + id + ".");
        if (id.equals(editingReceiptId)) {
            newReceipt();
        }
        searchVariants();
        loadReceipts();
    }

    /**
     * Hủy phiếu nháp đang chọn.
     */
    private void cancelSelected() {
        Long id = requireReceipt();
        if (!UiKit.confirm(view, "Hủy phiếu nhập #" + id + "?")) {
            return;
        }
        purchaseService.cancelReceipt(id);
        UiKit.info(view, "Đã hủy phiếu #" + id + ".");
        if (id.equals(editingReceiptId)) {
            newReceipt();
        }
        loadReceipts();
    }

    /**
     * Biến thể đang chọn ở bảng tìm kiếm.
     *
     * @return biến thể hoặc null
     */
    private ProductVariant selectedVariant() {
        int row = UiKit.selectedModelRow(view.tblVariants);
        if (row < 0) {
            return null;
        }
        Long id = (Long) view.tblVariants.getModel().getValueAt(row, 0);
        for (ProductVariant v : variants) {
            if (v.getId().equals(id)) {
                return v;
            }
        }
        return null;
    }

    /**
     * Mã phiếu đang chọn ở bảng danh sách.
     *
     * @return mã phiếu hoặc null
     */
    private Long selectedReceiptId() {
        int row = UiKit.selectedModelRow(view.tblReceipts);
        return row < 0 ? null : (Long) view.tblReceipts.getModel().getValueAt(row, 0);
    }

    /**
     * Mã phiếu đang chọn, bắt buộc phải có.
     *
     * @return mã phiếu
     * @throws BusinessException nếu chưa chọn
     */
    private Long requireReceipt() {
        Long id = selectedReceiptId();
        if (id == null) {
            throw new BusinessException("Hãy chọn một phiếu nhập.");
        }
        return id;
    }
}

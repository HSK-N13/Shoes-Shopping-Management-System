package services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dao.ProductVariantDAO;
import dao.PurchaseItemDAO;
import dao.PurchaseReceiptDAO;
import dao.StockMovementDAO;
import model.MovementType;
import model.PurchaseItem;
import model.PurchaseReceipt;
import model.ReceiptStatus;
import model.StockMovement;
import model.User;
import utils.BusinessException;
import utils.Session;
import utils.Transaction;

/**
 * Nghiệp vụ nhập hàng (mục 2.2, BR03, BR04, BR07).
 * Luồng: lập phiếu DRAFT -> (sửa) -> xác nhận CONFIRMED (tăng tồn) hoặc hủy CANCELLED.
 */
public class PurchaseService extends BaseService {
    /** Loại chứng từ ghi vào stock_movements.ref_type. */
    public static final String REF_TYPE = "PURCHASE_RECEIPT";

    private final PurchaseReceiptDAO purchaseReceiptDAO = new PurchaseReceiptDAO();
    private final PurchaseItemDAO purchaseItemDAO = new PurchaseItemDAO();
    private final ProductVariantDAO productVariantDAO = new ProductVariantDAO();
    private final StockMovementDAO stockMovementDAO = new StockMovementDAO();

    /**
     * Lập phiếu nhập mới ở trạng thái DRAFT. Tồn kho CHƯA thay đổi cho tới khi xác nhận (BR04).
     *
     * @param supplierId mã nhà cung cấp
     * @param items      danh sách dòng nhập (biến thể, số lượng, giá nhập)
     * @return phiếu nhập đã lưu
     */
    public PurchaseReceipt createPurchaseReceipt(Long supplierId, List<PurchaseItem> items) {
        User user = Session.requireLogin();
        BigDecimal total = validate(supplierId, items);
        PurchaseReceipt receipt = new PurchaseReceipt();
        receipt.setSupplierId(supplierId);
        receipt.setUserId(user.getId());
        receipt.setStatus(ReceiptStatus.DRAFT);
        receipt.setTotalAmount(total);
        receipt.setCreatedAt(LocalDateTime.now());
        return Transaction.execute(conn -> {
            purchaseReceiptDAO.save(receipt);
            purchaseItemDAO.saveAll(receipt.getId(), items);
            receipt.setItems(items);
            return receipt;
        });
    }

    /**
     * Sửa phiếu nhập còn ở trạng thái DRAFT (thay toàn bộ dòng chi tiết).
     * Phiếu đã xác nhận không được sửa (BR04).
     *
     * @param receiptId  mã phiếu
     * @param supplierId nhà cung cấp mới
     * @param items      danh sách dòng mới
     * @return phiếu nhập đã cập nhật
     */
    public PurchaseReceipt updateDraft(Long receiptId, Long supplierId, List<PurchaseItem> items) {
        Session.requireLogin();
        BigDecimal total = validate(supplierId, items);
        return Transaction.execute(conn -> {
            PurchaseReceipt r = purchaseReceiptDAO.findById(receiptId);
            if (r == null) {
                throw new BusinessException("Không tìm thấy phiếu nhập #" + receiptId + ".");
            }
            r.setSupplierId(supplierId);
            r.setTotalAmount(total);
            if (!purchaseReceiptDAO.updateDraft(r)) {
                throw new BusinessException("Phiếu đã xác nhận hoặc đã hủy, không được sửa (BR04).");
            }
            purchaseItemDAO.deleteByReceiptId(receiptId);
            purchaseItemDAO.saveAll(receiptId, items);
            r.setItems(items);
            return r;
        });
    }

    /**
     * Xác nhận phiếu nhập trong MỘT giao dịch: chuyển DRAFT -> CONFIRMED (chỉ một lần),
     * tăng tồn, cập nhật giá vốn = giá nhập gần nhất, ghi StockMovement.
     * Lỗi ở bất kỳ bước nào thì hoàn tác toàn bộ (BR03, BR04, BR07).
     *
     * @param receiptId mã phiếu
     */
    public void confirmReceipt(Long receiptId) {
        Session.requireLogin();
        Transaction.execute(conn -> {
            // Đổi trạng thái có điều kiện trước: nếu phiếu đã xác nhận/hủy thì 0 dòng -> báo lỗi
            if (!purchaseReceiptDAO.changeStatus(receiptId, ReceiptStatus.DRAFT, ReceiptStatus.CONFIRMED)) {
                throw new BusinessException("Phiếu không ở trạng thái Nháp; mỗi phiếu chỉ được xác nhận một lần (BR04).");
            }
            List<PurchaseItem> items = purchaseItemDAO.findByReceiptId(receiptId);
            if (items.isEmpty()) {
                throw new BusinessException("Phiếu nhập không có dòng hàng nào.");
            }
            for (PurchaseItem item : items) {
                if (!productVariantDAO.increaseStock(item.getVariantId(), item.getQuantity())) {
                    throw new BusinessException("Không tìm thấy biến thể " + item.getSku() + ".");
                }
                productVariantDAO.updateCostPrice(item.getVariantId(), item.getUnitCost());
                stockMovementDAO.record(item.getVariantId(), MovementType.PURCHASE, item.getQuantity(),
                        REF_TYPE, receiptId);
            }
            return null;
        });
    }

    /**
     * Hủy phiếu nhập còn ở trạng thái DRAFT. Phiếu đã xác nhận không thể hủy;
     * nếu sai phải lập điều chỉnh bằng kiểm kê (BR04).
     *
     * @param receiptId mã phiếu
     */
    public void cancelReceipt(Long receiptId) {
        Session.requireLogin();
        if (!db(() -> purchaseReceiptDAO.changeStatus(receiptId, ReceiptStatus.DRAFT, ReceiptStatus.CANCELLED))) {
            throw new BusinessException("Chỉ hủy được phiếu đang ở trạng thái Nháp. "
                    + "Phiếu đã xác nhận cần điều chỉnh bằng kiểm kê (BR04).");
        }
    }

    /**
     * Tìm phiếu nhập theo khoảng ngày và trạng thái.
     *
     * @param from   từ thời điểm (bao gồm), null = không giới hạn
     * @param to     đến thời điểm (không bao gồm), null = không giới hạn
     * @param status trạng thái, null = tất cả
     * @return danh sách phiếu nhập
     */
    public List<PurchaseReceipt> searchReceipts(LocalDateTime from, LocalDateTime to, ReceiptStatus status) {
        Session.requireLogin();
        return db(() -> purchaseReceiptDAO.search(from, to, status));
    }

    /**
     * Lấy phiếu nhập kèm danh sách dòng chi tiết.
     *
     * @param receiptId mã phiếu
     * @return phiếu nhập đầy đủ
     */
    public PurchaseReceipt getReceiptDetail(Long receiptId) {
        Session.requireLogin();
        PurchaseReceipt r = db(() -> purchaseReceiptDAO.findById(receiptId));
        if (r == null) {
            throw new BusinessException("Không tìm thấy phiếu nhập #" + receiptId + ".");
        }
        r.setItems(db(() -> purchaseItemDAO.findByReceiptId(receiptId)));
        return r;
    }

    /**
     * Lịch sử nhập kho (các biến động loại PURCHASE).
     *
     * @return danh sách biến động nhập hàng, mới nhất trước
     */
    public ArrayList<StockMovement> getImportHistory() {
        Session.requireLogin();
        return db(() -> stockMovementDAO.findByType(MovementType.PURCHASE));
    }

    /**
     * Kiểm tra dữ liệu phiếu nhập và tính tổng tiền.
     *
     * @param supplierId mã nhà cung cấp
     * @param items      các dòng nhập
     * @return tổng tiền = tổng (số lượng x giá nhập)
     * @throws BusinessException nếu dữ liệu không hợp lệ
     */
    private BigDecimal validate(Long supplierId, List<PurchaseItem> items) {
        if (supplierId == null) {
            throw new BusinessException("Nhà cung cấp không được để trống!");
        }
        if (items == null || items.isEmpty()) {
            throw new BusinessException("Danh sách mặt hàng nhập không được để trống!");
        }
        BigDecimal total = BigDecimal.ZERO;
        Set<Long> seen = new HashSet<>();
        for (PurchaseItem item : items) {
            if (item.getVariantId() == null) {
                throw new BusinessException("Dòng nhập chưa chọn biến thể.");
            }
            if (!seen.add(item.getVariantId())) {
                throw new BusinessException("Biến thể " + item.getSku() + " bị lặp; hãy gộp vào một dòng.");
            }
            if (item.getQuantity() <= 0) {
                throw new BusinessException("Số lượng nhập phải lớn hơn 0!");
            }
            if (item.getUnitCost() == null || item.getUnitCost().signum() < 0) {
                throw new BusinessException("Đơn giá nhập không được nhỏ hơn 0!");
            }
            total = total.add(item.getLineTotal());
        }
        return total;
    }
}

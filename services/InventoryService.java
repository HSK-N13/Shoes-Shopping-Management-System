package services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import dao.ProductVariantDAO;
import dao.StockMovementDAO;
import model.MovementType;
import model.ProductVariant;
import model.StockMovement;
import utils.BusinessException;
import utils.Session;
import utils.Transaction;

/**
 * Kiểm kê và tra cứu biến động tồn kho (mục 2.2, BR03).
 */
public class InventoryService extends BaseService {
    /** Loại chứng từ ghi vào stock_movements.ref_type khi kiểm kê. */
    public static final String REF_TYPE = "STOCKTAKE";

    private final ProductVariantDAO variantDAO = new ProductVariantDAO();
    private final StockMovementDAO movementDAO = new StockMovementDAO();

    /**
     * Lấy toàn bộ biến thể kèm tồn kho hiện tại (để kiểm kê).
     *
     * @return danh sách biến thể
     */
    public List<ProductVariant> getAllVariants() {
        Session.requireLogin();
        return db(variantDAO::getAll);
    }

    /**
     * Kiểm kê: với mỗi biến thể, nhập số lượng thực tế; hệ thống tạo điều chỉnh
     * chênh lệch (ADJUSTMENT) và đưa tồn về đúng số thực tế. Tất cả chạy trong
     * một giao dịch.
     *
     * @param actualQuantities map mã biến thể -> số lượng thực tế đếm được
     * @return số biến thể có chênh lệch và đã được điều chỉnh
     */
    public int applyStocktake(Map<Long, Integer> actualQuantities) {
        Session.requireLogin();
        if (actualQuantities == null || actualQuantities.isEmpty()) {
            throw new BusinessException("Chưa nhập số lượng thực tế cho biến thể nào.");
        }
        for (Map.Entry<Long, Integer> e : actualQuantities.entrySet()) {
            if (e.getValue() == null || e.getValue() < 0) {
                throw new BusinessException("Số lượng thực tế không được âm (BR03).");
            }
        }
        return Transaction.execute(conn -> {
            int adjusted = 0;
            for (Map.Entry<Long, Integer> e : actualQuantities.entrySet()) {
                // Khóa dòng để đọc tồn chính xác khi máy khác đang bán cùng lúc
                ProductVariant v = variantDAO.getVariantForUpdate(e.getKey());
                if (v == null) {
                    throw new BusinessException("Không tìm thấy biến thể #" + e.getKey() + ".");
                }
                int diff = e.getValue() - v.getStockQuantity();
                if (diff == 0) {
                    continue;
                }
                variantDAO.updateStock(v.getId(), e.getValue());
                movementDAO.record(v.getId(), MovementType.ADJUSTMENT, diff, REF_TYPE, null);
                adjusted++;
            }
            return adjusted;
        });
    }

    /**
     * Tra cứu lịch sử biến động tồn kho.
     *
     * @param variantId mã biến thể (null = tất cả)
     * @param type      loại biến động (null = tất cả)
     * @param from      từ thời điểm (null = không giới hạn)
     * @param to        đến thời điểm (null = không giới hạn)
     * @return danh sách biến động, mới nhất trước
     */
    public List<StockMovement> getMovements(Long variantId, MovementType type, LocalDateTime from, LocalDateTime to) {
        Session.requireLogin();
        return db(() -> movementDAO.search(variantId, type, from, to));
    }

    /**
     * Đối soát tồn kho với tổng lịch sử biến động (mục 5 của đặc tả).
     *
     * @return map SKU -> [tồn hiện tại, tổng biến động] của các biến thể bị lệch
     */
    public Map<String, int[]> reconcile() {
        Session.requireLogin();
        return db(movementDAO::findMismatches);
    }
}

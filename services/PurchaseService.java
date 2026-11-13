package services;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import dao.ProductVariantDAO;
import dao.PurchaseItemDAO;
import dao.PurchaseReceiptDAO;
import dao.StockMovementDAO;
import model.MovementType;
import model.PurchaseItem;
import model.PurchaseReceipt;
import model.ReceiptStatus;
import model.StockMovement;

public class PurchaseService {
	private final PurchaseReceiptDAO purchaseReceiptDAO = new PurchaseReceiptDAO();
    private final PurchaseItemDAO purchaseItemDAO = new PurchaseItemDAO();
    private final ProductVariantDAO productVariantDAO = new ProductVariantDAO();
    private final StockMovementDAO stockMovementDAO = new StockMovementDAO();

	public PurchaseReceipt createPurchaseReceipt(Long supplierId, Long userId, List<PurchaseItem> items, String note) throws SQLException {
		if (supplierId == null) {
            throw new IllegalArgumentException("Nhà cung cấp không được để trống!");
        }
        if (userId == null) {
            throw new IllegalArgumentException("Thông tin người lập phiếu không hợp lệ!");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Danh sách mặt hàng nhập không được để trống!");
        }
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        for (PurchaseItem item : items) {
            if (item.getQuantity() <= 0) {
                throw new IllegalArgumentException("Số lượng nhập phải lớn hơn 0!");
            }
            if (item.getUnitCost() == null || item.getUnitCost().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Đơn giá nhập không được nhỏ hơn 0!");
            }
            // Tính tổng tiền từng món = unitPrice * quantity
            BigDecimal itemTotal = item.getUnitCost().multiply(BigDecimal.valueOf(item.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);
        }
        
        PurchaseReceipt receipt = new PurchaseReceipt();
        receipt.setSupplierId(supplierId);
        receipt.setUserId(userId);
        receipt.setStatus(ReceiptStatus.CONFIRMED);
        receipt.setTotalAmount(totalAmount);
        receipt.setCreatedAt(LocalDateTime.now());
        
        
        PurchaseReceipt savedReceipt = purchaseReceiptDAO.save(receipt);
        
        for (PurchaseItem item : items) {
        		item.setReceiptId(savedReceipt.getId());
            purchaseItemDAO.save(item);
            
            int currentStock = productVariantDAO.findStockQuantityById(item.getVariantId());
            int newStock = currentStock + item.getQuantity();
            
            productVariantDAO.updateStock(item.getVariantId(), newStock);
            
         // Ghi vết biến động kho (StockMovement)
            StockMovement movement = new StockMovement();
            movement.setVariantId(item.getVariantId());        
            movement.setType(MovementType.PURCHASE);             
            movement.setQuantityChange(item.getQuantity());       
            movement.setRefType("PURCHASE_RECEIPT");             
            movement.setRefId(savedReceipt.getId());            
            movement.setCreatedAt(LocalDateTime.now());           

            stockMovementDAO.save(movement);
        }
        savedReceipt.setItems(items);
        return savedReceipt;
	}
	
	public ArrayList<StockMovement> getImportHistory() {
		ArrayList<StockMovement> stockMovements = new ArrayList<StockMovement>();
        try {
        		stockMovements =stockMovementDAO.findByType(MovementType.PURCHASE);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return stockMovements;
    }
	
	
	
}
package services;

import java.math.BigDecimal;
import java.util.List;

import dao.ProductDAO;
import dao.ProductVariantDAO;
import model.Product;
import model.ProductVariant;
import utils.BusinessException;
import utils.Session;
import utils.Transaction;

/**
 * Nghiệp vụ sản phẩm và biến thể (mục 2.1, BR01, BR02).
 * Tra cứu: mọi người dùng; thêm/sửa/ngừng kinh doanh, đổi giá: chỉ Admin (BR12).
 */
public class ProductService extends BaseService {
    private final ProductDAO productDAO = new ProductDAO();
    private final ProductVariantDAO variantDAO = new ProductVariantDAO();

    /**
     * Tìm sản phẩm theo từ khóa và bộ lọc.
     *
     * @param keyword    từ khóa (tên, thương hiệu, SKU, size, màu)
     * @param categoryId lọc theo danh mục (null = tất cả)
     * @param brandId    lọc theo thương hiệu (null = tất cả)
     * @param active     lọc theo trạng thái (null = tất cả)
     * @return danh sách sản phẩm
     */
    public List<Product> searchProducts(String keyword, Long categoryId, Long brandId, Boolean active) {
        Session.requireLogin();
        return db(() -> productDAO.searchProducts(keyword, categoryId, brandId, active));
    }

    /**
     * Lấy toàn bộ biến thể của một sản phẩm.
     *
     * @param productId mã sản phẩm
     * @return danh sách biến thể
     */
    public List<ProductVariant> getVariants(Long productId) {
        Session.requireLogin();
        return db(() -> variantDAO.getVariantsByProductId(productId));
    }

    /**
     * Tìm biến thể theo tên/SKU/thương hiệu, size, màu (mục 2.3).
     *
     * @param keyword    từ khóa
     * @param size       size
     * @param color      màu
     * @param onlyActive true để chỉ lấy hàng đang kinh doanh
     * @return danh sách biến thể
     */
    public List<ProductVariant> searchVariants(String keyword, String size, String color, boolean onlyActive) {
        Session.requireLogin();
        return db(() -> variantDAO.search(keyword, size, color, onlyActive));
    }

    /**
     * Lấy một biến thể theo mã.
     *
     * @param id mã biến thể
     * @return biến thể hoặc null
     */
    public ProductVariant getVariant(Long id) {
        Session.requireLogin();
        return db(() -> variantDAO.getVariantById(id));
    }

    /**
     * Thêm mới (id null) hoặc cập nhật sản phẩm.
     *
     * @param p sản phẩm cần lưu
     * @return sản phẩm đã lưu
     */
    public Product saveProduct(Product p) {
        Session.requireAdmin();
        p.setName(required(p.getName(), "Tên sản phẩm"));
        p.setDescription(optional(p.getDescription()));
        if (p.getCategoryId() == null) {
            throw new BusinessException("Phải chọn danh mục.");
        }
        if (p.getBrandId() == null) {
            throw new BusinessException("Phải chọn thương hiệu.");
        }
        if (p.getId() == null) {
            db(() -> productDAO.insertProduct(p));
        } else {
            Transaction.execute(conn -> {
                productDAO.updateProduct(p);
                if (!p.isActive()) {
                    // Ngừng kinh doanh mẫu giày thì ngừng luôn mọi biến thể (BR02)
                    variantDAO.deactivateByProduct(p.getId());
                }
                return null;
            });
        }
        return p;
    }

    /**
     * Xóa sản phẩm. Nếu sản phẩm đã phát sinh giao dịch thì chỉ chuyển sang
     * ngừng kinh doanh (BR02).
     *
     * @param productId mã sản phẩm
     * @return true nếu đã xóa hẳn, false nếu chỉ ngừng kinh doanh
     */
    public boolean deleteProduct(Long productId) {
        Session.requireAdmin();
        return Transaction.execute(conn -> {
            if (productDAO.hasTransactions(productId)) {
                productDAO.updateProductStatus(productId, false);
                variantDAO.deactivateByProduct(productId);
                return false;
            }
            productDAO.deleteProduct(productId);
            return true;
        });
    }

    /**
     * Thêm mới (id null) hoặc cập nhật biến thể; kiểm tra SKU duy nhất và tổ hợp
     * size + màu không trùng trong cùng sản phẩm (BR01).
     *
     * @param v biến thể cần lưu
     * @return biến thể đã lưu
     */
    public ProductVariant saveVariant(ProductVariant v) {
        Session.requireAdmin();
        if (v.getProductId() == null) {
            throw new BusinessException("Phải chọn sản phẩm trước khi thêm biến thể.");
        }
        v.setSku(required(v.getSku(), "SKU").toUpperCase());
        v.setSize(required(v.getSize(), "Size"));
        v.setColor(required(v.getColor(), "Màu"));
        nonNegative(v.getSellingPrice(), "Giá bán");
        if (v.getSellingPrice().signum() == 0) {
            throw new BusinessException("Giá bán phải lớn hơn 0.");
        }
        if (v.getMinStock() < 0) {
            throw new BusinessException("Ngưỡng tồn tối thiểu không được âm.");
        }
        if (db(() -> variantDAO.existsSku(v.getSku(), v.getId()))) {
            throw new BusinessException("SKU '" + v.getSku() + "' đã tồn tại (BR01).");
        }
        if (db(() -> variantDAO.existsSizeColor(v.getProductId(), v.getSize(), v.getColor(), v.getId()))) {
            throw new BusinessException("Sản phẩm đã có biến thể size " + v.getSize() + " màu " + v.getColor()
                    + " (BR01).");
        }
        if (v.isActive()) {
            Product p = db(() -> productDAO.getProductById(v.getProductId()));
            if (p != null && !p.isActive()) {
                throw new BusinessException("Mẫu giày đang ngừng kinh doanh, không thể bật bán biến thể.");
            }
        }
        if (v.getId() == null) {
            if (v.getCostPrice() == null) {
                v.setCostPrice(BigDecimal.ZERO);
            }
            db(() -> variantDAO.insertVariant(v));
        } else {
            db(() -> variantDAO.updateVariant(v));
        }
        return v;
    }

    /**
     * Xóa biến thể; nếu đã có giao dịch thì chỉ ngừng kinh doanh (BR02).
     *
     * @param variantId mã biến thể
     * @return true nếu đã xóa hẳn, false nếu chỉ ngừng kinh doanh
     */
    public boolean deleteVariant(Long variantId) {
        Session.requireAdmin();
        if (db(() -> variantDAO.hasTransactions(variantId))) {
            db(() -> variantDAO.updateVariantStatus(variantId, false));
            return false;
        }
        db(() -> variantDAO.deleteVariant(variantId));
        return true;
    }
}

package services;

import java.util.List;

import dao.BrandDAO;
import dao.CategoryDAO;
import model.Brand;
import model.Category;
import utils.BusinessException;
import utils.Session;

/**
 * Quản lý danh mục và thương hiệu (mục 2.1). Xem: mọi người dùng; thêm/sửa/xóa: chỉ Admin (BR12).
 */
public class CatalogService extends BaseService {
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final BrandDAO brandDAO = new BrandDAO();

    /**
     * Lấy danh mục.
     *
     * @param onlyActive true để chỉ lấy danh mục đang dùng
     * @return danh sách danh mục
     */
    public List<Category> getCategories(boolean onlyActive) {
        Session.requireLogin();
        return onlyActive ? db(categoryDAO::getActiveCategories) : db(categoryDAO::getAllCategory);
    }

    /**
     * Lấy thương hiệu.
     *
     * @param onlyActive true để chỉ lấy thương hiệu đang dùng
     * @return danh sách thương hiệu
     */
    public List<Brand> getBrands(boolean onlyActive) {
        Session.requireLogin();
        return onlyActive ? db(brandDAO::getActiveBrands) : db(brandDAO::getAllBrand);
    }

    /**
     * Thêm hoặc sửa danh mục (id null = thêm mới). Tên không được trùng.
     *
     * @param id     mã danh mục (null khi thêm)
     * @param name   tên danh mục
     * @param active trạng thái sử dụng
     * @return danh mục đã lưu
     */
    public Category saveCategory(Long id, String name, boolean active) {
        Session.requireAdmin();
        String n = required(name, "Tên danh mục");
        if (db(() -> categoryDAO.existsByName(n, id))) {
            throw new BusinessException("Danh mục '" + n + "' đã tồn tại.");
        }
        Category c = new Category();
        c.setId(id);
        c.setName(n);
        c.setActive(active);
        if (id == null) {
            db(() -> categoryDAO.insertCategory(c));
        } else {
            db(() -> categoryDAO.updateCategory(c));
        }
        return c;
    }

    /**
     * Xóa danh mục nếu chưa có sản phẩm; ngược lại chuyển sang ngừng sử dụng.
     *
     * @param id mã danh mục
     * @return true nếu đã xóa hẳn, false nếu chỉ ngừng sử dụng
     */
    public boolean deleteCategory(Long id) {
        Session.requireAdmin();
        if (db(() -> categoryDAO.countProducts(id)) > 0) {
            db(() -> categoryDAO.updateCategoryStatus(id, false));
            return false;
        }
        db(() -> categoryDAO.deleteCategory(id));
        return true;
    }

    /**
     * Thêm hoặc sửa thương hiệu (id null = thêm mới). Tên không được trùng.
     *
     * @param id     mã thương hiệu (null khi thêm)
     * @param name   tên thương hiệu
     * @param active trạng thái sử dụng
     * @return thương hiệu đã lưu
     */
    public Brand saveBrand(Long id, String name, boolean active) {
        Session.requireAdmin();
        String n = required(name, "Tên thương hiệu");
        if (db(() -> brandDAO.existsByName(n, id))) {
            throw new BusinessException("Thương hiệu '" + n + "' đã tồn tại.");
        }
        Brand b = new Brand();
        b.setId(id);
        b.setName(n);
        b.setActive(active);
        if (id == null) {
            db(() -> brandDAO.insertBrand(b));
        } else {
            db(() -> brandDAO.updateBrand(b));
        }
        return b;
    }

    /**
     * Xóa thương hiệu nếu chưa có sản phẩm; ngược lại chuyển sang ngừng sử dụng.
     *
     * @param id mã thương hiệu
     * @return true nếu đã xóa hẳn, false nếu chỉ ngừng sử dụng
     */
    public boolean deleteBrand(Long id) {
        Session.requireAdmin();
        if (db(() -> brandDAO.countProducts(id)) > 0) {
            db(() -> brandDAO.updateBrandStatus(id, false));
            return false;
        }
        db(() -> brandDAO.deleteBrand(id));
        return true;
    }
}

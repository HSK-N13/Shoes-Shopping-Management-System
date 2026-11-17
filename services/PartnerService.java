package services;

import java.util.List;

import dao.CustomerDAO;
import dao.SupplierDAO;
import model.Customer;
import model.Supplier;
import utils.BusinessException;
import utils.Session;

/**
 * Quản lý nhà cung cấp (mục 2.2) và khách hàng (mục 2.3).
 * Nhân viên và Admin đều được dùng.
 */
public class PartnerService extends BaseService {
    private final SupplierDAO supplierDAO = new SupplierDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();

    /**
     * Tìm nhà cung cấp theo tên/SĐT.
     *
     * @param keyword từ khóa (rỗng = tất cả)
     * @return danh sách nhà cung cấp
     */
    public List<Supplier> searchSuppliers(String keyword) {
        Session.requireLogin();
        return db(() -> supplierDAO.search(keyword));
    }

    /**
     * Thêm (id null) hoặc sửa nhà cung cấp.
     *
     * @param id    mã nhà cung cấp
     * @param name  tên
     * @param phone số điện thoại (tùy chọn)
     * @return nhà cung cấp đã lưu
     */
    public Supplier saveSupplier(Long id, String name, String phone) {
        Session.requireLogin();
        Supplier s = new Supplier();
        s.setId(id);
        s.setName(required(name, "Tên nhà cung cấp"));
        s.setPhone(phone(phone));
        if (id == null) {
            db(() -> supplierDAO.insert(s));
        } else {
            db(() -> supplierDAO.update(s));
        }
        return s;
    }

    /**
     * Xóa nhà cung cấp chưa có phiếu nhập.
     *
     * @param id mã nhà cung cấp
     * @throws BusinessException nếu đã có phiếu nhập
     */
    public void deleteSupplier(Long id) {
        Session.requireLogin();
        if (db(() -> supplierDAO.hasReceipts(id))) {
            throw new BusinessException("Nhà cung cấp đã có phiếu nhập, không thể xóa.");
        }
        db(() -> supplierDAO.delete(id));
    }

    /**
     * Tìm khách hàng theo tên/SĐT.
     *
     * @param keyword từ khóa (rỗng = tất cả)
     * @return danh sách khách hàng
     */
    public List<Customer> searchCustomers(String keyword) {
        Session.requireLogin();
        return db(() -> customerDAO.search(keyword));
    }

    /**
     * Thêm (id null) hoặc sửa khách hàng. Số điện thoại không được trùng khách khác.
     *
     * @param id       mã khách hàng
     * @param fullName họ tên
     * @param phone    số điện thoại (tùy chọn)
     * @return khách hàng đã lưu
     */
    public Customer saveCustomer(Long id, String fullName, String phone) {
        Session.requireLogin();
        Customer c = new Customer();
        c.setId(id);
        c.setFullName(required(fullName, "Họ tên khách hàng"));
        c.setPhone(phone(phone));
        if (c.getPhone() != null) {
            Customer other = db(() -> customerDAO.findByPhone(c.getPhone()));
            if (other != null && !other.getId().equals(id)) {
                throw new BusinessException("Số điện thoại đã thuộc khách hàng " + other.getFullName() + ".");
            }
        }
        if (id == null) {
            db(() -> customerDAO.insert(c));
        } else {
            db(() -> customerDAO.update(c));
        }
        return c;
    }

    /**
     * Xóa khách hàng chưa có đơn hàng.
     *
     * @param id mã khách hàng
     * @throws BusinessException nếu đã có đơn hàng
     */
    public void deleteCustomer(Long id) {
        Session.requireLogin();
        if (db(() -> customerDAO.hasOrders(id))) {
            throw new BusinessException("Khách hàng đã có đơn hàng, không thể xóa.");
        }
        db(() -> customerDAO.delete(id));
    }
}

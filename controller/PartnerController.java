package controller;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import model.Customer;
import model.Supplier;
import services.PartnerService;
import view.SimpleCrudPanel;
import view.UiKit;

/**
 * Xử lý màn hình nhà cung cấp hoặc khách hàng (chọn bằng {@link Kind}).
 * Nhân viên và Admin đều được dùng.
 */
public class PartnerController extends BaseController {

    /**
     * Loại đối tác mà controller quản lý.
     */
    public enum Kind {
        /** Nhà cung cấp. */
        SUPPLIER,
        /** Khách hàng. */
        CUSTOMER
    }

    private final SimpleCrudPanel view;
    private final Kind kind;
    private final PartnerService service = new PartnerService();
    private Long selectedId;

    /**
     * Gắn controller vào màn hình và đăng ký sự kiện.
     *
     * @param view màn hình CRUD
     * @param kind loại đối tác
     */
    public PartnerController(SimpleCrudPanel view, Kind kind) {
        this.view = view;
        this.kind = kind;
        view.btnSearch.addActionListener(e -> handle(this::refresh));
        view.txtKeyword.addActionListener(e -> handle(this::refresh));
        view.btnNew.addActionListener(e -> clear());
        view.btnSave.addActionListener(e -> handle(this::save));
        view.btnDelete.addActionListener(e -> handle(this::delete));
        view.table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onSelected();
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
     * Nạp lại danh sách theo từ khóa tìm kiếm.
     */
    public void refresh() {
        List<Object[]> rows = new ArrayList<>();
        if (kind == Kind.SUPPLIER) {
            for (Supplier s : service.searchSuppliers(view.txtKeyword.getText())) {
                rows.add(new Object[] { s.getId(), s.getName(), s.getPhone() });
            }
        } else {
            for (Customer c : service.searchCustomers(view.txtKeyword.getText())) {
                rows.add(new Object[] { c.getId(), c.getFullName(), c.getPhone() });
            }
        }
        UiKit.fill(view.table, rows);
        clear();
    }

    /**
     * Khi chọn một dòng: đổ dữ liệu lên form.
     */
    private void onSelected() {
        int row = UiKit.selectedModelRow(view.table);
        if (row < 0) {
            return;
        }
        selectedId = (Long) view.table.getModel().getValueAt(row, 0);
        view.txtField1.setText((String) view.table.getModel().getValueAt(row, 1));
        Object phone = view.table.getModel().getValueAt(row, 2);
        view.txtField2.setText(phone == null ? "" : phone.toString());
    }

    /**
     * Lưu (thêm mới hoặc cập nhật) bản ghi trên form.
     */
    private void save() {
        if (kind == Kind.SUPPLIER) {
            service.saveSupplier(selectedId, view.txtField1.getText(), view.txtField2.getText());
        } else {
            service.saveCustomer(selectedId, view.txtField1.getText(), view.txtField2.getText());
        }
        UiKit.info(view, "Đã lưu.");
        refresh();
    }

    /**
     * Xóa bản ghi đang chọn (chỉ khi chưa có chứng từ liên quan).
     */
    private void delete() {
        if (selectedId == null) {
            UiKit.error(view, "Hãy chọn dòng cần xóa.");
            return;
        }
        if (!UiKit.confirm(view, "Xóa bản ghi này?")) {
            return;
        }
        if (kind == Kind.SUPPLIER) {
            service.deleteSupplier(selectedId);
        } else {
            service.deleteCustomer(selectedId);
        }
        UiKit.info(view, "Đã xóa.");
        refresh();
    }

    /**
     * Xóa trắng form để thêm mới.
     */
    private void clear() {
        selectedId = null;
        view.table.clearSelection();
        view.txtField1.setText("");
        view.txtField2.setText("");
    }
}

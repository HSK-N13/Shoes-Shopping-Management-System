package controller;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import model.Brand;
import model.Category;
import services.CatalogService;
import view.SimpleCrudPanel;
import view.UiKit;

/**
 * Xử lý màn hình danh mục hoặc thương hiệu (một controller cho mỗi bảng, chọn bằng {@link Kind}).
 * Chỉ Admin được mở màn hình này (BR12).
 */
public class CatalogController extends BaseController {

    /**
     * Loại dữ liệu mà controller quản lý.
     */
    public enum Kind {
        /** Danh mục. */
        CATEGORY,
        /** Thương hiệu. */
        BRAND
    }

    private final SimpleCrudPanel view;
    private final Kind kind;
    private final CatalogService service = new CatalogService();
    private Long selectedId;

    /**
     * Gắn controller vào màn hình và đăng ký sự kiện.
     *
     * @param view màn hình CRUD
     * @param kind loại dữ liệu (danh mục hay thương hiệu)
     */
    public CatalogController(SimpleCrudPanel view, Kind kind) {
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
     * Nạp lại danh sách, có lọc theo từ khóa.
     */
    public void refresh() {
        String kw = view.txtKeyword.getText().trim().toLowerCase();
        List<Object[]> rows = new ArrayList<>();
        if (kind == Kind.CATEGORY) {
            for (Category c : service.getCategories(false)) {
                if (c.getName().toLowerCase().contains(kw)) {
                    rows.add(new Object[] { c.getId(), c.getName(), c.isActive() ? "Đang dùng" : "Ngừng dùng" });
                }
            }
        } else {
            for (Brand b : service.getBrands(false)) {
                if (b.getName().toLowerCase().contains(kw)) {
                    rows.add(new Object[] { b.getId(), b.getName(), b.isActive() ? "Đang dùng" : "Ngừng dùng" });
                }
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
        view.chkActive.setSelected("Đang dùng".equals(view.table.getModel().getValueAt(row, 2)));
    }

    /**
     * Lưu (thêm mới hoặc cập nhật) bản ghi trên form.
     */
    private void save() {
        if (kind == Kind.CATEGORY) {
            service.saveCategory(selectedId, view.txtField1.getText(), view.chkActive.isSelected());
        } else {
            service.saveBrand(selectedId, view.txtField1.getText(), view.chkActive.isSelected());
        }
        UiKit.info(view, "Đã lưu.");
        refresh();
    }

    /**
     * Xóa bản ghi đang chọn; nếu đã có sản phẩm thì chuyển sang ngừng dùng.
     */
    private void delete() {
        if (selectedId == null) {
            UiKit.error(view, "Hãy chọn dòng cần xóa.");
            return;
        }
        if (!UiKit.confirm(view, "Xóa bản ghi này?")) {
            return;
        }
        boolean deleted = kind == Kind.CATEGORY ? service.deleteCategory(selectedId) : service.deleteBrand(selectedId);
        UiKit.info(view, deleted ? "Đã xóa." : "Đã có sản phẩm sử dụng nên chỉ chuyển sang ngừng dùng.");
        refresh();
    }

    /**
     * Xóa trắng form để thêm mới.
     */
    private void clear() {
        selectedId = null;
        view.table.clearSelection();
        view.txtField1.setText("");
        view.chkActive.setSelected(true);
    }
}

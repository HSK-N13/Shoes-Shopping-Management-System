package controller;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;

import model.Brand;
import model.Category;
import model.Product;
import model.ProductVariant;
import services.CatalogService;
import services.ProductService;
import utils.Formatter;
import utils.Session;
import view.ProductPanel;
import view.UiKit;

/**
 * Xử lý sự kiện màn hình sản phẩm: tìm kiếm, chọn sản phẩm/biến thể, lưu, xóa.
 */
public class ProductController extends BaseController {
    private static final String ALL = "-- Tất cả --";

    private final ProductPanel view;
    private final ProductService productService = new ProductService();
    private final CatalogService catalogService = new CatalogService();
    private List<Product> products = new ArrayList<>();
    private List<ProductVariant> variants = new ArrayList<>();
    private Long selectedProductId;
    private Long selectedVariantId;

    /**
     * Gắn controller vào màn hình và đăng ký sự kiện.
     *
     * @param view màn hình sản phẩm
     */
    public ProductController(ProductPanel view) {
        this.view = view;
        view.setAdminMode(Session.isAdmin());
        view.btnSearch.addActionListener(e -> handle(this::search));
        view.txtKeyword.addActionListener(e -> handle(this::search));
        view.tblProducts.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                handle(this::onProductSelected);
            }
        });
        view.tblVariants.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onVariantSelected();
            }
        });
        view.btnNewProduct.addActionListener(e -> clearProductForm());
        view.btnSaveProduct.addActionListener(e -> handle(this::saveProduct));
        view.btnDeleteProduct.addActionListener(e -> handle(this::deleteProduct));
        view.btnNewVariant.addActionListener(e -> clearVariantForm());
        view.btnSaveVariant.addActionListener(e -> handle(this::saveVariant));
        view.btnDeleteVariant.addActionListener(e -> handle(this::deleteVariant));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Component parent() {
        return view;
    }

    /**
     * Nạp lại danh mục, thương hiệu và danh sách sản phẩm (gọi khi mở tab).
     */
    public void refresh() {
        handle(() -> {
            List<Category> cats = catalogService.getCategories(false);
            List<Brand> brands = catalogService.getBrands(false);
            fillFilter(view.cboFilterCategory, cats);
            fillFilter(view.cboFilterBrand, brands);
            fillCombo(view.cboCategory, catalogService.getCategories(true));
            fillCombo(view.cboBrand, catalogService.getBrands(true));
            search();
        });
    }

    /**
     * Tìm sản phẩm theo từ khóa và bộ lọc rồi hiển thị lên bảng.
     */
    private void search() {
        Long catId = idOf(view.cboFilterCategory.getSelectedItem());
        Long brandId = idOf(view.cboFilterBrand.getSelectedItem());
        Boolean active = null;
        if (view.cboFilterStatus.getSelectedIndex() == 1) {
            active = Boolean.TRUE;
        } else if (view.cboFilterStatus.getSelectedIndex() == 2) {
            active = Boolean.FALSE;
        }
        products = productService.searchProducts(view.txtKeyword.getText(), catId, brandId, active);
        List<Object[]> rows = new ArrayList<>();
        for (Product p : products) {
            rows.add(new Object[] { p.getId(), p.getName(), p.getCategoryName(), p.getBrandName(),
                    p.isActive() ? "Đang bán" : "Ngừng KD" });
        }
        UiKit.fill(view.tblProducts, rows);
        clearProductForm();
    }

    /**
     * Khi chọn một sản phẩm: đổ dữ liệu lên form và nạp danh sách biến thể.
     */
    private void onProductSelected() {
        int row = UiKit.selectedModelRow(view.tblProducts);
        if (row < 0) {
            return;
        }
        Long id = (Long) view.tblProducts.getModel().getValueAt(row, 0);
        Product p = find(id);
        if (p == null) {
            return;
        }
        selectedProductId = p.getId();
        view.txtName.setText(p.getName());
        view.txtDescription.setText(p.getDescription() == null ? "" : p.getDescription());
        view.chkActive.setSelected(p.isActive());
        selectById(view.cboCategory, p.getCategoryId());
        selectById(view.cboBrand, p.getBrandId());
        loadVariants();
    }

    /**
     * Nạp biến thể của sản phẩm đang chọn lên bảng biến thể.
     */
    private void loadVariants() {
        variants = selectedProductId == null ? new ArrayList<>() : productService.getVariants(selectedProductId);
        List<Object[]> rows = new ArrayList<>();
        for (ProductVariant v : variants) {
            rows.add(new Object[] { v.getId(), v.getSku(), v.getSize(), v.getColor(),
                    Formatter.money(v.getSellingPrice()), Formatter.money(v.getCostPrice()),
                    v.getStockQuantity(), v.getMinStock(), v.isActive() ? "Đang bán" : "Ngừng KD" });
        }
        UiKit.fill(view.tblVariants, rows);
        clearVariantForm();
    }

    /**
     * Khi chọn một biến thể: đổ dữ liệu lên form biến thể.
     */
    private void onVariantSelected() {
        int row = UiKit.selectedModelRow(view.tblVariants);
        if (row < 0) {
            return;
        }
        Long id = (Long) view.tblVariants.getModel().getValueAt(row, 0);
        for (ProductVariant v : variants) {
            if (v.getId().equals(id)) {
                selectedVariantId = v.getId();
                view.txtSku.setText(v.getSku());
                view.txtSize.setText(v.getSize());
                view.txtColor.setText(v.getColor());
                view.txtPrice.setText(Formatter.money(v.getSellingPrice()));
                view.txtMinStock.setText(String.valueOf(v.getMinStock()));
                view.chkVariantActive.setSelected(v.isActive());
            }
        }
    }

    /**
     * Lưu sản phẩm (thêm mới nếu chưa chọn sản phẩm nào).
     */
    private void saveProduct() {
        Product p = new Product();
        p.setId(selectedProductId);
        p.setName(view.txtName.getText());
        p.setDescription(view.txtDescription.getText());
        p.setCategoryId(idOf(view.cboCategory.getSelectedItem()));
        p.setBrandId(idOf(view.cboBrand.getSelectedItem()));
        p.setActive(view.chkActive.isSelected());
        productService.saveProduct(p);
        UiKit.info(view, "Đã lưu sản phẩm.");
        Long keep = p.getId();
        search();
        reselect(keep);
    }

    /**
     * Xóa sản phẩm đang chọn; nếu đã có giao dịch thì chỉ ngừng kinh doanh (BR02).
     */
    private void deleteProduct() {
        if (selectedProductId == null) {
            UiKit.error(view, "Hãy chọn sản phẩm cần xóa.");
            return;
        }
        if (!UiKit.confirm(view, "Xóa sản phẩm này? Nếu đã có giao dịch, sản phẩm sẽ chuyển sang ngừng kinh doanh.")) {
            return;
        }
        boolean deleted = productService.deleteProduct(selectedProductId);
        UiKit.info(view, deleted ? "Đã xóa sản phẩm."
                : "Sản phẩm đã phát sinh giao dịch nên được chuyển sang ngừng kinh doanh (BR02).");
        search();
    }

    /**
     * Lưu biến thể của sản phẩm đang chọn.
     */
    private void saveVariant() {
        if (selectedProductId == null) {
            UiKit.error(view, "Hãy chọn sản phẩm trước.");
            return;
        }
        ProductVariant v = new ProductVariant();
        v.setId(selectedVariantId);
        v.setProductId(selectedProductId);
        v.setSku(view.txtSku.getText());
        v.setSize(view.txtSize.getText());
        v.setColor(view.txtColor.getText());
        v.setSellingPrice(Formatter.parseMoney(view.txtPrice.getText()));
        v.setMinStock(Formatter.parseInt(view.txtMinStock.getText(), "Tồn tối thiểu"));
        v.setActive(view.chkVariantActive.isSelected());
        productService.saveVariant(v);
        UiKit.info(view, "Đã lưu biến thể " + v.getSku() + ".");
        loadVariants();
    }

    /**
     * Xóa biến thể đang chọn; nếu đã có giao dịch thì chỉ ngừng kinh doanh (BR02).
     */
    private void deleteVariant() {
        if (selectedVariantId == null) {
            UiKit.error(view, "Hãy chọn biến thể cần xóa.");
            return;
        }
        if (!UiKit.confirm(view, "Xóa biến thể này? Nếu đã có giao dịch, biến thể sẽ chuyển sang ngừng kinh doanh.")) {
            return;
        }
        boolean deleted = productService.deleteVariant(selectedVariantId);
        UiKit.info(view, deleted ? "Đã xóa biến thể."
                : "Biến thể đã phát sinh giao dịch nên được chuyển sang ngừng kinh doanh (BR02).");
        loadVariants();
    }

    /**
     * Xóa trắng form sản phẩm và bảng biến thể để thêm sản phẩm mới.
     */
    private void clearProductForm() {
        selectedProductId = null;
        view.tblProducts.clearSelection();
        view.txtName.setText("");
        view.txtDescription.setText("");
        view.chkActive.setSelected(true);
        variants = new ArrayList<>();
        UiKit.fill(view.tblVariants, new ArrayList<>());
        clearVariantForm();
    }

    /**
     * Xóa trắng form biến thể để thêm biến thể mới.
     */
    private void clearVariantForm() {
        selectedVariantId = null;
        view.tblVariants.clearSelection();
        view.txtSku.setText("");
        view.txtSize.setText("");
        view.txtColor.setText("");
        view.txtPrice.setText("");
        view.txtMinStock.setText("5");
        view.chkVariantActive.setSelected(true);
    }

    /**
     * Chọn lại dòng sản phẩm theo id sau khi tải lại bảng.
     *
     * @param id mã sản phẩm
     */
    private void reselect(Long id) {
        for (int i = 0; i < view.tblProducts.getRowCount(); i++) {
            int m = view.tblProducts.convertRowIndexToModel(i);
            if (id != null && id.equals(view.tblProducts.getModel().getValueAt(m, 0))) {
                view.tblProducts.setRowSelectionInterval(i, i);
                return;
            }
        }
    }

    /**
     * Tìm sản phẩm trong danh sách đang hiển thị.
     *
     * @param id mã sản phẩm
     * @return sản phẩm hoặc null
     */
    private Product find(Long id) {
        for (Product p : products) {
            if (p.getId().equals(id)) {
                return p;
            }
        }
        return null;
    }

    /**
     * Đổ dữ liệu vào combo lọc, thêm mục "Tất cả" ở đầu.
     *
     * @param combo combo cần đổ
     * @param items danh sách phần tử
     */
    private void fillFilter(JComboBox<Object> combo, List<?> items) {
        combo.removeAllItems();
        combo.addItem(ALL);
        for (Object o : items) {
            combo.addItem(o);
        }
    }

    /**
     * Đổ dữ liệu vào combo chọn (không có mục "Tất cả").
     *
     * @param combo combo cần đổ
     * @param items danh sách phần tử
     */
    private void fillCombo(JComboBox<Object> combo, List<?> items) {
        combo.removeAllItems();
        for (Object o : items) {
            combo.addItem(o);
        }
    }

    /**
     * Chọn phần tử trong combo theo id; nếu phần tử đang ngừng sử dụng (không có
     * trong combo) thì để trống.
     *
     * @param combo combo
     * @param id    mã cần chọn
     */
    private void selectById(JComboBox<Object> combo, Long id) {
        combo.setSelectedIndex(-1);
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (id != null && id.equals(idOf(combo.getItemAt(i)))) {
                combo.setSelectedIndex(i);
            }
        }
    }

    /**
     * Lấy id của danh mục/thương hiệu được chọn trong combo.
     *
     * @param item phần tử được chọn
     * @return id, hoặc null nếu chọn "Tất cả"/không chọn
     */
    private Long idOf(Object item) {
        if (item instanceof Category) {
            return ((Category) item).getId();
        }
        if (item instanceof Brand) {
            return ((Brand) item).getId();
        }
        return null;
    }
}

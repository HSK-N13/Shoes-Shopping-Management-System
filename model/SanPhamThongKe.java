package model;

/**
 * Chứa 1 dòng thống kê của sản phẩm. Dùng chung cho 3 loại:
 *  - Bán chạy      : soLuong = số lượng đã bán
 *  - Tồn kho       : soLuong = số lượng đang tồn
 *  - Dưới ngưỡng   : soLuong = số lượng đang tồn, nguongToiThieu = mức tối thiểu
 */
public class SanPhamThongKe {

    private String tenSanPham;
    private int soLuong;
    private int nguongToiThieu; // chỉ dùng cho danh sách "dưới ngưỡng", các loại khác để 0

    public SanPhamThongKe() {
    }

    public SanPhamThongKe(String tenSanPham, int soLuong, int nguongToiThieu) {
        this.tenSanPham = tenSanPham;
        this.soLuong = soLuong;
        this.nguongToiThieu = nguongToiThieu;
    }

    public String getTenSanPham() {
        return tenSanPham;
    }

    public void setTenSanPham(String tenSanPham) {
        this.tenSanPham = tenSanPham;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
    }

    public int getNguongToiThieu() {
        return nguongToiThieu;
    }

    public void setNguongToiThieu(int nguongToiThieu) {
        this.nguongToiThieu = nguongToiThieu;
    }
}

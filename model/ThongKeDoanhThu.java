package model;

/**
 * Model = lớp chỉ để chứa dữ liệu (giống Brand, Category của bạn).
 * Lớp này chứa kết quả thống kê doanh thu của 1 khoảng thời gian.
 */
public class ThongKeDoanhThu {

    private double doanhThu; // tổng tiền bán được
    private int soDon;       // số đơn hàng
    private double giaVon;   // tổng tiền vốn (giá nhập x số lượng bán)

    public ThongKeDoanhThu() {
    }

    public ThongKeDoanhThu(double doanhThu, int soDon, double giaVon) {
        this.doanhThu = doanhThu;
        this.soDon = soDon;
        this.giaVon = giaVon;
    }

    // Lợi nhuận gộp = doanh thu trừ giá vốn (tính luôn, không cần lưu riêng)
    public double getLoiNhuanGop() {
        return doanhThu - giaVon;
    }

    public double getDoanhThu() {
        return doanhThu;
    }

    public void setDoanhThu(double doanhThu) {
        this.doanhThu = doanhThu;
    }

    public int getSoDon() {
        return soDon;
    }

    public void setSoDon(int soDon) {
        this.soDon = soDon;
    }

    public double getGiaVon() {
        return giaVon;
    }

    public void setGiaVon(double giaVon) {
        this.giaVon = giaVon;
    }
}

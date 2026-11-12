package dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import connectDB.DBConnection; // SỬA: đổi thành tên lớp kết nối có sẵn trong thư mục connectDB của bạn
import model.SanPhamThongKe;
import model.ThongKeDoanhThu;

/**
 * DAO = lớp chuyên nói chuyện với database (giống ProductDAO, BrandDAO).
 * Lớp này KHÔNG có giao diện, chỉ chạy SQL rồi trả kết quả về.
 * Người làm view/controller sẽ gọi các hàm public ở dưới để lấy số liệu.
 *
 * ====== GIẢ ĐỊNH TÊN BẢNG / CỘT (đối chiếu với file QuanLyBanHang.sql) ======
 * Orders(id, order_date)                                    -> đơn hàng
 * OrderDetails(order_id, variant_id, quantity, unit_price)  -> chi tiết đơn (giá bán)
 * ProductVariant(id, name, stock, min_stock, import_price)  -> tồn kho, ngưỡng tối thiểu, giá nhập (giá vốn)
 * ImportDetails(variant_id, quantity, import_price)         -> chi tiết phiếu nhập
 * Tên khác thì chỉ cần sửa trong các câu SQL bên dưới.
 */
public class ThongKeDAO {

    /**
     * Thống kê doanh thu trong khoảng [tuNgay, denNgay)  (có tuNgay, KHÔNG có denNgay).
     *  - Theo NGÀY : tuNgay = ngày đó,        denNgay = ngày hôm sau
     *  - Theo THÁNG: tuNgay = mùng 1 của tháng, denNgay = mùng 1 tháng sau
     *  - Theo NĂM  : tuNgay = 1/1 của năm,      denNgay = 1/1 năm sau
     * Nên chỉ cần 1 hàm cho cả 3 loại.
     */
    public ThongKeDoanhThu thongKeDoanhThu(Date tuNgay, Date denNgay) {
        ThongKeDoanhThu kq = new ThongKeDoanhThu();

        String sql = "SELECT COUNT(DISTINCT o.id), "              // số đơn
                   + "       SUM(d.quantity * d.unit_price), "    // doanh thu = số lượng x giá bán
                   + "       SUM(d.quantity * v.import_price) "   // giá vốn = số lượng x giá nhập
                   + "FROM Orders o "
                   + "JOIN OrderDetails d ON o.id = d.order_id "
                   + "JOIN ProductVariant v ON d.variant_id = v.id "
                   + "WHERE o.order_date >= ? AND o.order_date < ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDate(1, tuNgay);  // dấu ? thứ 1
            ps.setDate(2, denNgay); // dấu ? thứ 2

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                kq.setSoDon(rs.getInt(1));
                kq.setDoanhThu(rs.getDouble(2)); // nếu không có đơn nào thì SUM = null -> getDouble trả 0
                kq.setGiaVon(rs.getDouble(3));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return kq;
    }

    /** Top 10 sản phẩm bán chạy nhất (soLuong = số lượng đã bán). */
    public List<SanPhamThongKe> sanPhamBanChay() {
        String sql = "SELECT v.name, SUM(d.quantity) AS tong_ban "
                   + "FROM OrderDetails d "
                   + "JOIN ProductVariant v ON d.variant_id = v.id "
                   + "GROUP BY v.name "
                   + "ORDER BY tong_ban DESC";
        return layDanhSach(sql, false, 10); // chỉ lấy 10 dòng đầu
    }

    /** Tồn kho của tất cả sản phẩm (soLuong = số lượng tồn). */
    public List<SanPhamThongKe> tonKho() {
        String sql = "SELECT name, stock FROM ProductVariant ORDER BY name";
        return layDanhSach(sql, false, 0); // 0 = lấy hết
    }

    /** Hàng dưới ngưỡng tồn tối thiểu, cần nhập thêm (có cả nguongToiThieu). */
    public List<SanPhamThongKe> hangDuoiNguong() {
        String sql = "SELECT name, stock, min_stock FROM ProductVariant "
                   + "WHERE stock < min_stock ORDER BY name";
        return layDanhSach(sql, true, 0);
    }

    /** Tổng giá trị nhập = cộng (số lượng nhập x giá nhập) của mọi phiếu nhập. */
    public double tongGiaTriNhap() {
        String sql = "SELECT SUM(quantity * import_price) FROM ImportDetails";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Hàm phụ dùng chung cho 3 hàm danh sách ở trên.
     * coNguong : true nếu câu SQL có thêm cột thứ 3 (min_stock)
     * maxDong  : giới hạn số dòng (0 = không giới hạn)
     */
    private List<SanPhamThongKe> layDanhSach(String sql, boolean coNguong, int maxDong) {
        List<SanPhamThongKe> ds = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setMaxRows(maxDong);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                SanPhamThongKe sp = new SanPhamThongKe();
                sp.setTenSanPham(rs.getString(1)); // cột đếm từ 1
                sp.setSoLuong(rs.getInt(2));
                if (coNguong) {
                    sp.setNguongToiThieu(rs.getInt(3));
                }
                ds.add(sp);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }
}

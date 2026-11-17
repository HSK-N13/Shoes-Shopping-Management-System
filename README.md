# Shoes-Shopping-Management-System

Bài tập lớn HSK Java: quản lý cửa hàng giày dép bán tại quầy (Java Swing + SQL Server),
xây dựng theo file *Đặc tả nghiệp vụ - Quản lý cửa hàng giày dép*.

## Chạy chương trình

1. **Tạo CSDL** trong SQL Server Management Studio:
   - CSDL mới: chạy `database/schema.sql` (xóa và tạo lại `QuanLyBanHang`, có dữ liệu mẫu).
   - Đã tạo CSDL bằng file `QuanLyBanHang.sql` cũ: chạy thêm `database/update_from_old_script.sql`
     (băm lại mật khẩu mẫu, thêm ràng buộc, không xóa dữ liệu).
2. **Cấu hình kết nối** (nếu khác mặc định `sa` / `sapassword` / `localhost:1433`):
   sao chép `db.properties.example` thành `db.properties` ở thư mục gốc dự án rồi sửa.
3. **Chạy** lớp `app.Main` (Eclipse: Run As → Java Application). Thư viện `lib/sqljdbc42.jar` đã có trong `.classpath`.

Hoặc chạy bằng dòng lệnh (Java 11+):

```bash
javac -encoding UTF-8 -d bin -cp lib/sqljdbc42.jar $(find . -name "*.java" -not -path "./bin/*")
```

```bash
java -cp "bin:lib/sqljdbc42.jar" app.Main
```

(Trên Windows thay `:` bằng `;` trong `-cp`.)

**Tài khoản mẫu** (mật khẩu `123456`): `admin` (Admin), `staff1` (Nhân viên).

## Kiến trúc

| Gói | Vai trò |
|---|---|
| `view` | Giao diện Swing (JFrame/JPanel), không gọi SQL |
| `controller` | Bắt sự kiện giao diện, gọi Service, hiển thị lỗi |
| `services` | Nghiệp vụ và kiểm tra quy tắc BR01–BR12, kiểm tra quyền |
| `dao` | Truy cập database bằng JDBC |
| `model` | 13 lớp theo Class Diagram và các enum |
| `dto` | Dữ liệu báo cáo |
| `utils` | Phiên đăng nhập, giao dịch JDBC, băm mật khẩu, định dạng |
| `connectDB` | Kết nối SQL Server dùng chung |
| `app` | Lớp `Main` khởi động |

## Màn hình theo vai trò (BR12)

| Màn hình | Admin | Nhân viên |
|---|---|---|
| Bán hàng, Đơn hàng | ✔ (hủy được đơn đã thanh toán) | ✔ (chỉ hủy đơn nháp) |
| Sản phẩm | Thêm/sửa/ngừng KD, giá | Chỉ tra cứu (ẩn giá vốn) |
| Danh mục, Thương hiệu, Tài khoản | ✔ | Ẩn |
| Khách hàng, Nhà cung cấp, Nhập hàng, Kiểm kê | ✔ | ✔ |
| Báo cáo | Doanh thu, giá vốn, lợi nhuận | Doanh thu, tồn kho (ẩn lợi nhuận) |

Service vẫn kiểm tra lại quyền, kể cả khi nút đã bị ẩn.

## Đối chiếu quy tắc nghiệp vụ

| Mã | Nơi xử lý |
|---|---|
| BR01 SKU duy nhất, size+màu không trùng | `ProductService.saveVariant`, ràng buộc UNIQUE trong DB |
| BR02 Đã giao dịch thì chỉ ngừng KD; biến thể ngừng KD không bán | `ProductService.deleteProduct/deleteVariant`, `OrderService.addItem/saveDraft/confirmAndPay` |
| BR03 Tồn không âm, mọi thay đổi ghi StockMovement | `ProductVariantDAO.decreaseStockIfEnough`, CHECK trong DB, `StockMovementDAO.record` |
| BR04 Phiếu nhập chỉ xác nhận một lần, đã xác nhận không sửa | `PurchaseService.confirmReceipt/updateDraft/cancelReceipt` (đổi trạng thái có điều kiện) |
| BR05 Xác nhận đơn trong một giao dịch | `OrderService.confirmAndPay` + `utils.Transaction` |
| BR06 Chụp giá bán, giá vốn vào OrderItem | `OrderItemDAO.updateSnapshot` khi xác nhận |
| BR07 Giá vốn = giá nhập gần nhất; lợi nhuận gộp | `PurchaseService.confirmReceipt`, `ReportDAO.revenueByPeriod` |
| BR08 Giảm giá ≤ thành tiền; nhân viên ≤ 20% | `OrderService.validateDiscounts` |
| BR09 Tổng tiền tính lại từ các dòng | `OrderService.recalculate` |
| BR10 Một đơn một thanh toán bằng tổng tiền | `OrderService.confirmAndPay`, UNIQUE `payments.order_id` |
| BR11 Hủy đơn nháp tự do; đơn đã thanh toán chỉ Admin, hoàn kho | `OrderService.cancelOrder` |
| BR12 Phân quyền | `utils.Session`, `MainController`, kiểm tra trong mọi Service |

Chống bán vượt tồn khi nhiều máy cùng bán: trừ kho bằng
`UPDATE ... SET stock_quantity = stock_quantity - ? WHERE id = ? AND stock_quantity >= ?`;
nếu 0 dòng được cập nhật thì báo hết hàng và rollback.

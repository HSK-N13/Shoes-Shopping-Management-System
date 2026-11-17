/* =====================================================================
   Cập nhật CSDL đã tạo bằng file QuanLyBanHang.sql cũ để chạy được phiên bản mới.
   - Đổi mật khẩu mẫu sang dạng băm PBKDF2 (mật khẩu: 123456).
   - Bổ sung ràng buộc theo đặc tả: tồn không âm (BR03), size+màu không trùng (BR01),
     mỗi đơn một thanh toán (BR10).
   Không xóa dữ liệu. Có thể chạy nhiều lần.
   ===================================================================== */
USE QuanLyBanHang;
GO

UPDATE users SET password_hash = 'pbkdf2$65536$XzLuKz6rpMFJfPfPISp0bg==$XLCw9vvEAy8a0cFEqYbtVrZRe9Mj2XAFtEPIJSzRhBk='
WHERE username = 'admin' AND password_hash NOT LIKE 'pbkdf2$%';
UPDATE users SET password_hash = 'pbkdf2$65536$JjiJTPz8d8l++/UdYBqlEg==$21IfEWTDNopOLVbVqxoxiD4sW6SwsxBMh9WVv+G6knI='
WHERE username = 'staff1' AND password_hash NOT LIKE 'pbkdf2$%';
GO

UPDATE product_variants SET stock_quantity = 0 WHERE stock_quantity IS NULL;
UPDATE product_variants SET min_stock = 0 WHERE min_stock IS NULL;
UPDATE product_variants SET active = 1 WHERE active IS NULL;
UPDATE products SET active = 1 WHERE active IS NULL;
UPDATE users SET active = 1 WHERE active IS NULL;
GO

IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = 'CK_variant_stock_nonnegative')
    ALTER TABLE product_variants ADD CONSTRAINT CK_variant_stock_nonnegative CHECK (stock_quantity >= 0);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'UQ_variant_size_color')
    CREATE UNIQUE INDEX UQ_variant_size_color ON product_variants(product_id, size, color);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'UQ_payments_order')
    CREATE UNIQUE INDEX UQ_payments_order ON payments(order_id);
GO

/* Dữ liệu mẫu cũ có tồn kho lệch với lịch sử biến động (ví dụ AF1-WHITE-41 tồn 20 nhưng
   lịch sử +20 -1 = 19). Dùng chức năng "Đối soát tồn kho" ở tab Kiểm kê để xem, và
   kiểm kê để đưa tồn về đúng thực tế. */

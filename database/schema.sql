/* =====================================================================
   Quản lý cửa hàng giày dép - Tạo CSDL QuanLyBanHang (SQL Server)
   - Mỗi lớp trong Class Diagram là một bảng, quan hệ là khóa ngoại.
   - sku và username có ràng buộc UNIQUE; mật khẩu lưu dạng băm PBKDF2.
   - Dữ liệu mẫu nhất quán: stock_quantity = tổng quantity_change.
   Tài khoản mẫu (mật khẩu đều là 123456):
     admin  / 123456  (ADMIN)
     staff1 / 123456  (STAFF)
   CẢNH BÁO: script xóa và tạo lại database QuanLyBanHang.
   ===================================================================== */
USE master;
GO
IF DB_ID(N'QuanLyBanHang') IS NOT NULL
BEGIN
    ALTER DATABASE QuanLyBanHang SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE QuanLyBanHang;
END
GO
CREATE DATABASE QuanLyBanHang;
GO
USE QuanLyBanHang;
GO

CREATE TABLE users (
    id            BIGINT IDENTITY(1,1) PRIMARY KEY,
    username      VARCHAR(50)   NOT NULL UNIQUE,
    password_hash VARCHAR(255)  NOT NULL,
    full_name     NVARCHAR(100) NOT NULL,
    role          VARCHAR(20)   NOT NULL CHECK (role IN ('ADMIN', 'STAFF')),
    active        BIT           NOT NULL DEFAULT 1
);

CREATE TABLE categories (
    id     BIGINT IDENTITY(1,1) PRIMARY KEY,
    name   NVARCHAR(100) NOT NULL,
    active BIT NOT NULL DEFAULT 1
);

CREATE TABLE brands (
    id     BIGINT IDENTITY(1,1) PRIMARY KEY,
    name   NVARCHAR(100) NOT NULL,
    active BIT NOT NULL DEFAULT 1
);

CREATE TABLE products (
    id          BIGINT IDENTITY(1,1) PRIMARY KEY,
    category_id BIGINT NULL REFERENCES categories(id),
    brand_id    BIGINT NULL REFERENCES brands(id),
    name        NVARCHAR(200) NOT NULL,
    description NVARCHAR(MAX) NULL,
    active      BIT NOT NULL DEFAULT 1
);

CREATE TABLE product_variants (
    id             BIGINT IDENTITY(1,1) PRIMARY KEY,
    product_id     BIGINT NOT NULL REFERENCES products(id),
    sku            VARCHAR(50) NOT NULL UNIQUE,                       -- BR01
    size           VARCHAR(20) NOT NULL,
    color          NVARCHAR(50) NOT NULL,
    selling_price  DECIMAL(18,2) NOT NULL CHECK (selling_price >= 0),
    cost_price     DECIMAL(18,2) NOT NULL DEFAULT 0 CHECK (cost_price >= 0),
    stock_quantity INT NOT NULL DEFAULT 0 CHECK (stock_quantity >= 0), -- BR03: tồn không âm
    min_stock      INT NOT NULL DEFAULT 5 CHECK (min_stock >= 0),
    active         BIT NOT NULL DEFAULT 1,
    CONSTRAINT UQ_variant_size_color UNIQUE (product_id, size, color)  -- BR01
);

CREATE TABLE customers (
    id        BIGINT IDENTITY(1,1) PRIMARY KEY,
    full_name NVARCHAR(100) NOT NULL,
    phone     VARCHAR(20) NULL
);

CREATE TABLE suppliers (
    id    BIGINT IDENTITY(1,1) PRIMARY KEY,
    name  NVARCHAR(150) NOT NULL,
    phone VARCHAR(20) NULL
);

CREATE TABLE purchase_receipts (
    id           BIGINT IDENTITY(1,1) PRIMARY KEY,
    supplier_id  BIGINT NOT NULL REFERENCES suppliers(id),
    user_id      BIGINT NOT NULL REFERENCES users(id),
    created_at   DATETIME NOT NULL DEFAULT GETDATE(),
    status       VARCHAR(20) NOT NULL CHECK (status IN ('DRAFT', 'CONFIRMED', 'CANCELLED')),
    total_amount DECIMAL(18,2) NOT NULL DEFAULT 0
);

CREATE TABLE purchase_items (
    id         BIGINT IDENTITY(1,1) PRIMARY KEY,
    receipt_id BIGINT NOT NULL REFERENCES purchase_receipts(id),
    variant_id BIGINT NOT NULL REFERENCES product_variants(id),
    quantity   INT NOT NULL CHECK (quantity > 0),
    unit_cost  DECIMAL(18,2) NOT NULL CHECK (unit_cost >= 0)
);

CREATE TABLE orders (
    id              BIGINT IDENTITY(1,1) PRIMARY KEY,
    customer_id     BIGINT NULL REFERENCES customers(id),
    user_id         BIGINT NOT NULL REFERENCES users(id),
    created_at      DATETIME NOT NULL DEFAULT GETDATE(),
    status          VARCHAR(20) NOT NULL CHECK (status IN ('DRAFT', 'PAID', 'CANCELLED')),
    discount_amount DECIMAL(18,2) NOT NULL DEFAULT 0 CHECK (discount_amount >= 0),
    total_amount    DECIMAL(18,2) NOT NULL DEFAULT 0
);

CREATE TABLE order_items (
    id              BIGINT IDENTITY(1,1) PRIMARY KEY,
    order_id        BIGINT NOT NULL REFERENCES orders(id),
    variant_id      BIGINT NOT NULL REFERENCES product_variants(id),
    quantity        INT NOT NULL CHECK (quantity > 0),
    unit_price      DECIMAL(18,2) NOT NULL,   -- BR06: ảnh chụp giá bán
    unit_cost       DECIMAL(18,2) NOT NULL,   -- BR06: ảnh chụp giá vốn
    discount_amount DECIMAL(18,2) NOT NULL DEFAULT 0 CHECK (discount_amount >= 0)
);

CREATE TABLE payments (
    id       BIGINT IDENTITY(1,1) PRIMARY KEY,
    order_id BIGINT NOT NULL UNIQUE REFERENCES orders(id),             -- BR10: một đơn một thanh toán
    amount   DECIMAL(18,2) NOT NULL,
    method   VARCHAR(20) NOT NULL CHECK (method IN ('CASH', 'TRANSFER')),
    paid_at  DATETIME NOT NULL DEFAULT GETDATE()
);

CREATE TABLE stock_movements (
    id              BIGINT IDENTITY(1,1) PRIMARY KEY,
    variant_id      BIGINT NOT NULL REFERENCES product_variants(id),
    type            VARCHAR(20) NOT NULL CHECK (type IN ('PURCHASE', 'SALE', 'ADJUSTMENT', 'CANCEL')),
    quantity_change INT NOT NULL,
    ref_type        VARCHAR(50) NULL,
    ref_id          BIGINT NULL,
    created_at      DATETIME NOT NULL DEFAULT GETDATE()
);

CREATE INDEX IX_orders_created ON orders(created_at, status);
CREATE INDEX IX_order_items_order ON order_items(order_id);
CREATE INDEX IX_purchase_items_receipt ON purchase_items(receipt_id);
CREATE INDEX IX_stock_movements_variant ON stock_movements(variant_id, created_at);
GO

/* ------------------------- Dữ liệu mẫu ------------------------- */
INSERT INTO users (username, password_hash, full_name, role, active) VALUES
 ('admin',  'pbkdf2$65536$XzLuKz6rpMFJfPfPISp0bg==$XLCw9vvEAy8a0cFEqYbtVrZRe9Mj2XAFtEPIJSzRhBk=', N'Quản Lý Viên', 'ADMIN', 1),
 ('staff1', 'pbkdf2$65536$JjiJTPz8d8l++/UdYBqlEg==$21IfEWTDNopOLVbVqxoxiD4sW6SwsxBMh9WVv+G6knI=', N'Nhân Viên 1', 'STAFF', 1);

INSERT INTO categories (name) VALUES (N'Giày Thể Thao'), (N'Giày Chạy Bộ'), (N'Dép');
INSERT INTO brands (name) VALUES (N'Nike'), (N'Adidas'), (N'Bitis');

INSERT INTO products (category_id, brand_id, name, description) VALUES
 (1, 1, N'Nike Air Force 1', N'Giày thể thao cổ điển'),
 (2, 2, N'Adidas Ultraboost', N'Giày chạy bộ êm ái'),
 (3, 3, N'Dép Bitis Hunter', N'Dép quai ngang');

-- Tồn ban đầu = 0; tồn chỉ tăng qua phiếu nhập đã xác nhận bên dưới
INSERT INTO product_variants (product_id, sku, size, color, selling_price, cost_price, stock_quantity, min_stock) VALUES
 (1, 'AF1-WHITE-41', '41', N'Trắng', 2500000, 0, 0, 5),
 (1, 'AF1-BLACK-42', '42', N'Đen',   2600000, 0, 0, 5),
 (2, 'UB-GREY-42',   '42', N'Xám',   3200000, 0, 0, 3),
 (3, 'BH-RED-40',    '40', N'Đỏ',     350000, 0, 0, 10);

INSERT INTO customers (full_name, phone) VALUES (N'Nguyễn Văn A', '0987654321');
INSERT INTO suppliers (name, phone) VALUES (N'Công ty TNHH Bán Sỉ Thể Thao', '0901234567');

-- Phiếu nhập #1 đã xác nhận
INSERT INTO purchase_receipts (supplier_id, user_id, created_at, status, total_amount)
VALUES (1, 1, DATEADD(DAY, -3, GETDATE()), 'CONFIRMED', 70050000);
INSERT INTO purchase_items (receipt_id, variant_id, quantity, unit_cost) VALUES
 (1, 1, 20, 1800000), (1, 2, 15, 1850000), (1, 3, 2, 2400000), (1, 4, 6, 250000);
UPDATE product_variants SET stock_quantity = 20, cost_price = 1800000 WHERE id = 1;
UPDATE product_variants SET stock_quantity = 15, cost_price = 1850000 WHERE id = 2;
UPDATE product_variants SET stock_quantity = 2,  cost_price = 2400000 WHERE id = 3;
UPDATE product_variants SET stock_quantity = 6,  cost_price = 250000  WHERE id = 4;
INSERT INTO stock_movements (variant_id, type, quantity_change, ref_type, ref_id, created_at) VALUES
 (1, 'PURCHASE', 20, 'PURCHASE_RECEIPT', 1, DATEADD(DAY, -3, GETDATE())),
 (2, 'PURCHASE', 15, 'PURCHASE_RECEIPT', 1, DATEADD(DAY, -3, GETDATE())),
 (3, 'PURCHASE',  2, 'PURCHASE_RECEIPT', 1, DATEADD(DAY, -3, GETDATE())),
 (4, 'PURCHASE',  6, 'PURCHASE_RECEIPT', 1, DATEADD(DAY, -3, GETDATE()));

-- Đơn #1 đã thanh toán: 1 đôi AF1 trắng 41
INSERT INTO orders (customer_id, user_id, created_at, status, discount_amount, total_amount)
VALUES (1, 2, DATEADD(DAY, -1, GETDATE()), 'PAID', 0, 2500000);
INSERT INTO order_items (order_id, variant_id, quantity, unit_price, unit_cost, discount_amount)
VALUES (1, 1, 1, 2500000, 1800000, 0);
INSERT INTO payments (order_id, amount, method, paid_at) VALUES (1, 2500000, 'TRANSFER', DATEADD(DAY, -1, GETDATE()));
UPDATE product_variants SET stock_quantity = stock_quantity - 1 WHERE id = 1;
INSERT INTO stock_movements (variant_id, type, quantity_change, ref_type, ref_id, created_at)
VALUES (1, 'SALE', -1, 'ORDER', 1, DATEADD(DAY, -1, GETDATE()));
GO

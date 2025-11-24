-- =============================================
-- DATABASE: ARLENE JEWELRY SHOP
-- =============================================
CREATE DATABASE IF NOT EXISTS arlene_jewelry;
USE arlene_jewelry;

-- =============================================
-- TABLE: USERS (Admin dan Customer)
-- =============================================
CREATE TABLE IF NOT EXISTS users (
    user_id VARCHAR(20) PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(20),
    address TEXT,
    role ENUM('CUSTOMER', 'ADMIN') NOT NULL,
    full_name VARCHAR(100),
    admin_level VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- TABLE: PRODUCTS (Data perhiasan)
-- =============================================
CREATE TABLE IF NOT EXISTS products (
    product_id VARCHAR(20) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    material VARCHAR(50) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    stock INT NOT NULL,
    description TEXT,
    image_path VARCHAR(255),
    weight DECIMAL(5,2),
    has_gemstone BOOLEAN DEFAULT FALSE,
    gemstone_type VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- TABLE: SHOPPING_CARTS (Keranjang belanja) - ✅ DIPERBAIKI
-- =============================================
CREATE TABLE IF NOT EXISTS shopping_carts (
    cart_id VARCHAR(100) PRIMARY KEY,  -- ✅ UBAH JADI 100!
    user_id VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- =============================================
-- TABLE: CART_ITEMS (Item dalam keranjang) - ✅ DIPERBAIKI
-- =============================================
CREATE TABLE IF NOT EXISTS cart_items (
    cart_item_id INT AUTO_INCREMENT PRIMARY KEY,
    cart_id VARCHAR(100) NOT NULL,  -- ✅ UBAH JADI 100!
    product_id VARCHAR(20) NOT NULL,
    quantity INT NOT NULL,
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (cart_id) REFERENCES shopping_carts(cart_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE
);

-- =============================================
-- TABLE: ORDERS (Data order/pesanan)
-- =============================================
CREATE TABLE IF NOT EXISTS orders (
    order_id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(20) NOT NULL,
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(10,2) NOT NULL,
    status ENUM('PENDING', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED') DEFAULT 'PENDING',
    payment_method VARCHAR(50),
    shipping_address TEXT,
    notes TEXT,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- =============================================
-- TABLE: ORDER_ITEMS (Item dalam order)
-- =============================================
CREATE TABLE IF NOT EXISTS order_items (
    order_item_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id VARCHAR(50) NOT NULL,
    product_id VARCHAR(20) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE
);

-- =============================================
-- INSERT DATA SAMPLE: USERS
-- =============================================
INSERT INTO users (user_id, username, password, email, phone, address, role, admin_level, full_name) VALUES 
('ADM001', 'admin', 'admin123', 'admin@arlene.com', '08123456789', 'Jl. Admin No. 1, Jakarta', 'ADMIN', 'Super Admin', NULL),
('CUST001', 'customer1', 'pass123', 'customer1@email.com', '08111111111', 'Jl. Customer No. 1, Bandung', 'CUSTOMER', NULL, 'Alice Johnson'),
('CUST002', 'alice', 'alice123', 'alice@email.com', '08222222222', 'Jl. Merdeka No. 45, Surabaya', 'CUSTOMER', NULL, 'Alice Wonderland'),
('CUST003', 'budi', 'budi123', 'budi@email.com', '08333333333', 'Jl. Sudirman No. 10, Jakarta', 'CUSTOMER', NULL, 'Budi Santoso');

-- =============================================
-- INSERT DATA SAMPLE: PRODUCTS 
-- =============================================
INSERT INTO products (product_id, name, category, material, price, stock, description, image_path, weight, has_gemstone, gemstone_type) VALUES
-- RINGS (Cincin)
('R001', 'Diamond Engagement Ring', 'Ring', 'Platinum', 2500.00, 10, 'Beautiful diamond engagement ring with platinum band.', 'assets/images/products/R001.png', 5.2, TRUE, 'Diamond'),
('R002', 'Gold Wedding Band', 'Ring', 'Gold', 1200.00, 15, 'Elegant gold wedding band for your special day.', 'assets/images/products/R002.png', 4.8, FALSE, 'None'),
('R003', 'Sapphire Promise Ring', 'Ring', 'White Gold', 1800.00, 8, 'Stunning sapphire ring with diamond accents.', 'assets/images/products/R003.png', 5.5, TRUE, 'Sapphire'),

-- NECKLACES (Kalung)
('N001', 'Pearl Necklace', 'Necklace', 'Pearl', 800.00, 8, 'Classic pearl necklace for elegant occasions.', 'assets/images/products/N001.png', 12.0, FALSE, 'None'),
('N002', 'Gold Chain Necklace', 'Necklace', 'Gold', 600.00, 12, 'Stylish gold chain necklace for daily wear.', 'assets/images/products/N002.png', 8.5, FALSE, 'None'),

-- BRACELETS (Gelang)
('B001', 'Silver Charm Bracelet', 'Bracelet', 'Silver', 300.00, 20, 'Beautiful silver charm bracelet with various charms.', 'assets/images/products/B001.png', 15.0, FALSE, 'None'),
('B002', 'Diamond Tennis Bracelet', 'Bracelet', 'Gold', 3500.00, 5, 'Luxurious diamond tennis bracelet for special events.', 'assets/images/products/B002.png', 18.5, TRUE, 'Diamond'),

-- EARRINGS (Anting)
('E001', 'Diamond Stud Earrings', 'Earrings', 'Platinum', 1500.00, 10, 'Elegant diamond stud earrings for formal occasions.', 'assets/images/products/E001.png', 3.2, TRUE, 'Diamond'),
('E002', 'Gold Hoop Earrings', 'Earrings', 'Gold', 400.00, 15, 'Fashionable gold hoop earrings for everyday style.', 'assets/images/products/E002.png', 4.5, FALSE, 'None');

-- =============================================
-- TAMPILKAN DATA YANG SUDAH DIMASUKKAN
-- =============================================
SELECT '=== USERS ===' as '';
SELECT user_id, username, role, email FROM users;

SELECT '=== PRODUCTS ===' as '';
SELECT product_id, name, category, material, price, stock FROM products;

SELECT '=== TABLE STRUCTURES ===' as '';
DESCRIBE shopping_carts;

DESCRIBE cart_items;

-- Greengrocer Application Database Schema
-- MySQL Database: greengrocer_db
-- Connection: myuser@localhost / 1234

-- Create database
CREATE DATABASE IF NOT EXISTS greengrocer_db;
USE greengrocer_db;

-- Create user and grant privileges
-- Note: This might require root privileges to execute
CREATE USER IF NOT EXISTS 'myuser'@'localhost' IDENTIFIED BY '1234';
GRANT ALL PRIVILEGES ON greengrocer_db.* TO 'myuser'@'localhost';
FLUSH PRIVILEGES;

-- =============================================
-- USER INFO TABLE
-- =============================================
CREATE TABLE IF NOT EXISTS UserInfo (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('customer', 'carrier', 'owner') NOT NULL,
    full_name VARCHAR(100),
    address VARCHAR(255),
    phone VARCHAR(20),
    email VARCHAR(100),
    loyalty_points INT DEFAULT 0,
    total_orders INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- PRODUCT INFO TABLE
-- =============================================
CREATE TABLE IF NOT EXISTS ProductInfo (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type ENUM('vegetable', 'fruit') NOT NULL,
    unit_type ENUM('kg', 'piece') NOT NULL DEFAULT 'kg',
    price DECIMAL(10, 2) NOT NULL,
    stock DECIMAL(10, 2) NOT NULL DEFAULT 0,
    threshold DECIMAL(10, 2) DEFAULT 5.0,
    image LONGBLOB,
    image_name VARCHAR(100),
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- =============================================
-- ORDER INFO TABLE
-- =============================================
CREATE TABLE IF NOT EXISTS OrderInfo (
    id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    carrier_id INT,
    order_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    requested_delivery_time DATETIME NOT NULL,
    actual_delivery_time DATETIME,
    status ENUM('pending', 'selected', 'delivered', 'cancelled') DEFAULT 'pending',
    subtotal DECIMAL(10, 2) NOT NULL,
    vat_amount DECIMAL(10, 2) NOT NULL,
    discount_amount DECIMAL(10, 2) DEFAULT 0,
    total_cost DECIMAL(10, 2) NOT NULL,
    coupon_code VARCHAR(50),
    invoice LONGBLOB,
    notes TEXT,
    FOREIGN KEY (customer_id) REFERENCES UserInfo(id),
    FOREIGN KEY (carrier_id) REFERENCES UserInfo(id)
);

-- =============================================
-- ORDER ITEMS TABLE
-- =============================================
CREATE TABLE IF NOT EXISTS OrderItems (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    product_name VARCHAR(100) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES OrderInfo(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES ProductInfo(id)
);

-- =============================================
-- MESSAGES TABLE
-- =============================================
CREATE TABLE IF NOT EXISTS Messages (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sender_id INT NOT NULL,
    receiver_id INT NOT NULL,
    subject VARCHAR(200),
    content TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    parent_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sender_id) REFERENCES UserInfo(id),
    FOREIGN KEY (receiver_id) REFERENCES UserInfo(id),
    FOREIGN KEY (parent_id) REFERENCES Messages(id)
);

-- =============================================
-- COUPONS TABLE
-- =============================================
CREATE TABLE IF NOT EXISTS Coupons (
    id INT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    discount_percent DECIMAL(5, 2) NOT NULL,
    min_cart_value DECIMAL(10, 2) DEFAULT 0,
    max_uses INT DEFAULT 1,
    current_uses INT DEFAULT 0,
    user_id INT,
    valid_from DATETIME DEFAULT CURRENT_TIMESTAMP,
    valid_until DATETIME,
    is_active BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (user_id) REFERENCES UserInfo(id)
);

-- =============================================
-- CARRIER RATINGS TABLE
-- =============================================
CREATE TABLE IF NOT EXISTS CarrierRatings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    carrier_id INT NOT NULL,
    customer_id INT NOT NULL,
    order_id INT NOT NULL,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (carrier_id) REFERENCES UserInfo(id),
    FOREIGN KEY (customer_id) REFERENCES UserInfo(id),
    FOREIGN KEY (order_id) REFERENCES OrderInfo(id),
    UNIQUE KEY unique_rating (order_id, customer_id)
);

-- =============================================
-- LOYALTY SETTINGS TABLE
-- =============================================
CREATE TABLE IF NOT EXISTS LoyaltySettings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    min_orders INT NOT NULL,
    discount_percent DECIMAL(5, 2) NOT NULL,
    description VARCHAR(200)
);

-- =============================================
-- INSERT DEFAULT USERS
-- =============================================
INSERT INTO UserInfo (username, password, role, full_name, address, phone, email) VALUES
('cust', '$argon2id$v=19$m=65536,t=3,p=1$HjB2q0lcIGARQbllAZHQ5g==$uhxlHn2Xj17rifTAAoKbR1eQiMPP/60sL0SUn7BS28g=', 'customer', 'Test Customer', 'Istanbul, Turkey', '555-0001', 'customer@test.com'),
('carr', '$argon2id$v=19$m=65536,t=3,p=1$Un7JvsE0ofDbHeJ1NspLSw==$um1YuxIzHHfg8Q6/0gdcyXkJHpR9Iiaw+hOU+k7+g3k=', 'carrier', 'Test Carrier', 'Istanbul, Turkey', '555-0002', 'carrier@test.com'),
('own', '$argon2id$v=19$m=65536,t=3,p=1$Q7FYdcDMxVfPOCaFgttknQ==$2T/b+wK67N/aP5TqJ5ggWQbCpH3ueQMPm97y/QYf+wY=', 'owner', 'Store Owner', 'Istanbul, Turkey', '555-0003', 'owner@test.com'),
('customer1', '$argon2id$v=19$m=65536,t=3,p=1$8/UypHgH3oPBDvDH5Ngo/Q==$06Ln52oBL2xGi0w+elRlbwkXtaYW5MqYANJoPkPdxpo=', 'customer', 'Ahmet Yilmaz', 'Kadikoy, Istanbul', '555-1001', 'ahmet@email.com'),
('customer2', '$argon2id$v=19$m=65536,t=3,p=1$8/UypHgH3oPBDvDH5Ngo/Q==$06Ln52oBL2xGi0w+elRlbwkXtaYW5MqYANJoPkPdxpo=', 'customer', 'Ayse Demir', 'Besiktas, Istanbul', '555-1002', 'ayse@email.com'),
('customer3', '$argon2id$v=19$m=65536,t=3,p=1$8/UypHgH3oPBDvDH5Ngo/Q==$06Ln52oBL2xGi0w+elRlbwkXtaYW5MqYANJoPkPdxpo=', 'customer', 'Mehmet Kaya', 'Uskudar, Istanbul', '555-1003', 'mehmet@email.com'),
('carrier1', '$argon2id$v=19$m=65536,t=3,p=1$8/UypHgH3oPBDvDH5Ngo/Q==$06Ln52oBL2xGi0w+elRlbwkXtaYW5MqYANJoPkPdxpo=', 'carrier', 'Ali Ozturk', 'Fatih, Istanbul', '555-2001', 'ali@email.com'),
('carrier2', '$argon2id$v=19$m=65536,t=3,p=1$8/UypHgH3oPBDvDH5Ngo/Q==$06Ln52oBL2xGi0w+elRlbwkXtaYW5MqYANJoPkPdxpo=', 'carrier', 'Veli Celik', 'Beyoglu, Istanbul', '555-2002', 'veli@email.com');

-- =============================================
-- INSERT VEGETABLES (12+)
-- =============================================
INSERT INTO ProductInfo (name, type, price, stock, threshold, description) VALUES
('Patates', 'vegetable', 8.50, 100.0, 10.0, 'Taze yerli patates'),
('Domates', 'vegetable', 12.00, 80.0, 8.0, 'Organik salkım domates'),
('Salatalık', 'vegetable', 10.00, 60.0, 6.0, 'Taze çıtır salatalık'),
('Biber', 'vegetable', 15.00, 50.0, 5.0, 'Dolmalık yeşil biber'),
('Patlıcan', 'vegetable', 14.00, 45.0, 5.0, 'Kemer patlıcan'),
('Soğan', 'vegetable', 7.00, 120.0, 15.0, 'Kuru soğan'),
('Havuç', 'vegetable', 9.00, 70.0, 7.0, 'Taze havuç'),
('Kabak', 'vegetable', 11.00, 40.0, 4.0, 'Sakız kabağı'),
('Ispanak', 'vegetable', 18.00, 30.0, 3.0, 'Taze ıspanak'),
('Marul', 'vegetable', 8.00, 35.0, 4.0, 'Kıvırcık marul'),
('Brokoli', 'vegetable', 22.00, 25.0, 3.0, 'Taze brokoli'),
('Lahana', 'vegetable', 6.00, 50.0, 5.0, 'Beyaz lahana'),
('Sarımsak', 'vegetable', 45.00, 20.0, 2.0, 'Yerli sarımsak'),
('Turp', 'vegetable', 7.50, 40.0, 4.0, 'Kırmızı turp'),
('Pırasa', 'vegetable', 12.00, 35.0, 4.0, 'Taze pırasa');

-- =============================================
-- INSERT FRUITS (12+)
-- =============================================
INSERT INTO ProductInfo (name, type, price, stock, threshold, description) VALUES
('Elma', 'fruit', 15.00, 90.0, 10.0, 'Amasya elması'),
('Armut', 'fruit', 18.00, 60.0, 6.0, 'Deveci armudu'),
('Portakal', 'fruit', 12.00, 100.0, 12.0, 'Finike portakalı'),
('Mandalina', 'fruit', 14.00, 80.0, 8.0, 'Bodrum mandalinası'),
('Muz', 'fruit', 35.00, 50.0, 5.0, 'İthal muz'),
('Üzüm', 'fruit', 25.00, 40.0, 4.0, 'Sultani üzüm'),
('Çilek', 'fruit', 45.00, 20.0, 2.0, 'Taze çilek'),
('Karpuz', 'fruit', 8.00, 200.0, 20.0, 'Diyarbakır karpuzu'),
('Kavun', 'fruit', 10.00, 80.0, 8.0, 'Kırkağaç kavunu'),
('Şeftali', 'fruit', 28.00, 35.0, 4.0, 'Bursa şeftalisi'),
('Kayısı', 'fruit', 32.00, 30.0, 3.0, 'Malatya kayısısı'),
('Kiraz', 'fruit', 55.00, 25.0, 3.0, 'Napolyon kiraz'),
('Erik', 'fruit', 20.00, 40.0, 4.0, 'Can eriği'),
('Nar', 'fruit', 18.00, 45.0, 5.0, 'Hicaz narı'),
('Limon', 'fruit', 16.00, 70.0, 7.0, 'Meyer limon');

-- =============================================
-- INSERT LOYALTY SETTINGS
-- =============================================
INSERT INTO LoyaltySettings (min_orders, discount_percent, description) VALUES
(5, 5.00, '5 ve üzeri sipariş için %5 indirim'),
(10, 10.00, '10 ve üzeri sipariş için %10 indirim'),
(20, 15.00, '20 ve üzeri sipariş için %15 indirim');

-- =============================================
-- INSERT SAMPLE COUPONS
-- =============================================
INSERT INTO Coupons (code, discount_percent, min_cart_value, max_uses, valid_until) VALUES
('HOSGELDIN', 10.00, 50.00, 100, DATE_ADD(NOW(), INTERVAL 30 DAY)),
('YAZ2024', 15.00, 100.00, 50, DATE_ADD(NOW(), INTERVAL 60 DAY)),
('SADIK', 20.00, 150.00, 25, DATE_ADD(NOW(), INTERVAL 90 DAY));

-- =============================================
-- INSERT SAMPLE ORDERS (for testing)
-- =============================================
INSERT INTO OrderInfo (customer_id, carrier_id, requested_delivery_time, actual_delivery_time, status, subtotal, vat_amount, total_cost) VALUES
(1, 2, DATE_ADD(NOW(), INTERVAL 1 DAY), NULL, 'pending', 85.00, 15.30, 100.30),
(4, 2, DATE_ADD(NOW(), INTERVAL 2 DAY), NULL, 'selected', 120.00, 21.60, 141.60),
(5, 7, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), 'delivered', 95.00, 17.10, 112.10),
(6, 8, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), 'delivered', 150.00, 27.00, 177.00);

-- =============================================
-- INSERT SAMPLE ORDER ITEMS
-- =============================================
INSERT INTO OrderItems (order_id, product_id, product_name, amount, unit_price, total_price) VALUES
(1, 1, 'Patates', 2.5, 8.50, 21.25),
(1, 2, 'Domates', 1.5, 12.00, 18.00),
(1, 16, 'Elma', 2.0, 15.00, 30.00),
(2, 3, 'Salatalık', 1.0, 10.00, 10.00),
(2, 4, 'Biber', 2.0, 15.00, 30.00),
(2, 17, 'Armut', 3.0, 18.00, 54.00),
(3, 5, 'Patlıcan', 1.5, 14.00, 21.00),
(3, 6, 'Soğan', 3.0, 7.00, 21.00),
(3, 20, 'Muz', 1.0, 35.00, 35.00),
(4, 7, 'Havuç', 2.0, 9.00, 18.00),
(4, 8, 'Kabak', 1.5, 11.00, 16.50),
(4, 21, 'Üzüm', 2.0, 25.00, 50.00);

-- =============================================
-- INSERT SAMPLE MESSAGES
-- =============================================
INSERT INTO Messages (sender_id, receiver_id, subject, content) VALUES
(1, 3, 'Teslimat Hakkında', 'Merhaba, siparişimin ne zaman teslim edileceğini öğrenebilir miyim?'),
(4, 3, 'Ürün Talebi', 'Avokado satışa sunulacak mı?'),
(5, 3, 'Teşekkür', 'Hizmetiniz için teşekkür ederim, çok memnun kaldım.');

-- =============================================
-- INSERT SAMPLE CARRIER RATINGS
-- =============================================
INSERT INTO CarrierRatings (carrier_id, customer_id, order_id, rating, comment) VALUES
(7, 5, 3, 5, 'Çok hızlı ve nazik teslimat'),
(8, 6, 4, 4, 'Teslimat zamanında yapıldı');

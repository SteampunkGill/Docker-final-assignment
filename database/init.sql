SET NAMES 'utf8mb4';
-- 创建数据库
CREATE DATABASE IF NOT EXISTS `simple_mall` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `simple_mall`;

-- 1. 用户表 (来自实体: Users)
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
                         `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
                         `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
                         `password` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码（加密存储）',
                         `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '手机号',
                         `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                         `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                         PRIMARY KEY (`id`),
                         UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 2. 地址表 (来自实体: Addresses, 实现1:N关系: Owns)
DROP TABLE IF EXISTS `addresses`;
CREATE TABLE `addresses` (
                             `id` bigint NOT NULL AUTO_INCREMENT COMMENT '地址ID',
                             `user_id` bigint NOT NULL COMMENT '用户ID (外键, 实现1:N)',
                             `receiver_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '收货人姓名',
                             `receiver_phone` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '收货人电话',
                             `province` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '省',
                             `city` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '市',
                             `district` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '区',
                             `detail` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '详细地址',
                             `is_default` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否为默认地址(0:否, 1:是)',
                             `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                             PRIMARY KEY (`id`),
                             KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户收货地址表';

-- 3. 商品分类表 (来自实体: Categories)
DROP TABLE IF EXISTS `categories`;
CREATE TABLE `categories` (
                              `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类ID',
                              `name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类名称',
                              `parent_id` bigint NOT NULL DEFAULT '0' COMMENT '父分类ID (0为顶级分类)',
                              `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                              PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分类表';

-- 4. 商品表 (来自实体: Products, 实现1:N关系: Categorizes)
DROP TABLE IF EXISTS `products`;
CREATE TABLE `products` (
                            `id` bigint NOT NULL AUTO_INCREMENT COMMENT '商品ID',
                            `category_id` bigint NOT NULL COMMENT '分类ID (外键, 实现1:N)',
                            `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品名称',
                            `description` text COLLATE utf8mb4_unicode_ci COMMENT '商品描述',
                            `price` decimal(10,2) NOT NULL COMMENT '价格',
                            `stock` int NOT NULL DEFAULT '0' COMMENT '库存',
                            `image_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '商品主图URL',
                            `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                            PRIMARY KEY (`id`),
                            KEY `idx_category_id` (`category_id`),
                            KEY `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

-- 5. 购物车表 (实现M:N关系: AddsToCart)
DROP TABLE IF EXISTS `carts`;
CREATE TABLE `carts` (
                         `id` bigint NOT NULL AUTO_INCREMENT COMMENT '购物车项ID',
                         `user_id` bigint NOT NULL COMMENT '用户ID (外键)',
                         `product_id` bigint NOT NULL COMMENT '商品ID (外键)',
                         `quantity` int NOT NULL COMMENT '数量',
                         `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                         `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                         PRIMARY KEY (`id`),
                         UNIQUE KEY `uk_user_product` (`user_id`,`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='购物车表';

-- 6. 订单表 (来自实体: Orders, 实现1:N关系: Places)
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders` (
                          `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单ID',
                          `order_no` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单号',
                          `user_id` bigint NOT NULL COMMENT '用户ID (外键, 实现1:N)',
                          `total_amount` decimal(10,2) NOT NULL COMMENT '订单总金额',
                          `status` int NOT NULL DEFAULT '0' COMMENT '订单状态(0:待付款, 1:已付款, 2:已发货, 3:已完成, 4:已取消)',
                          `receiver_info` json NOT NULL COMMENT '收货人快照(JSON)',
                          `payment_time` datetime DEFAULT NULL COMMENT '支付时间',
                          `shipping_time` datetime DEFAULT NULL COMMENT '发货时间',
                          `complete_time` datetime DEFAULT NULL COMMENT '完成时间',
                          `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                          `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                          PRIMARY KEY (`id`),
                          UNIQUE KEY `uk_order_no` (`order_no`),
                          KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- 7. 订单明细表 (实现M:N关系: Contains)
DROP TABLE IF EXISTS `order_items`;
CREATE TABLE `order_items` (
                               `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单明细ID',
                               `order_id` bigint NOT NULL COMMENT '订单ID (外键)',
                               `product_id` bigint NOT NULL COMMENT '商品ID (外键)',
                               `product_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品名称快照',
                               `price` decimal(10,2) NOT NULL COMMENT '商品价格快照',
                               `quantity` int NOT NULL COMMENT '购买数量',
                               `image_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '商品图片快照',
                               `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                               PRIMARY KEY (`id`),
                               KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单明细表';
SET NAMES 'utf8mb4';
USE `simple_mall`;

-- 插入用户数据
INSERT INTO `users` (`username`, `password`, `phone`, `create_time`, `update_time`) VALUES
                                                                                        ('zhangsan', '$2a$10$e.g.encoded.password.for.zhangsan', '13800001111', NOW(), NOW()),
                                                                                        ('lisi', '$2a$10$e.g.encoded.password.for.lisi', '13900002222', NOW(), NOW()),
                                                                                        ('wangwu', '$2a$10$e.g.encoded.password.for.wangwu', '13700003333', NOW(), NOW());

-- 插入地址数据
INSERT INTO `addresses` (`user_id`, `receiver_name`, `receiver_phone`, `province`, `city`, `district`, `detail`, `is_default`, `create_time`, `update_time`) VALUES
                                                                                                                                                                 (1, '张三', '13800001111', '广东省', '广州市', '天河区', '天河路1号', 1, NOW(), NOW()),
                                                                                                                                                                 (1, '张三', '13800001111', '广东省', '深圳市', '福田区', '福华路2号', 0, NOW(), NOW()),
                                                                                                                                                                 (2, '李四', '13900002222', '上海市', '上海市', '浦东新区', '世纪大道3号', 1, NOW(), NOW());

-- 插入商品分类数据
INSERT INTO `categories` (`name`, `parent_id`, `create_time`, `update_time`) VALUES
                                                                                 ('电子产品', 0, NOW(), NOW()),
                                                                                 ('手机', 1, NOW(), NOW()),
                                                                                 ('笔记本电脑', 1, NOW(), NOW()),
                                                                                 ('家居用品', 0, NOW(), NOW()),
                                                                                 ('厨房电器', 4, NOW(), NOW()),
                                                                                 ('卧室家具', 4, NOW(), NOW());

-- 插入商品数据
INSERT INTO `products` (`category_id`, `name`, `description`, `price`, `stock`, `image_url`, `create_time`, `update_time`) VALUES
                                                                                                                               (2, 'iPhone 15 Pro Max', '苹果最新款旗舰手机，性能卓越，拍照一流。', 9999.00, 100, 'http://example.com/images/iphone15.jpg', NOW(), NOW()),
                                                                                                                               (2, '华为 Mate 60 Pro', '国产高端商务手机，信号强劲，续航持久。', 7999.00, 80, 'http://example.com/images/mate60.jpg', NOW(), NOW()),
                                                                                                                               (3, 'MacBook Pro M3', '苹果专业级笔记本电脑，M3芯片，性能强悍。', 15999.00, 50, 'http://example.com/images/macbookpro.jpg', NOW(), NOW()),
                                                                                                                               (3, '联想小新Pro 16', '高性能轻薄本，适合办公和轻度游戏。', 6999.00, 120, 'http://example.com/images/xiaoxin16.jpg', NOW(), NOW()),
                                                                                                                               (5, '美的电饭煲', '智能预约，多功能电饭煲。', 399.00, 200, 'http://example.com/images/dianfanbao.jpg', NOW(), NOW()),
                                                                                                                               (6, '宜家双人床', '简约现代风格双人床，舒适耐用。', 1299.00, 30, 'http://example.com/images/bed.jpg', NOW(), NOW());

-- 插入购物车数据 (用户1购买了iPhone和电饭煲，用户2购买了华为手机)
INSERT INTO `carts` (`user_id`, `product_id`, `quantity`, `create_time`, `update_time`) VALUES
                                                                                            (1, 1, 1, NOW(), NOW()), -- 用户1的购物车：iPhone 15 Pro Max * 1
                                                                                            (1, 5, 2, NOW(), NOW()), -- 用户1的购物车：美的电饭煲 * 2
                                                                                            (2, 2, 1, NOW(), NOW()); -- 用户2的购物车：华为 Mate 60 Pro * 1

-- 插入订单数据 (用户1下了一个订单，用户2下了一个订单)
INSERT INTO `orders` (`order_no`, `user_id`, `total_amount`, `status`, `receiver_info`, `payment_time`, `create_time`, `update_time`) VALUES
                                                                                                                                          ('ORD202512010001', 1, 10797.00, 1, '{
                                                                                                                                            "receiverName": "张三",
                                                                                                                                            "receiverPhone": "13800001111",
                                                                                                                                            "province": "广东省",
                                                                                                                                            "city": "广州市",
                                                                                                                                            "district": "天河区",
                                                                                                                                            "detail": "天河路1号"
                                                                                                                                          }', NOW(), NOW(), NOW()),
                                                                                                                                          ('ORD202512010002', 2, 7999.00, 0, '{
                                                                                                                                            "receiverName": "李四",
                                                                                                                                            "receiverPhone": "13900002222",
                                                                                                                                            "province": "上海市",
                                                                                                                                            "city": "上海市",
                                                                                                                                            "district": "浦东新区",
                                                                                                                                            "detail": "世纪大道3号"
                                                                                                                                          }', NULL, NOW(), NOW());

-- 插入订单明细数据
INSERT INTO `order_items` (`order_id`, `product_id`, `product_name`, `price`, `quantity`, `image_url`, `create_time`, `update_time`) VALUES
                                                                                                                                         (1, 1, 'iPhone 15 Pro Max', 9999.00, 1, 'http://example.com/images/iphone15.jpg', NOW(), NOW()),
                                                                                                                                         (1, 5, '美的电饭煲', 399.00, 2, 'http://example.com/images/dianfanbao.jpg', NOW(), NOW()),
                                                                                                                                         (2, 2, '华为 Mate 60 Pro', 7999.00, 1, 'http://example.com/images/mate60.jpg', NOW(), NOW());

-- 友情提示：
-- 1. `users` 表中的 `password` 字段通常需要加密存储。我这里用了一个占位符 `$2a$10$e.g.encoded.password.for.zhangsan`。
--    在实际应用中，您应该使用 Spring Security 或其他加密库来生成真实的加密密码。
-- 2. `receiver_info` 字段是 JSON 类型，我提供了示例的 JSON 字符串。
-- 3. `image_url` 字段是示例图片地址，请根据实际情况替换。
-- 4. `NOW()` 函数用于自动插入当前的日期和时间。
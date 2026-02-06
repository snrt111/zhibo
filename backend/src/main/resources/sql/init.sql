-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS zhibo DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用zhibo数据库
USE zhibo;

-- 创建用户表
CREATE TABLE IF NOT EXISTS `user` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(100) NOT NULL COMMENT '密码',
  `nickname` VARCHAR(50) NOT NULL COMMENT '昵称',
  `email` VARCHAR(100) COMMENT '邮箱',
  `phone` VARCHAR(20) COMMENT '手机号',
  `avatar` VARCHAR(255) COMMENT '头像',
  `gender` TINYINT(1) DEFAULT 0 COMMENT '性别：0-未知，1-男，2-女',
  `user_type` TINYINT(1) DEFAULT 0 COMMENT '用户类型：0-普通用户，1-主播，2-管理员',
  `status` TINYINT(1) DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`),
  UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 创建直播表
CREATE TABLE IF NOT EXISTS `live` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '直播ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '主播ID',
  `title` VARCHAR(100) NOT NULL COMMENT '直播标题',
  `description` TEXT COMMENT '直播描述',
  `cover` VARCHAR(255) COMMENT '直播封面',
  `category_id` BIGINT(20) COMMENT '分类ID',
  `stream_key` VARCHAR(100) NOT NULL COMMENT '推流密钥',
  `status` TINYINT(1) DEFAULT 0 COMMENT '状态：0-未开始，1-直播中，2-已结束',
  `view_count` BIGINT(20) DEFAULT 0 COMMENT '观看人数',
  `start_time` DATETIME COMMENT '开始时间',
  `end_time` DATETIME COMMENT '结束时间',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='直播表';

-- 创建直播分类表
CREATE TABLE IF NOT EXISTS `category` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
  `icon` VARCHAR(255) COMMENT '分类图标',
  `sort` INT(11) DEFAULT 0 COMMENT '排序',
  `status` TINYINT(1) DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='直播分类表';

-- 创建弹幕表
CREATE TABLE IF NOT EXISTS `danmaku` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '弹幕ID',
  `live_id` BIGINT(20) NOT NULL COMMENT '直播ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `content` VARCHAR(200) NOT NULL COMMENT '弹幕内容',
  `color` VARCHAR(20) DEFAULT '#FFFFFF' COMMENT '弹幕颜色',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_live_id` (`live_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='弹幕表';

-- 创建礼物表
CREATE TABLE IF NOT EXISTS `gift` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '礼物ID',
  `name` VARCHAR(50) NOT NULL COMMENT '礼物名称',
  `price` DECIMAL(10,2) NOT NULL COMMENT '礼物价格',
  `icon` VARCHAR(255) COMMENT '礼物图标',
  `animation` VARCHAR(255) COMMENT '礼物动画',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='礼物表';

-- 创建礼物记录表
CREATE TABLE IF NOT EXISTS `gift_record` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `live_id` BIGINT(20) NOT NULL COMMENT '直播ID',
  `from_user_id` BIGINT(20) NOT NULL COMMENT '赠送用户ID',
  `to_user_id` BIGINT(20) NOT NULL COMMENT '接收用户ID',
  `gift_id` BIGINT(20) NOT NULL COMMENT '礼物ID',
  `count` INT(11) NOT NULL DEFAULT 1 COMMENT '礼物数量',
  `total_amount` DECIMAL(10,2) NOT NULL COMMENT '总金额',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_live_id` (`live_id`),
  KEY `idx_from_user_id` (`from_user_id`),
  KEY `idx_to_user_id` (`to_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='礼物记录表';

-- 创建关注表
CREATE TABLE IF NOT EXISTS `follow` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '关注ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `follow_user_id` BIGINT(20) NOT NULL COMMENT '关注用户ID',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_follow` (`user_id`, `follow_user_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_follow_user_id` (`follow_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='关注表';

-- 创建评论表
CREATE TABLE IF NOT EXISTS `comment` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `live_id` BIGINT(20) NOT NULL COMMENT '直播ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `content` VARCHAR(500) NOT NULL COMMENT '评论内容',
  `parent_id` BIGINT(20) DEFAULT 0 COMMENT '父评论ID',
  `like_count` INT(11) DEFAULT 0 COMMENT '点赞数',
  `status` TINYINT(1) DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_live_id` (`live_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- 插入默认分类数据
INSERT INTO `category` (`name`, `sort`, `status`) VALUES
('游戏', 1, 1),
('娱乐', 2, 1),
('音乐', 3, 1),
('体育', 4, 1),
('教育', 5, 1);

-- 插入默认礼物数据
INSERT INTO `gift` (`name`, `price`, `icon`) VALUES
('鲜花', 1.00, 'flower.png'),
('掌声', 5.00, 'clap.png'),
('火箭', 100.00, 'rocket.png'),
('跑车', 500.00, 'car.png'),
('城堡', 1000.00, 'castle.png');

-- 插入默认管理员账号 (密码: 123456)
INSERT INTO `user` (`username`, `password`, `nickname`, `user_type`, `status`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '管理员', 2, 1);

-- 插入默认主播账号 (密码: 123456)
INSERT INTO `user` (`username`, `password`, `nickname`, `user_type`, `status`) VALUES
('anchor1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '主播1号', 1, 1),
('anchor2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '主播2号', 1, 1);

-- 插入默认普通用户账号 (密码: 123456)
INSERT INTO `user` (`username`, `password`, `nickname`, `user_type`, `status`) VALUES
('user1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '用户1号', 0, 1),
('user2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '用户2号', 0, 1);

-- 插入测试直播数据
INSERT INTO `live` (`user_id`, `title`, `description`, `cover`, `category_id`, `stream_key`, `status`, `view_count`) VALUES
(2, '主播1号的游戏直播', '欢迎来到我的游戏直播间，今天玩点好玩的！', 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=live%20streaming%20game%20cover&image_size=square', 1, 'stream_key_001', 1, 1520),
(3, '主播2号的音乐秀', '一起来听歌放松吧！', 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=live%20streaming%20music%20concert&image_size=square', 3, 'stream_key_002', 1, 2340);

-- 插入测试弹幕数据
INSERT INTO `danmaku` (`live_id`, `user_id`, `content`, `color`) VALUES
(1, 4, '主播好厉害！', '#FFFFFF'),
(1, 5, '666666', '#FF0000'),
(2, 4, '好听！', '#FFFFFF'),
(2, 5, '再来一首', '#00FF00');
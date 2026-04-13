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
  `user_type` TINYINT(1) DEFAULT 0 COMMENT '用户类型：0-普通用户，1-管理员',
  `status` TINYINT(1) DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
  `openid` VARCHAR(64) COMMENT '第三方开放平台ID',
  `unionid` VARCHAR(64) COMMENT '第三方平台UnionID',
  `provider` VARCHAR(20) COMMENT '第三方平台标识(wechat/qq/weibo)',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`),
  UNIQUE KEY `uk_phone` (`phone`),
  UNIQUE KEY `uk_openid_provider` (`openid`, `provider`)
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
  `font_size` INT(11) DEFAULT 1 COMMENT '字体大小 0-小 1-中 2-大',
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
  `is_hot` TINYINT(1) DEFAULT 0 COMMENT '是否热门：0-否，1-是',
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

-- 创建提现申请表
CREATE TABLE IF NOT EXISTS `withdraw` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '提现ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `amount` DECIMAL(10,2) NOT NULL COMMENT '提现金额',
  `balance_before` DECIMAL(10,2) NOT NULL COMMENT '提现前余额',
  `balance_after` DECIMAL(10,2) NOT NULL COMMENT '提现后余额',
  `status` TINYINT(1) DEFAULT 0 COMMENT '状态：0-待审核，1-审核通过，2-审核拒绝，3-已打款',
  `bank_card` VARCHAR(50) COMMENT '银行卡号',
  `bank_name` VARCHAR(100) COMMENT '银行名称',
  `account_name` VARCHAR(50) COMMENT '开户人姓名',
  `audit_user_id` BIGINT(20) COMMENT '审核人ID',
  `audit_time` DATETIME COMMENT '审核时间',
  `audit_remark` VARCHAR(500) COMMENT '审核备注',
  `pay_time` DATETIME COMMENT '打款时间',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提现申请表';

-- 创建用户余额表
CREATE TABLE IF NOT EXISTS `user_balance` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '余额ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `total_income` DECIMAL(10,2) DEFAULT 0 COMMENT '总收入',
  `available_balance` DECIMAL(10,2) DEFAULT 0 COMMENT '可用余额',
  `frozen_balance` DECIMAL(10,2) DEFAULT 0 COMMENT '冻结余额',
  `total_withdraw` DECIMAL(10,2) DEFAULT 0 COMMENT '总提现金额',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户余额表';

-- 创建充值表
CREATE TABLE IF NOT EXISTS `recharge` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '充值ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `amount` DECIMAL(10,2) NOT NULL COMMENT '充值金额',
  `payment_method` VARCHAR(50) NOT NULL COMMENT '支付方式',
  `transaction_id` VARCHAR(100) NOT NULL COMMENT '交易ID',
  `status` INT NOT NULL DEFAULT 0 COMMENT '状态：0-待支付，1-已支付，2-支付失败',
  `created_at` DATETIME NOT NULL COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL COMMENT '更新时间',
  `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='充值表';

-- 创建支付表
CREATE TABLE IF NOT EXISTS `payment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '支付ID',
  `order_id` VARCHAR(50) NOT NULL COMMENT '订单ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `amount` DECIMAL(10,2) NOT NULL COMMENT '支付金额',
  `payment_method` VARCHAR(50) NOT NULL COMMENT '支付方式',
  `payment_status` INT NOT NULL DEFAULT 0 COMMENT '支付状态：0-待支付，1-已支付，2-支付失败',
  `transaction_id` VARCHAR(100) DEFAULT NULL COMMENT '第三方交易ID',
  `callback_data` TEXT COMMENT '回调数据',
  `created_at` DATETIME NOT NULL COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL COMMENT '更新时间',
  `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_id` (`order_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_payment_method` (`payment_method`),
  KEY `idx_payment_status` (`payment_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付表';

-- 创建支付配置表
CREATE TABLE IF NOT EXISTS `payment_config` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `config_key` VARCHAR(50) NOT NULL COMMENT '配置键',
  `config_value` TEXT NOT NULL COMMENT '配置值',
  `payment_method` VARCHAR(50) NOT NULL COMMENT '支付方式',
  `status` INT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `created_at` DATETIME NOT NULL COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_payment_method` (`payment_method`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付配置表';

-- 创建举报表
CREATE TABLE IF NOT EXISTS `report` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '举报ID',
  `reporter_id` BIGINT(20) NOT NULL COMMENT '举报人ID',
  `target_type` TINYINT(1) NOT NULL COMMENT '举报类型：1-直播，2-用户，3-评论，4-弹幕',
  `target_id` BIGINT(20) NOT NULL COMMENT '举报目标ID',
  `reason` VARCHAR(500) NOT NULL COMMENT '举报原因',
  `description` TEXT COMMENT '详细描述',
  `evidence` VARCHAR(500) COMMENT '证据图片',
  `status` TINYINT(1) DEFAULT 0 COMMENT '状态：0-待处理，1-已处理（有效），2-已处理（无效）',
  `handler_id` BIGINT(20) COMMENT '处理人ID',
  `handle_time` DATETIME COMMENT '处理时间',
  `handle_result` VARCHAR(500) COMMENT '处理结果',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_reporter_id` (`reporter_id`),
  KEY `idx_target` (`target_type`, `target_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='举报表';

-- 创建内容审核表
CREATE TABLE IF NOT EXISTS `content_audit` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '审核ID',
  `content_type` TINYINT(1) NOT NULL COMMENT '内容类型：1-直播，2-评论，3-弹幕',
  `content_id` BIGINT(20) NOT NULL COMMENT '内容ID',
  `content` TEXT COMMENT '审核内容',
  `status` TINYINT(1) DEFAULT 0 COMMENT '状态：0-待审核，1-审核通过，2-审核拒绝',
  `auditor_id` BIGINT(20) COMMENT '审核人ID',
  `audit_time` DATETIME COMMENT '审核时间',
  `audit_result` VARCHAR(500) COMMENT '审核结果',
  `auto_audit` TINYINT(1) DEFAULT 0 COMMENT '是否自动审核：0-人工，1-自动',
  `risk_level` TINYINT(1) DEFAULT 0 COMMENT '风险等级：0-无风险，1-低风险，2-中风险，3-高风险',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_content` (`content_type`, `content_id`),
  KEY `idx_status` (`status`),
  KEY `idx_risk_level` (`risk_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容审核表';

-- 创建敏感词表
CREATE TABLE IF NOT EXISTS `sensitive_word` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '敏感词ID',
  `word` VARCHAR(100) NOT NULL COMMENT '敏感词',
  `category` TINYINT(1) DEFAULT 0 COMMENT '分类：0-通用，1-政治，2-色情，3-暴力，4-广告',
  `level` TINYINT(1) DEFAULT 1 COMMENT '风险等级：1-低，2-中，3-高',
  `enabled` TINYINT(1) DEFAULT 1 COMMENT '是否启用',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_word` (`word`),
  KEY `idx_category` (`category`),
  KEY `idx_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='敏感词表';

-- 创建AI模型配置表
CREATE TABLE IF NOT EXISTS `ai_model_config` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `name` VARCHAR(100) NOT NULL COMMENT '模型名称',
  `type` VARCHAR(50) NOT NULL COMMENT '模型类型：ollama/thirdparty',
  `base_url` VARCHAR(500) NOT NULL COMMENT 'API基础地址',
  `api_key` VARCHAR(500) COMMENT 'API密钥',
  `model_name` VARCHAR(100) NOT NULL COMMENT '模型标识',
  `status` TINYINT(1) DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `priority` INT(11) DEFAULT 0 COMMENT '优先级',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI模型配置表';

-- 创建生成图片记录表
CREATE TABLE IF NOT EXISTS `generated_image` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '图片ID',
  `user_id` BIGINT(20) COMMENT '用户ID',
  `prompt` TEXT NOT NULL COMMENT '生成提示词',
  `image_url` VARCHAR(500) NOT NULL COMMENT '图片URL',
  `model_id` BIGINT(20) COMMENT '使用的模型ID',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='生成图片记录表';

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
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '管理员', 1, 1);

-- 插入默认普通用户账号 (密码: 123456)
INSERT INTO `user` (`username`, `password`, `nickname`, `user_type`, `status`) VALUES
('user1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '用户1号', 0, 1),
('user2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '用户2号', 0, 1),
('user3', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '用户3号', 0, 1),
('user4', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '用户4号', 0, 1);

-- 插入测试直播数据
INSERT INTO `live` (`user_id`, `title`, `description`, `cover`, `category_id`, `stream_key`, `status`, `view_count`) VALUES
(2, '用户1号的游戏直播', '欢迎来到我的游戏直播间，今天玩点好玩的！', 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=live%20streaming%20game%20cover&image_size=square', 1, 'stream_key_001', 1, 1520),
(3, '用户2号的音乐秀', '一起来听歌放松吧！', 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=live%20streaming%20music%20concert&image_size=square', 3, 'stream_key_002', 1, 2340);

-- 插入测试弹幕数据
INSERT INTO `danmaku` (`live_id`, `user_id`, `content`, `color`) VALUES
(1, 4, '主播好厉害！', '#FFFFFF'),
(1, 5, '666666', '#FF0000'),
(2, 4, '好听！', '#FFFFFF'),
(2, 5, '再来一首', '#00FF00');

-- 插入默认敏感词
INSERT INTO `sensitive_word` (`word`, `category`, `level`) VALUES
('色情', 2, 3),
('暴力', 3, 3),
('赌博', 3, 3),
('毒品', 3, 3),
('诈骗', 3, 3),
('代刷', 4, 2),
('加微信', 4, 2),
('加QQ', 4, 2);

-- 为测试主播用户创建余额
INSERT INTO `user_balance` (`user_id`, `total_income`, `available_balance`) VALUES
(2, 1500.00, 1500.00),
(3, 2300.00, 2300.00);

-- 插入默认AI模型配置（Ollama本地模型示例）
INSERT INTO `ai_model_config` (`name`, `type`, `base_url`, `model_name`, `status`, `priority`) VALUES
('Ollama本地模型', 'ollama', 'http://host.docker.internal:11434', 'llava', 1, 1),
('DeepSeek', 'thirdparty', 'https://api.deepseek.com/v1/images/generations', 'deepseek-chat', 0, 2),
('百炼', 'thirdparty', 'https://dashscope.aliyuncs.com/api/v1/services/aigc/text2image/image-synthesis', 'wanx-v1', 0, 3);

-- 创建观看历史表
CREATE TABLE IF NOT EXISTS `watch_history` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `live_id` BIGINT(20) NOT NULL COMMENT '直播ID',
  `live_title` VARCHAR(100) COMMENT '直播标题',
  `live_cover` VARCHAR(255) COMMENT '直播封面',
  `anchor_id` BIGINT(20) COMMENT '主播ID',
  `anchor_name` VARCHAR(50) COMMENT '主播名称',
  `watch_duration` INT(11) DEFAULT 0 COMMENT '观看时长（秒）',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_live_id` (`live_id`),
  KEY `idx_user_live` (`user_id`, `live_id`),
  KEY `idx_updated_at` (`updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='观看历史表';

-- 插入默认支付配置
INSERT INTO `payment_config` (`config_key`, `config_value`, `payment_method`, `status`, `created_at`, `updated_at`) VALUES
('enabled', 'true', 'alipay', 1, NOW(), NOW()),
('enabled', 'true', 'wechat', 1, NOW(), NOW()),
('enabled', 'true', 'bank', 1, NOW(), NOW());

-- 创建用户通知设置表
CREATE TABLE IF NOT EXISTS `user_notification_setting` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '设置ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `follow_live_start` TINYINT(1) DEFAULT 1 COMMENT '关注主播开播提醒：0-关闭，1-开启',
  `system_notification` TINYINT(1) DEFAULT 1 COMMENT '系统通知：0-关闭，1-开启',
  `gift_notification` TINYINT(1) DEFAULT 1 COMMENT '礼物通知：0-关闭，1-开启',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  KEY `idx_follow_live_start` (`follow_live_start`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户通知设置表';

-- 创建开播提醒记录表
CREATE TABLE IF NOT EXISTS `live_start_notification` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '接收通知的用户ID',
  `anchor_id` BIGINT(20) NOT NULL COMMENT '主播ID',
  `live_id` BIGINT(20) COMMENT '直播ID（开播后填充）',
  `live_title` VARCHAR(100) COMMENT '直播标题',
  `live_cover` VARCHAR(255) COMMENT '直播封面',
  `status` TINYINT(1) DEFAULT 0 COMMENT '状态：0-待发送，1-已发送，2-已读',
  `sent_at` DATETIME COMMENT '发送时间',
  `read_at` DATETIME COMMENT '读取时间',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_anchor_id` (`anchor_id`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='开播提醒记录表';

-- 为现有用户创建默认通知设置
INSERT INTO `user_notification_setting` (`user_id`, `follow_live_start`, `system_notification`, `gift_notification`)
SELECT `id`, 1, 1, 1 FROM `user` WHERE `id` NOT IN (SELECT `user_id` FROM `user_notification_setting`);

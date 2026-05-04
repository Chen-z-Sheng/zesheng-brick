-- ==============================
-- 用户表
-- ==============================
CREATE TABLE IF NOT EXISTS `client_user`
(
    `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `openid`          VARCHAR(64)     NULL COMMENT '微信openid',
    `donut_user_id`   VARCHAR(128)    NULL COMMENT '微信多端身份管理 user_id',
    `session_key`     VARCHAR(128)    NULL COMMENT '微信session_key',
    `nick_name`       VARCHAR(60)     NULL COMMENT '用户昵称',
    `avatar_url`      VARCHAR(255)    NULL COMMENT '头像URL',
    `phone`           VARCHAR(20)     NULL COMMENT '手机号',
    `invite_code`     VARCHAR(6)      NULL COMMENT '邀请码',
    `inviter_user_id` BIGINT UNSIGNED NULL COMMENT '直邀上级用户ID',
    `invite_path`     VARCHAR(1024)   NULL COMMENT '邀请路径',
    `status`          TINYINT         NOT NULL DEFAULT 1 COMMENT '账号状态：1=启用，0=禁用',
    `created_at`      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_client_user_openid` (`openid`),
    UNIQUE KEY `uk_client_user_donut_user_id` (`donut_user_id`),
    UNIQUE KEY `uk_client_user_invite_code` (`invite_code`),
    KEY `idx_client_user_inviter_user_id` (`inviter_user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT = '用户端-用户表';


-- ==============================
-- 用户寄件人信息表
-- ==============================
CREATE TABLE IF NOT EXISTS `client_user_senders`
(
    `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `user_id`     BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `name`        VARCHAR(32)     NOT NULL COMMENT '寄件人姓名',
    `phone`       VARCHAR(20)     NOT NULL COMMENT '寄件人手机号',
    `use_count`   INT             NOT NULL DEFAULT 0 COMMENT '被填写报单使用次数，用于列表排序',
    `created_at`  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_user_senders_user_id` (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT = '用户端-寄件人信息';

-- ==============================
-- 用户收款信息表
-- ==============================
CREATE TABLE IF NOT EXISTS `client_user_payment_info`
(
    `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `user_id`         BIGINT UNSIGNED NOT NULL COMMENT '关联用户ID',
    `real_name`       VARCHAR(32)     NULL COMMENT '真实姓名',
    `alipay_account`  VARCHAR(64)     NULL COMMENT '支付宝账号',
    `wechat_qrcode`   VARCHAR(512)    NULL COMMENT '微信收款码图片URL',
    `alipay_qrcode`   VARCHAR(512)    NULL COMMENT '支付宝收款码图片URL',
    `bank_card_no`    VARCHAR(32)     NULL COMMENT '银行卡号',
    `bank_name`       VARCHAR(64)     NULL COMMENT '开户银行',
    `bank_branch`     VARCHAR(128)    NULL COMMENT '开户支行',
    `created_at`      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_payment_info_user_id` (`user_id`),
    CONSTRAINT `fk_payment_info_user` FOREIGN KEY (`user_id`) REFERENCES `client_user` (`id`) ON DELETE CASCADE,
    KEY `idx_payment_info_user_id` (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT = '用户端-用户收款信息表';

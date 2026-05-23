-- ==============================
-- 管理端用户表
-- ==============================
CREATE TABLE `admin_user`
(
    `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `username`      VARCHAR(60)     NOT NULL COMMENT '用户名',
    `password_hash` VARCHAR(255)    NOT NULL COMMENT '密码哈希',
    `role_id`       BIGINT UNSIGNED NULL COMMENT '所属角色ID',
    `phone`         VARCHAR(20)     NULL COMMENT '手机号',
    `avatar_url`    VARCHAR(255)    NULL NULL COMMENT '头像链接',
    `status`        TINYINT         NOT NULL DEFAULT 1 COMMENT '账号状态：1=启用，0=禁用',
    `create_by`     BIGINT          NULL NULL COMMENT '创建人ID',
    `update_by`     BIGINT          NULL NULL COMMENT '最后更新人ID',
    `last_login_at` DATETIME        NULL COMMENT '最后登录时间',
    `last_login_ip` VARCHAR(45)     NULL COMMENT '最后登录IP',
    `deleted_at`    DATETIME        NULL COMMENT '软删除时间',
    `remark`        VARCHAR(500)    NULL COMMENT '备注',
    `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_phone` (`phone`),
    KEY `idx_admin_user_role` (`role_id`),
    CONSTRAINT `fk_admin_user_role` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT = '管理端-用户表';

-- ==============================
-- 帮助中心FAQ表
-- ==============================
CREATE TABLE IF NOT EXISTS `admin_help_faq`
(
    `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `question`   VARCHAR(256)    NOT NULL COMMENT '问题',
    `answer`     TEXT            NOT NULL COMMENT '答案',
    `sort_order` INT             NOT NULL DEFAULT 0 COMMENT '排序号，升序',
    `status`     TINYINT         NOT NULL DEFAULT 1 COMMENT '状态：1=启用，0=禁用',
    `created_at` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY `idx_help_faq_status_sort` (`status`, `sort_order`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT = '管理端-帮助中心FAQ';

-- ==============================
-- 用户问题反馈表
-- ==============================
CREATE TABLE IF NOT EXISTS `admin_user_feedback`
(
    `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `user_id`       BIGINT UNSIGNED NOT NULL COMMENT '反馈用户ID',
    `feedback_type` VARCHAR(64)     NOT NULL COMMENT '反馈类型',
    `content`       VARCHAR(1000)   NOT NULL COMMENT '反馈内容',
    `image_urls`    JSON            NULL COMMENT '反馈图片URL列表',
    `status`        TINYINT         NOT NULL DEFAULT 0 COMMENT '状态：0待处理 1已处理',
    `reply_content` VARCHAR(1000)   NULL COMMENT '管理员回复内容',
    `replied_at`    DATETIME        NULL COMMENT '回复时间',
    `reply_by`      BIGINT UNSIGNED NULL COMMENT '回复管理员ID',
    `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_at`    DATETIME        NULL COMMENT '软删除时间',
    INDEX `idx_user_feedback_user_time` (`user_id`, `created_at`),
    INDEX `idx_user_feedback_type` (`feedback_type`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT = '用户问题反馈表';

-- ==============================
-- 公告表
-- ==============================
CREATE TABLE IF NOT EXISTS `admin_announcement`
(
    `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `title`      VARCHAR(128)    NOT NULL COMMENT '公告标题',
    `content`    TEXT            NOT NULL COMMENT '公告内容（富文本HTML）',
    `status`     TINYINT         NOT NULL DEFAULT 0 COMMENT '状态：1=启用，0=未启用',
    `created_at` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY `idx_announcement_status_updated` (`status`, `updated_at`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT = '管理端-公告表';

-- ==============================
-- 公告忽略记录表
-- ==============================
CREATE TABLE IF NOT EXISTS `admin_announcement_ignore`
(
    `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `announcement_id` BIGINT UNSIGNED NOT NULL COMMENT '公告ID',
    `openid`          VARCHAR(64)     NOT NULL COMMENT '用户openid',
    `created_at`      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY `uk_announcement_ignore_ann_openid` (`announcement_id`, `openid`),
    KEY `idx_announcement_ignore_openid` (`openid`),
    CONSTRAINT `fk_admin_announcement_ignore_announcement`
        FOREIGN KEY (`announcement_id`) REFERENCES `admin_announcement` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT = '管理端-公告忽略记录表';

-- ==============================
-- 固结表单提交记录
-- ==============================
CREATE TABLE IF NOT EXISTS `admin_form_submissions`
(
    `id`                  BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT COMMENT '提交记录ID',
    `scheme_id`           BIGINT UNSIGNED NOT NULL COMMENT '关联方案ID',
    `user_id`             BIGINT UNSIGNED NOT NULL COMMENT '提交人ID',
    `data_json`           JSON            NOT NULL COMMENT '表单提交数据(JSON)',
    `quantity`            INT             NOT NULL DEFAULT 1 COMMENT '提交时的数量',
    `settled_amount`      DECIMAL(10, 2)  NULL COMMENT '结算金额',
    `status`              TINYINT         NOT NULL DEFAULT 1 COMMENT '状态：0=草稿 1=已提交 2=运输中 3=入库中 4=已打款 5=异常 6=已退货',
    `settled_at`          DATETIME        NULL COMMENT '结算/回款时间',
    `settled_proof_url`   VARCHAR(255)    NULL COMMENT '回款凭证截图URL',
    `settled_proof_urls`  JSON            NULL COMMENT '回款凭证截图URL列表',
    `admin_internal_note` VARCHAR(255)    NULL COMMENT '管理员内部备注',
    `created_at`          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_at`          DATETIME        NULL COMMENT '软删除时间',
    CHECK (JSON_VALID(data_json)),
    CONSTRAINT `fk_admin_form_submission_scheme` FOREIGN KEY (`scheme_id`) REFERENCES `admin_form_schemes` (`id`) ON DELETE RESTRICT,
    INDEX `idx_submission_user_time` (`user_id`, `created_at`),
    INDEX `idx_submission_scheme_time` (`scheme_id`, `created_at`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT = '管理端-固结表单提交记录';

-- ==============================
-- 行情报单提交记录
-- ==============================
CREATE TABLE IF NOT EXISTS `admin_sell_order_submissions`
(
    `id`                  BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    `user_id`             BIGINT UNSIGNED NOT NULL COMMENT '提交人ID',
    `sender_name`         VARCHAR(32)     NOT NULL COMMENT '寄件人姓名',
    `sender_phone`        VARCHAR(20)     NOT NULL COMMENT '寄件人手机号',
    `logistics_company`   VARCHAR(64)     NOT NULL COMMENT '物流公司',
    `logistics_no`        VARCHAR(512)    NOT NULL COMMENT '寄件单号（多个以英文逗号分隔）',
    `storage`             TINYINT         NOT NULL DEFAULT 0 COMMENT '是否寄存：0=否 1=是',
    `storage_date`        DATE            NULL COMMENT '寄存日期',
    `remark`              VARCHAR(500)    NULL COMMENT '用户备注',
    `items_json`          JSON            NOT NULL COMMENT '商品明细：[{productName,price,quantity}]',
    `status`              TINYINT         NOT NULL DEFAULT 1 COMMENT '状态：0=草稿 1=已提交 2=运输中 3=入库中 4=已打款 5=异常 6=已退货',
    `settled_proof_urls`  JSON            NULL COMMENT '回款凭证截图URL列表',
    `settled_amount`      DECIMAL(12, 2)  NULL COMMENT '回款金额',
    `admin_internal_note` VARCHAR(500)    NULL COMMENT '管理员内部备注',
    `created_at`          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_at`          DATETIME        NULL COMMENT '软删除时间',
    INDEX `idx_sell_order_user_time` (`user_id`, `created_at`),
    INDEX `idx_sell_order_status` (`status`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT = '管理端-行情报单提交记录';

-- ==============================
-- 下单地址表
-- ==============================
CREATE TABLE `admin_delivery_addresses`
(
    `id`          BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT COMMENT '地址ID',
    `name`        VARCHAR(100) NOT NULL COMMENT '地址名称/备注',
    `full_address` VARCHAR(500) NOT NULL COMMENT '完整地址',
    `sort_order`  INT          NOT NULL DEFAULT 0 COMMENT '排序号',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：0=禁用 1=启用',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_status_sort` (`status`, `sort_order`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT = '管理端-下单地址';

-- ==============================
-- 方案表
-- ==============================
CREATE TABLE `admin_form_schemes`
(
    `id`          BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT COMMENT '方案ID',
    `name`        VARCHAR(150)    NOT NULL COMMENT '方案名称',
    `address_id`  BIGINT UNSIGNED NULL COMMENT '关联下单地址ID',
    `description` VARCHAR(255)    NULL COMMENT '方案说明',
    `status`      TINYINT         NOT NULL DEFAULT 1 COMMENT '状态：0=停用 1=启用 2=草稿',
    `unit_price`  DECIMAL(10, 2)  NOT NULL COMMENT '每单结算金额',
    `created_at`  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_status` (`status`),
    CONSTRAINT `fk_scheme_address` FOREIGN KEY (`address_id`) REFERENCES `admin_delivery_addresses` (`id`) ON DELETE SET NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT = '管理端-报单方案';

-- ==============================
-- 管理端-回收品类一级分类表
-- ==============================
CREATE TABLE IF NOT EXISTS `admin_category_level1`
(
    `id`         INT UNSIGNED    NOT NULL AUTO_INCREMENT COMMENT '一级分类ID',
    `name`       VARCHAR(50)     NOT NULL COMMENT '大分类名称',
    `sort_order` INT             NOT NULL DEFAULT 0 COMMENT '排序号，升序',
    `status`     TINYINT         NOT NULL DEFAULT 1 COMMENT '状态：1=启用，0=禁用',
    `created_at` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT = '管理端-回收品类一级分类表';

-- ==============================
-- 管理端-回收品类二级分类表
-- ==============================
CREATE TABLE IF NOT EXISTS `admin_category_level2`
(
    `id`         INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '二级分类ID（主键）',
    `level1_id`  INT UNSIGNED NOT NULL COMMENT '关联一级分类ID',
    `name`       VARCHAR(50)  NOT NULL COMMENT '次分类名称',
    `sort_order` INT          NOT NULL DEFAULT 0 COMMENT '排序号，升序',
    `status`     TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1=启用，0=禁用',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_level1_name` (`level1_id`, `name`),
    CONSTRAINT `fk_admin_category_level2_level1` FOREIGN KEY (`level1_id`) REFERENCES `admin_category_level1` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT = '管理端-回收品类二级分类表';

-- ==============================
-- 管理端-回收品类三级分类表
-- ==============================
CREATE TABLE IF NOT EXISTS `admin_category_level3`
(
    `id`         INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '三级分类ID（主键）',
    `level2_id`  INT UNSIGNED NOT NULL COMMENT '关联二级分类ID',
    `name`       VARCHAR(50)  NOT NULL COMMENT '三级分类名称',
    `sort_order` INT          NOT NULL DEFAULT 0 COMMENT '排序号，升序',
    `status`     TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1=启用，0=禁用',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_level2_name` (`level2_id`, `name`),
    CONSTRAINT `fk_admin_category_level3_level2` FOREIGN KEY (`level2_id`) REFERENCES `admin_category_level2` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT = '管理端-回收品类三级分类表';

-- ==============================
-- 管理端-回收行情记录表
-- ==============================
CREATE TABLE IF NOT EXISTS `admin_recycle_price`
(
    `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '行情记录ID（主键）',
    `level3_id`     INT UNSIGNED    NOT NULL COMMENT '关联三级分类ID',
    `price_date`    DATE            NOT NULL COMMENT '行情日期',
    `recycle_price` DECIMAL(10, 2)  NOT NULL COMMENT '回收价格',
    `remark`        VARCHAR(200)    DEFAULT '' COMMENT '备注',
    `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_level3_date` (`level3_id`, `price_date`),
    KEY `idx_price_date` (`price_date`),
    CONSTRAINT `fk_admin_recycle_price_level3` FOREIGN KEY (`level3_id`) REFERENCES `admin_category_level3` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT = '管理端-回收行情记录表';

-- ==============================
-- 旧库结构清理（如历史版本包含 level2_id）
-- ==============================
-- 执行前可先备份表结构；以下语句在不存在对应对象时可按需跳过
-- ALTER TABLE `admin_recycle_price` DROP FOREIGN KEY `fk_admin_recycle_price_level2`;
-- ALTER TABLE `admin_recycle_price` DROP INDEX `uk_level2_date`;
-- ALTER TABLE `admin_recycle_price` DROP COLUMN `level2_id`;

-- ==============================
-- 管理端-物流公司字典（小程序行情报单联想，与 C 端只读共用）
-- ==============================
CREATE TABLE IF NOT EXISTS `admin_logistics_company`
(
    `id`         BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`       VARCHAR(100)    NOT NULL COMMENT '公司名称',
    `code`       VARCHAR(50)     NULL COMMENT '公司代码',
    `sort`       INT             DEFAULT 0 COMMENT '排序',
    `status`     TINYINT(1)      NOT NULL DEFAULT 1 COMMENT '状态：1=启用，0=禁用',
    `created_at` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_admin_logistics_name` (`name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT = '管理端-物流公司字典';
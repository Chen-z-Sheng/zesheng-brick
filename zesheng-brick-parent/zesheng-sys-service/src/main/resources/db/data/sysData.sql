-- ==============================
-- 角色表
-- ==============================
CREATE TABLE `sys_role` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` VARCHAR(64) NOT NULL COMMENT '角色名称（用于界面显示）',
  `code` VARCHAR(64) NOT NULL COMMENT '角色编码（英文/下划线）',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1=启用，0=禁用',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '备注/说明',
  `deleted_at` DATETIME NULL COMMENT '软删除时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`code`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '系统端-角色表';

-- ==============================
-- 权限点表
-- ==============================
CREATE TABLE `sys_permission` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `code` VARCHAR(150) NOT NULL COMMENT '权限码：<资源>:<动作>，如 user:create；唯一',
  `resource` VARCHAR(100) NOT NULL COMMENT '资源名/模块名，如 user/order/form',
  `action` VARCHAR(50) NOT NULL COMMENT '动作，如 list/read/create/update/delete/export/approve',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '权限点说明（用于回显/帮助）',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_perm_code` (`code`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '系统端-权限表';

-- ==============================
-- 角色-权限关联表
-- ==============================
CREATE TABLE `sys_role_permission` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_id` BIGINT UNSIGNED NOT NULL COMMENT '角色ID',
  `permission_id` BIGINT UNSIGNED NOT NULL COMMENT '权限ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_perm` (`role_id`, `permission_id`),
  CONSTRAINT `fk_rp_role` FOREIGN KEY (`role_id`) REFERENCES `sys_role`(`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_rp_perm` FOREIGN KEY (`permission_id`) REFERENCES `sys_permission`(`id`) ON DELETE RESTRICT
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '系统端-角色与权限关联';

-- ==============================
-- 系统配置表
-- ==============================
CREATE TABLE `sys_config` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `key` VARCHAR(128) NOT NULL COMMENT '配置键（建议用点号分层，如 home.banner）',
  `value` LONGTEXT NOT NULL COMMENT '配置值原文，按 value_type 在应用层解析',
  `value_type` VARCHAR(16) NOT NULL DEFAULT 'json' COMMENT '值类型：json|string|number|boolean',
  `remark` VARCHAR(255) NULL COMMENT '备注说明',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_config_key` (`key`),
  KEY `idx_sys_config_updated_at` (`updated_at`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '系统配置表';

-- ==============================
-- 系统端-定时任务表
-- ==============================
CREATE TABLE IF NOT EXISTS `sys_job_task`
(
  `id`                   BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `job_name`             VARCHAR(100)    NOT NULL COMMENT '任务名称',
  `cron_expression`      VARCHAR(100)    NOT NULL COMMENT 'Cron表达式',
  `handler_name`         VARCHAR(100)    NOT NULL COMMENT '执行器Bean名称',
  `handler_param`        TEXT            NULL COMMENT '执行参数',
  `status`               TINYINT         NOT NULL DEFAULT 1 COMMENT '状态：1=启用，0=停用',
  `running`              TINYINT         NOT NULL DEFAULT 0 COMMENT '执行中标记：1=执行中，0=空闲',
  `last_execute_at`      DATETIME        NULL COMMENT '最后执行时间',
  `next_execute_at`      DATETIME        NULL COMMENT '下次执行时间',
  `last_execute_status`  TINYINT         NOT NULL DEFAULT 0 COMMENT '最后执行状态：1=成功，0=失败/未执行',
  `last_execute_message` VARCHAR(500)    NULL COMMENT '最后执行结果',
  `created_at`           DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`           DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_sys_job_task_status_next` (`status`, `next_execute_at`),
  KEY `idx_sys_job_task_running` (`running`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '系统端-定时任务表';

-- ==============================
-- 系统端-定时任务日志表
-- ==============================
CREATE TABLE IF NOT EXISTS `sys_job_task_log`
(
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `task_id`     BIGINT UNSIGNED NOT NULL COMMENT '任务ID',
  `job_name`    VARCHAR(100)    NOT NULL COMMENT '任务名称快照',
  `start_time`  DATETIME        NOT NULL COMMENT '开始时间',
  `end_time`    DATETIME        NOT NULL COMMENT '结束时间',
  `status`      TINYINT         NOT NULL COMMENT '执行状态：1=成功，0=失败',
  `message`     VARCHAR(500)    NULL COMMENT '执行结果信息',
  `error_stack` MEDIUMTEXT      NULL COMMENT '错误堆栈',
  `created_at`  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_sys_job_task_log_task_time` (`task_id`, `start_time`),
  CONSTRAINT `fk_sys_job_task_log_task_id`
    FOREIGN KEY (`task_id`) REFERENCES `sys_job_task` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '系统端-定时任务执行日志';

-- ==============================
-- 系统端-待办任务表
-- ==============================
CREATE TABLE IF NOT EXISTS `sys_todo_task`
(
  `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title`      VARCHAR(200)    NOT NULL COMMENT '任务标题',
  `content`    TEXT            NULL COMMENT '任务内容',
  `status`     TINYINT         NOT NULL DEFAULT 0 COMMENT '状态：0=待处理，1=已处理',
  `created_at` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_sys_todo_task_status` (`status`),
  KEY `idx_sys_todo_task_updated_at` (`updated_at`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '系统端-待办任务表';
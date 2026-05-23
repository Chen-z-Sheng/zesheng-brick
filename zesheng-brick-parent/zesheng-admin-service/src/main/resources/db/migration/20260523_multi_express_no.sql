-- =============================================================================
-- 多快递单号支持 · 数据库迁移脚本
-- 执行一次即可（线上 / 测试环境）
--
-- 变更说明：
--   1. 行情报单 logistics_no 扩展为 VARCHAR(512)，多个单号用英文逗号存储
--   2. 固结报单 data_json：expressNo 迁移为 expressNos 数组，并删除 expressNo
--
-- 执行前请确认当前库名（默认 zesheng_brick，与 application 配置一致）
-- =============================================================================

USE `zesheng_brick`;

-- -----------------------------------------------------------------------------
-- 1. 行情报单：扩展寄件单号字段
-- -----------------------------------------------------------------------------
ALTER TABLE `admin_sell_order_submissions`
    MODIFY COLUMN `logistics_no` VARCHAR(512) NOT NULL COMMENT '寄件单号（多个以英文逗号分隔）';

-- -----------------------------------------------------------------------------
-- 2. 固结报单：expressNo → expressNos（单号场景；你目前仅少量数据足够）
-- -----------------------------------------------------------------------------
UPDATE `admin_form_submissions`
SET `data_json` = JSON_REMOVE(
        JSON_SET(
                `data_json`,
                '$.expressNos',
                IF(
                        COALESCE(JSON_UNQUOTE(JSON_EXTRACT(`data_json`, '$.expressNo')), '') = '',
                        JSON_ARRAY(),
                        JSON_ARRAY(JSON_UNQUOTE(JSON_EXTRACT(`data_json`, '$.expressNo')))
                )
        ),
        '$.expressNo'
                  )
WHERE `deleted_at` IS NULL
  AND JSON_CONTAINS_PATH(`data_json`, 'one', '$.expressNo');

-- -----------------------------------------------------------------------------
-- 3. 可选校验
-- -----------------------------------------------------------------------------
-- SELECT id,
--        JSON_EXTRACT(`data_json`, '$.expressNos') AS express_nos,
--        JSON_CONTAINS_PATH(`data_json`, 'one', '$.expressNo') AS has_old_express_no
-- FROM `admin_form_submissions`
-- WHERE `deleted_at` IS NULL;
--
-- SELECT id, `logistics_no`, CHAR_LENGTH(`logistics_no`) AS no_len
-- FROM `admin_sell_order_submissions`
-- WHERE `deleted_at` IS NULL;

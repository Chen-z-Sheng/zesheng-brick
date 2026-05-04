package com.zesheng.client.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.zesheng.client.enums.SubmissionStatus;
import com.zesheng.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 用户端-表单提交记录
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "admin_form_submissions", autoResultMap = true)
@Schema(description = "用户端-表单提交记录")
public class FormSubmission extends BaseEntity {
    /**
     * 关联方案ID
     */
    private Long schemeId;

    /**
     * 方案名称（非表字段，查询时填充）
     */
    @TableField(exist = false)
    private String schemeName;

    /**
     * 每单结算金额（非表字段，由 schemeId 查 admin_form_schemes.unit_price 填充，应结金额=unitPrice*quantity）
     */
    @TableField(exist = false)
    private BigDecimal unitPrice;

    /**
     * 提交人ID
     */
    private Long userId;

    /**
     * 表单提交数据JSON
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> dataJson;

    /**
     * 提交时的数量
     */
    private Integer quantity = 1;

    /**
     * 结算金额
     */
    private BigDecimal settledAmount;

    /**
     * 提交状态
     */
    private SubmissionStatus status = SubmissionStatus.DRAFT;

    /**
     * 结算/回款时间
     */
    private LocalDateTime settledAt;

    /**
     * 回款凭证截图URL
     */
    private String settledProofUrl;

    /**
     * 管理员内部备注
     */
    private String adminInternalNote;

    /**
     * 软删除时间
     */
    private LocalDateTime deletedAt;
}
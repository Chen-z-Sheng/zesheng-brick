package com.zesheng.admin.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.zesheng.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 固结报单提交记录（与 admin_form_submissions 表对应）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "admin_form_submissions", autoResultMap = true)
@Schema(description = "固结报单提交记录")
public class FormSubmission extends BaseEntity {

    @Schema(description = "关联方案ID")
    private Long schemeId;

    @Schema(description = "提交人ID")
    private Long userId;

    @TableField(exist = false)
    @Schema(description = "提交人展示名：优先打款真实姓名，否则小程序昵称")
    private String displayName;

    @TableField(exist = false)
    @Schema(description = "方案名称（非表字段，由 schemeId 查询填充）")
    private String schemeName;

    @TableField(exist = false)
    @Schema(description = "每单结算金额（非表字段，由 schemeId 查 admin_form_schemes 填充，用于应结金额=unitPrice*quantity）")
    private BigDecimal unitPrice;

    @TableField(typeHandler = JacksonTypeHandler.class)
    @Schema(description = "表单扩展数据：giftDesc/expressNo/orderNoMain/orderNoGift/imageUrls/signDate/remark")
    private Map<String, Object> dataJson;

    @Schema(description = "提交数量")
    private Integer quantity = 1;

    @Schema(description = "结算金额")
    private BigDecimal settledAmount;

    @Schema(description = "状态：0草稿 1已提交 2运输中 7已签收 3入库中 4已打款 5异常 6已退货")
    private Integer status;

    @Schema(description = "结算/回款时间")
    private LocalDateTime settledAt;

    @Schema(description = "回款凭证截图URL（兼容旧数据，单条）")
    private String settledProofUrl;

    @TableField(value = "settled_proof_urls", typeHandler = JacksonTypeHandler.class)
    @Schema(description = "回款凭证截图URL列表（存 JSON 列）")
    private List<String> settledProofUrls;

    @Schema(description = "管理员内部备注")
    private String adminInternalNote;

    @Schema(description = "软删除时间")
    private LocalDateTime deletedAt;
}

package com.zesheng.admin.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.zesheng.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 行情报单提交记录（单表，商品明细存 items_json）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "admin_sell_order_submissions", autoResultMap = true)
@Schema(description = "行情报单提交记录")
public class SellOrderSubmission extends BaseEntity {

    @Schema(description = "提交人ID")
    private Long userId;

    @Schema(description = "寄件人姓名")
    private String senderName;

    @Schema(description = "寄件人手机号")
    private String senderPhone;

    @Schema(description = "物流公司")
    private String logisticsCompany;

    @Schema(description = "寄件单号")
    private String logisticsNo;

    @Schema(description = "是否寄存：0=否 1=是")
    private Integer storage;

    @Schema(description = "寄存日期")
    private LocalDate storageDate;

    @Schema(description = "用户备注")
    private String remark;

    @TableField(typeHandler = JacksonTypeHandler.class)
    @Schema(description = "商品明细")
    private List<Map<String, Object>> itemsJson;

    @Schema(description = "状态：0草稿 1已提交 2运输中 3入库中 4已打款 5异常 6已退货")
    private Integer status;

    @TableField(typeHandler = JacksonTypeHandler.class)
    @Schema(description = "回款凭证截图URL列表")
    private List<String> settledProofUrls;

    @Schema(description = "回款金额")
    private BigDecimal settledAmount;

    @Schema(description = "管理员内部备注")
    private String adminInternalNote;

    @Schema(description = "软删除时间")
    private LocalDateTime deletedAt;

    @TableField(exist = false)
    @Schema(description = "展示用姓名：优先打款信息真实姓名，否则小程序昵称")
    private String displayName;
}

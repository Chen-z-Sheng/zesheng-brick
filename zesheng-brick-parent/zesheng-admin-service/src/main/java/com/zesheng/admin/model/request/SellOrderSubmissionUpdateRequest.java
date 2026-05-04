package com.zesheng.admin.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 行情报单更新（管理端可修改除提交人ID外的全部字段，含商品明细）
 */
@Data
@Schema(description = "行情报单更新")
public class SellOrderSubmissionUpdateRequest {

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

    @Schema(description = "商品明细（可增删改）")
    private List<SellOrderItemDto> items;

    @Schema(description = "状态：0草稿 1已提交 2运输中 3入库中 4已打款 5异常 6已退货")
    private Integer status;

    @Schema(description = "回款金额")
    private BigDecimal settledAmount;

    @Schema(description = "管理员内部备注")
    private String adminInternalNote;

    @Data
    @Schema(description = "商品行")
    public static class SellOrderItemDto {
        @Schema(description = "商品名称")
        private String productName;
        @Schema(description = "单价")
        private BigDecimal price;
        @Schema(description = "数量")
        private Integer quantity;
    }
}

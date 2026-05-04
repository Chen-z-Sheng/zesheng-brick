package com.zesheng.admin.model.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 固结报单提交记录更新（管理端修改状态、备注、扩展数据等）
 */
@Data
@Schema(description = "固结报单提交记录更新")
public class FormSubmissionUpdateRequest {

    @Schema(description = "数量")
    private Integer quantity;

    @Schema(description = "结算金额")
    private BigDecimal settledAmount;

    @Schema(description = "状态：0草稿 1已提交 2运输中 3入库中 4已打款 5异常 6已退货")
    private Integer status;

    @Schema(description = "结算/回款时间", example = "2026-03-12 21:43:06")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime settledAt;

    @Schema(description = "回款凭证截图URL")
    private String settledProofUrl;

    @Schema(description = "管理员内部备注")
    private String adminInternalNote;

    @Schema(description = "扩展数据：加赠说明、快递单号、主单号、礼品单号、签收日期、备注等")
    private Map<String, Object> dataJson;
}

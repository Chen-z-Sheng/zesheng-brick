package com.zesheng.admin.model.request;

import com.zesheng.common.request.PageAndSortQueryRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 固结报单提交记录分页查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "固结报单提交记录分页查询")
public class FormSubmissionPageRequest extends PageAndSortQueryRequest {

    @Schema(description = "方案ID（精确）")
    private Long schemeId;

    @Schema(description = "用户ID（精确）")
    private Long userId;

    @Schema(description = "方案名称关键字，模糊匹配后按匹配到的方案ID过滤")
    private String schemeNameKeyword;

    @Schema(description = "用户关键字，按打款信息真实姓名模糊匹配后取用户ID过滤")
    private String userKeyword;

    @Schema(description = "状态：0草稿 1已提交 2运输中 7已签收 3入库中 4已打款 5异常 6已退货")
    private Integer status;
}

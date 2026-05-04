package com.zesheng.admin.model.request;

import com.zesheng.common.request.PageAndSortQueryRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 行情报单分页查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "行情报单分页查询")
public class SellOrderSubmissionPageRequest extends PageAndSortQueryRequest {

    @Schema(description = "用户ID（精确）")
    private Long userId;

    @Schema(description = "用户关键字，按打款信息真实姓名或用户昵称模糊匹配后取用户ID过滤")
    private String userKeyword;

    @Schema(description = "状态")
    private Integer status;
}

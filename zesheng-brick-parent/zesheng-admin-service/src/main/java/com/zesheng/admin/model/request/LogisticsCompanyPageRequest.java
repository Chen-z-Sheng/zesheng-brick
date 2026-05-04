package com.zesheng.admin.model.request;

import com.zesheng.common.request.PageQueryRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 物流公司分页查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "物流公司分页查询")
public class LogisticsCompanyPageRequest extends PageQueryRequest {

    @Schema(description = "公司名称关键词，模糊匹配")
    private String name;
}

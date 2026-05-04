package com.zesheng.admin.model.request;

import com.zesheng.common.request.PageAndSortQueryRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 公告分页查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "公告分页查询")
public class AnnouncementPageRequest extends PageAndSortQueryRequest {

    @Schema(description = "标题关键字（模糊匹配）")
    private String titleKeyword;
}

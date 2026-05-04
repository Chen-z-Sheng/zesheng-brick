package com.zesheng.admin.model.response;

import com.zesheng.common.response.PageResult;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 行情分页响应
 */
@Schema(description = "行情分页响应")
public class RecyclePricePageResponse extends PageResult<RecyclePriceVo> {
}

package com.zesheng.admin.model.request;

import com.zesheng.common.request.PageAndSortQueryRequest;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "UserPageRequest", description = "用户表分页查询参数")
public class UserPageRequest extends PageAndSortQueryRequest {

}
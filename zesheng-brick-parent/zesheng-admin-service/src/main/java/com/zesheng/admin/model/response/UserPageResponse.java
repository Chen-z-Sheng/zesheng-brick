package com.zesheng.admin.model.response;

import com.zesheng.common.response.PageResult;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "UserPageResponse", description = "用户表分页响应")
public class UserPageResponse extends PageResult<UserVo> {

}
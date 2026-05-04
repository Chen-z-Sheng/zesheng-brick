package com.zesheng.admin.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(name = "UserSaveResponse", description = "用户表新增响应")
public class UserSaveResponse extends UserVo {

    @Schema(description = "用户表主键ID", example = "6")
    private Long id;

    @Schema(name = "密码哈希")
    private String passwordHash;

}

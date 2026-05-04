package com.zesheng.admin.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(name = "UserUpdateResponse", description = "用户表更新响应")
public class UserUpdateResponse extends UserVo {

    @Schema(description = "用户表主键ID", example = "6")
    private Long id;

}
package com.zesheng.sys.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Role更新响应
 *
 * @author czk
 * @since Thu Feb 19
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(name = "RoleUpdateResponse", description = "系统端-角色表更新响应")
public class RoleUpdateResponse extends RoleVo {
    // ID已在Vo中声明，此处无需重复定义（若需显式可保留）
    @Schema(description = "系统端-角色表主键ID", example = "6")
    private Long id;
}
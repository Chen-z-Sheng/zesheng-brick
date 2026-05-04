package com.zesheng.sys.model.request;

import com.zesheng.common.request.PageAndSortQueryRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Role分页查询参数
 *
 * @author czk
 * @since Thu Feb 19
 */
@Data
@Schema(name = "RolePageRequest", description = "系统端-角色表分页查询参数")
public class RolePageRequest extends PageAndSortQueryRequest {
// 继承PageAndSortQueryDto：包含pageNum/pageSize/sortField/sortOrder等字段
}
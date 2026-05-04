package com.zesheng.sys.model.request;

import com.zesheng.common.request.PageAndSortQueryRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
* RolePermission分页查询参数
*
* @author czk
* @since Fri Feb 20
*/
@Data
@Schema(name = "RolePermissionPageRequest", description = "系统端-权限表分页查询参数")
public class RolePermissionPageRequest extends PageAndSortQueryRequest {
// 继承PageAndSortQueryRequest：包含pageNum/pageSize/sortField/sortOrder等字段
}
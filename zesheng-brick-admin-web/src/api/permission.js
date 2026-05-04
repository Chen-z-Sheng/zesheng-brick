// 角色、权限相关接口
import request from '@/utils/request'

/**
 * 获取用户权限列表
 * @param {Number} userId - 用户ID
 * 对应后端接口：GET /admin/sys/permission/user/{userId}
 */
export async function getUserPermissions(userId) {
  return request({
    url: `/admin/sys/permission/user/${userId}`,
    method: 'get'
  })
}

// ==================== 权限相关接口 ====================
/**
 * 获取所有权限列表（不分页）
 * 对应后端接口：GET /admin/sys/permission
 */
export async function getPermissionList() {
  return request({
    url: '/admin/sys/permission',
    method: 'get'
  })
}

/**
 * 获取权限详情
 * @param {Number} id - 权限ID
 * 对应后端接口：GET /admin/sys/permission/{id}
 */
export async function getPermissionDetail(id) {
  return request({
    url: `/admin/sys/permission/${id}`,
    method: 'get'
  })
}

/**
 * 新增权限
 * @param {Object} data - 权限新增参数
 * 对应后端接口：POST /admin/sys/permission
 */
export async function createPermission(data) {
  return request({
    url: '/admin/sys/permission',
    method: 'post',
    data
  })
}

/**
 * 修改权限
 * @param {Number} id - 权限ID
 * @param {Object} data - 权限修改参数
 * 对应后端接口：PATCH /admin/sys/permission/{id}
 */
export async function updatePermission(id, data) {
  return request({
    url: `/admin/sys/permission/${id}`,
    method: 'patch',
    data
  })
}

/**
 * 批量删除权限
 * @param {Array<Number>} ids - 权限ID列表
 * 对应后端接口：DELETE /admin/sys/permission/batch
 */
export async function deletePermissionBatch(ids) {
  return request({
    url: '/admin/sys/permission/batch',
    method: 'delete',
    data: ids // DELETE请求传递数组参数，需放在data中
  })
}

/**
 * 分页查询权限
 * @param {Object} params - 分页参数（pageNum/pageSize/sort等）
 * 对应后端接口：GET /admin/sys/permission/page
 */
export async function getPermissionPage(params) {
  return request({
    url: '/admin/sys/permission/page',
    method: 'get',
    params
  })
}

// ==================== 角色相关接口 ====================
/**
 * 获取所有角色列表（不分页）
 * 对应后端接口：GET /admin/sys/role
 */
export async function getRoleList() {
  return request({
    url: '/admin/sys/role',
    method: 'get'
  })
}

/** 别名，兼容 permissions.vue */
export const getRoleAll = getRoleList

/** 别名，兼容 permissions.vue */
export const getPermissionAll = getPermissionList

/**
 * 获取角色详情
 * @param {Number} id - 角色ID
 * 对应后端接口：GET /admin/sys/role/{id}
 */
export async function getRoleDetail(id) {
  return request({
    url: `/admin/sys/role/${id}`,
    method: 'get'
  })
}

/**
 * 新增角色
 * @param {Object} data - 角色新增参数
 * 对应后端接口：POST /admin/sys/role
 */
export async function createRole(data) {
  return request({
    url: '/admin/sys/role',
    method: 'post',
    data
  })
}

/**
 * 修改角色
 * @param {Number} id - 角色ID
 * @param {Object} data - 角色修改参数
 * 对应后端接口：PATCH /admin/sys/role/{id}
 */
export async function updateRole(id, data) {
  return request({
    url: `/admin/sys/role/${id}`,
    method: 'patch',
    data
  })
}

/**
 * 批量删除角色
 * @param {Array<Number>} ids - 角色ID列表
 * 对应后端接口：DELETE /admin/sys/role/batch
 */
export async function deleteRoleBatch(ids) {
  return request({
    url: '/admin/sys/role/batch',
    method: 'delete',
    data: ids
  })
}

/** 删除单个角色，兼容 permissions.vue */
export async function deleteRole(id) {
  return deleteRoleBatch(Array.isArray(id) ? id : [id])
}

/**
 * 分页查询角色
 * @param {Object} params - 分页参数（pageNum/pageSize/sort等）
 * 对应后端接口：GET /admin/sys/role/page
 */
export async function getRolePage(params) {
  return request({
    url: '/admin/sys/role/page',
    method: 'get',
    params
  })
}

// ==================== 角色权限关联接口（补充） ====================
/**
 * 新增角色权限关联
 * @param {Object} data - 角色权限关联参数
 * 对应后端接口：POST /admin/sys/role-permission
 */
export async function createRolePermission(data) {
  return request({
    url: '/admin/sys/role-permission',
    method: 'post',
    data
  })
}

/**
 * 单个删除角色权限关联
 * @param {Number} id - 角色权限关联ID
 * 对应后端接口：DELETE /admin/sys/role-permission/{id}
 */
export async function deleteRolePermission(id) {
  return request({
    url: `/admin/sys/role-permission/${id}`,
    method: 'delete'
  })
}

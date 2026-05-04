import request from "@/utils/request";

// 登录
export async function login(data) {
  return request({
    url: "/admin/auth/login",
    method: "post",
    data,
  });
}

// 获取用户信息
export function getUserInfo(userId) {
  return request({
    url: `/admin/user/${userId}`,
    method: "get",
  });
}

// 退出登录
export function logout() {
  return request({
    url: "/admin/auth/logout",
    method: "post",
  });
}

/**
 * 新增管理端用户（后端 POST /admin/user，仅超级管理员权限码 admin 可调用）
 */
export function createUser(data) {
  return request({
    url: "/admin/user",
    method: "post",
    data,
  });
}

// 刷新 token
export function refreshToken() {
  return request({
    url: "/admin/auth/refresh",
    method: "post",
  });
}

/**
 * 分页获取用户列表
 * 对应后端：GET /admin/user/page
 */
export function getAll(params) {
  const { page = 1, pageSize = 20, ...rest } = params || {}
  return request({
    url: "/admin/user/page",
    method: "get",
    params: { pageNum: page, pageSize, ...rest },
  })
}

/**
 * 修改用户信息
 * 对应后端：PATCH /admin/user/{id}
 */
export function update(id, data) {
  return request({
    url: `/admin/user/${id}`,
    method: "patch",
    data,
  })
}

/**
 * 删除用户（批量删除接口，传 id 数组）
 * 对应后端：POST /admin/user/delete
 */
export function deleteUser(id) {
  return request({
    url: "/admin/user/delete",
    method: "post",
    data: Array.isArray(id) ? id : [id],
  })
}

/**
 * 当前登录用户更新资料（手机号、头像）
 * 对应后端：PATCH /admin/user/self/profile
 */
export function updateSelfProfile(data) {
  return request({
    url: "/admin/user/self/profile",
    method: "patch",
    data,
  })
}

/**
 * 当前登录用户修改密码
 * 对应后端：POST /admin/user/self/password
 */
export function changeOwnPassword(data) {
  return request({
    url: "/admin/user/self/password",
    method: "post",
    data,
  })
}

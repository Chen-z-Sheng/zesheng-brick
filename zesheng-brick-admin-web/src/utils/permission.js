import store from '@/store'

/**
 * @description 权限校验（数组命中任一即可）
 * 兼容：user 模块是否 namespaced；从 getters 或 state 兜底取值
 * @param {string[]} value 需要的权限列表，如 ['admin','manager']
 * @returns {boolean}
 */
export default function checkPermission(value) {
  if (!Array.isArray(value) || value.length === 0) return false

  const permissions =
    store.getters?.['user/permissions'] ??
    store.getters?.permissions ??
    store.state?.user?.permissions ??
    []

  if (!Array.isArray(permissions) || permissions.length === 0) return false
  return permissions.some(role => value.includes(role))
}

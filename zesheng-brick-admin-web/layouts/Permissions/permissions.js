import store from '@/store'

function checkAndRemove(element, value) {
  if (!value || !Array.isArray(value) || value.length === 0) return
  const permissions = store.getters['user/permissions']
  if (!permissions || !Array.isArray(permissions)) return
  const hasPermission = permissions.some((role) => value.includes(role))
  if (!hasPermission && element.parentNode) {
    element.parentNode.removeChild(element)
  }
}

export default {
  mounted(element, binding) {
    checkAndRemove(element, binding.value)
  },
  inserted(element, binding) {
    checkAndRemove(element, binding.value)
  },
}

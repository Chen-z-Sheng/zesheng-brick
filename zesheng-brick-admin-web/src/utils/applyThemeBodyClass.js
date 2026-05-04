const THEME_STORAGE_KEY = "vue-admin-better-theme"

/**
 * 从 localStorage 读取主题名并设置到 document.body，与「主题配置」抽屉、系统设置页共用
 */
export function applyThemeBodyClassFromStorage() {
  try {
    const raw = localStorage.getItem(THEME_STORAGE_KEY)
    if (!raw) {
      document.body.className = "vue-admin-better-theme-default"
      return
    }
    const parsed = JSON.parse(raw)
    const name = parsed?.name && typeof parsed.name === "string" ? parsed.name : "default"
    document.body.className = `vue-admin-better-theme-${name}`
  } catch {
    document.body.className = "vue-admin-better-theme-default"
  }
}

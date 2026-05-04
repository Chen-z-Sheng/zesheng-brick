import { getUserInfo, login, logout } from "@/api/user"
import { getUserPermissions } from "@/api/permission"
import { getAccessToken, removeAccessToken, setAccessToken } from "@/utils/accessToken"
import {
  getRefreshToken as getStoredRefreshToken,
  removeRefreshToken,
  setRefreshToken as persistRefreshToken,
} from "@/utils/refreshToken"
import { resetRouter } from "@/router"
import { title } from "@/config"
import { ElMessage } from "element-plus"
import { jwtDecode } from "jwt-decode"

function deriveUserIdFromToken(token) {
  try {
    if (!token) return ""
    const p = jwtDecode(token)
    return String(p.userId ?? p.sub ?? "")
  } catch {
    return ""
  }
}

const state = () => {
  const token = getAccessToken()
  return {
    accessToken: token,
    refreshToken: getStoredRefreshToken() || "",
    tokenType: "Bearer",
    userId: deriveUserIdFromToken(token),
    username: "",
    avatar: "",
    permissions: [],
    userLoaded: false,
  }
}

const getters = {
  accessToken: s => s.accessToken,
  refreshToken: s => s.refreshToken,
  tokenType: s => s.tokenType,
  userId: s => s.userId,
  username: s => s.username,
  avatar: s => s.avatar,
  permissions: s => s.permissions,
  userLoaded: s => s.userLoaded,
}

const mutations = {
  setAccessToken(s, t) { s.accessToken = t; setAccessToken(t) },
  setRefreshToken(s, t) {
    const v = t || ""
    s.refreshToken = v
    if (v) persistRefreshToken(v)
    else removeRefreshToken()
  },
  setTokenType(s, t) { s.tokenType = t || "Bearer" },
  setUserId(s, id) { s.userId = id },
  setUsername(s, v) { s.username = v },
  setAvatar(s, v) { s.avatar = v },
  setPermissions(s, v) { s.permissions = v || [] },
  SET_USER_LOADED(s, v) { s.userLoaded = !!v },
}

const actions = {
  setPermissions({ commit }, v) { commit("setPermissions", v) },

  async login({ commit }, userInfo) {
    const { data } = await login(userInfo)
    const { accessToken, refreshToken, tokenType } = data || {}
    if (!accessToken) {
      ElMessage.error("登录接口异常：未返回 accessToken")
      return
    }
    commit("setAccessToken", accessToken)
    commit("setRefreshToken", refreshToken || "")
    commit("setTokenType", tokenType || "Bearer")
    const id = deriveUserIdFromToken(accessToken)
    if (!id) {
      ElMessage.error("登录令牌解析失败")
      return
    }
    commit("setUserId", id)

    const hour = new Date().getHours()
    const thisTime = hour < 8 ? "早上好" : hour <= 11 ? "上午好" : hour <= 13 ? "中午好" : hour < 18 ? "下午好" : "晚上好"
    ElMessage.success(`欢迎登录${title}，${thisTime}！`)
  },

  async getUserInfo({ commit, state }) {
    try {
      // 1. 确保有 userId，没有就从 token 里解出来
      if (!state.userId) {
        const id = deriveUserIdFromToken(state.accessToken)
        if (id) {
          commit("setUserId", id)
        } else {
          console.warn("[user/getUserInfo] userId 缺失，跳过请求")
          return null // 用 null 表示失败，避免跟 [] 搞混
        }
      }

      // 2. 调用接口拿用户信息
      const { data: userInfo } = await getUserInfo(state.userId)
      console.log("[DEBUG] getUserInfo raw data =", userInfo)

      if (!userInfo) {
        ElMessage.error("验证失败，请重新登录")
        return null
      }

      // 3. 调用权限接口获取用户权限列表
      const { data: permissionsData } = await getUserPermissions(state.userId)
      console.log("[DEBUG] getUserPermissions raw data =", permissionsData)

      // 4. 解析权限，提取 code 字段（严格按后端返回，不再前端塞 admin）
      let permissions = []
      if (Array.isArray(permissionsData) && permissionsData.length > 0) {
        permissions = permissionsData.map(item => item.code)
      }

      // 5. 兜底：无任何权限时给 'user'，避免完全无法使用
      if (!permissions.length) {
        permissions = ["user"]
      }

      const username = userInfo.username || "用户"
      const avatar = userInfo.avatarUrl || userInfo.avatar || ""

      console.log("[DEBUG] computed permissions =", permissions)

      commit("setPermissions", permissions)
      commit("setUsername", username)
      commit("setAvatar", avatar)
      commit("SET_USER_LOADED", true)

      // 非常重要：返回的是权限数组，路由守卫要用
      return permissions
    } catch (error) {
      console.error("获取用户信息失败:", error)
      ElMessage.error("获取用户信息失败，请重新登录")
      return null
    }
  }
  ,

  async logout({ dispatch }) {
    try { await logout() } catch { }
    await dispatch("resetAccessToken")
    await resetRouter()
    location.reload()
  },

  resetAccessToken({ commit }) {
    commit("setPermissions", [])
    commit("setUserId", "")
    commit("setAccessToken", "")
    commit("setRefreshToken", "")
    commit("setTokenType", "Bearer")
    commit("SET_USER_LOADED", false)
    removeAccessToken()
  },
}

export default { state, getters, mutations, actions }

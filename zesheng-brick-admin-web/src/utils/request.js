import axios from "axios"
import {
  baseURL,
  contentType,
  debounce,
  invalidCode,
  loginInterception,
  noPermissionCode,
  requestTimeout,
  successCode,
} from "@/config"
import store from "@/store"
import qs from "qs"
import router from "@/router"
import { isArray } from "@/utils/validate"
import { ElLoading, ElMessage } from "element-plus"
import { pickBy } from "lodash-es"

// 生产环境 mock（保持你的逻辑）
if (process.env.NODE_ENV === "production") {
  const mockContext = require.context("../../mock/controller", true, /\.js$/)
  mockContext.keys().forEach((key) => {
    const m = mockContext(key)
    if (m.default) m.default
  })
}

let loadingInstance
let isRefreshing = false
let pendingQueue = [] // 刷新期间挂起的请求

function isRefreshRequest(config) {
  return config?.url?.includes("/admin/auth/refresh")
}

const instance = axios.create({
  baseURL,
  timeout: requestTimeout,
  headers: { "Content-Type": contentType },
})

instance.defaults.retry = 3
instance.defaults.retryDelay = 1000

const handleCode = (code, msg) => {
  // 兼容 HTTP 401和业务invalidCode
  if (code === 401 || code === invalidCode) {
    ElMessage.error(msg || "未授权或登录已过期")
    store.dispatch("user/resetAccessToken")
    if (loginInterception) {
      // 跳转到登录页并带上当前路由作为redirect参数
      const currentPath = encodeURIComponent(window.location.pathname + window.location.search)
      router.replace({ path: `/login?redirect=${currentPath}` }).catch(() => {})
    }
    return
  }
  switch (code) {
    case noPermissionCode:
      router.push({ path: "/401" }).catch(() => { })
      break
    default:
      ElMessage.error(msg || `后端接口${code}异常`)
      break
  }
}

instance.interceptors.request.use(
  (config) => {
    // 对于OPTIONS预检请求，不添加Authorization头
    if (config.method !== 'OPTIONS') {
      // 刷新接口只校验 body 中的 refreshToken，勿带过期 access，避免个别环境下 /auth/** 未放行时误拦
      const isRefresh = config?.url?.includes("/admin/auth/refresh")
      if (!isRefresh) {
        const accessToken = store.getters?.accessToken || store.state?.user?.accessToken
        const tokenType = store.getters?.tokenType || store.state?.user?.tokenType || "Bearer"
        if (accessToken) {
          config.headers = config.headers || {}
          config.headers.Authorization = `${tokenType} ${accessToken}` // Bearer <token>
        }
      } else if (config.headers) {
        delete config.headers.Authorization
      }
    }

    // FormData 不处理，避免破坏 multipart 请求
    if (config.data && !(config.data instanceof FormData)) {
      config.data = pickBy(config.data, (v) => v !== undefined && v !== null)
    }

    if (
      config.data &&
      !(config.data instanceof FormData) &&
      config.headers["Content-Type"] ===
      "application/x-www-form-urlencoded;charset=UTF-8"
    ) {
      config.data = qs.stringify(config.data)
    }

    // multipart 上传时由浏览器自动设置 Content-Type（含 boundary），不覆盖
    if (config.data instanceof FormData && config.headers) {
      delete config.headers["Content-Type"]
    }

    if (debounce.some((item) => config.url?.includes(item))) {
      loadingInstance = ElLoading.service()
    }

    return config
  },
  (error) => Promise.reject(error)
)

function maybeRetry(error, axiosInstance) {
  const cfg = error.config || {}
  const status = error?.response?.status
  if (status && status < 500) return Promise.reject(error) // 4xx 不重试

  cfg.retry = cfg.retry ?? instance.defaults.retry
  cfg.retryDelay = cfg.retryDelay ?? instance.defaults.retryDelay
  cfg.__retryCount = cfg.__retryCount || 0
  if (cfg.__retryCount >= cfg.retry) return Promise.reject(error)

  cfg.__retryCount += 1
  return new Promise((resolve) => {
    setTimeout(() => {
      console.log(`重试请求: ${cfg.url}, 尝试次数: ${cfg.__retryCount}`)
      resolve()
    }, cfg.retryDelay)
  }).then(() => axiosInstance(cfg))
}

// 挂起请求，等刷新完成后重放
function queueRequest(config) {
  return new Promise((resolve, reject) => {
    pendingQueue.push({ resolve, reject, config })
  })
}
function flushQueue(error, newAccessToken, tokenType = "Bearer") {
  pendingQueue.forEach(({ resolve, reject, config }) => {
    if (error) {
      reject(error)
    } else {
      const replay = { ...config, headers: { ...(config.headers || {}) } }
      replay.headers.Authorization = `${tokenType} ${newAccessToken}`
      resolve(instance(replay))
    }
  })
  pendingQueue = []
}

// 刷新 accessToken
async function doRefreshToken() {
  const refreshToken = store.getters?.refreshToken || store.state?.user?.refreshToken
  if (!refreshToken) throw new Error("缺少 refreshToken")

  // 后端接口：POST /admin/auth/refresh，从请求体中获取refreshToken
  const resp = await instance.post("/admin/auth/refresh", {
    refreshToken: refreshToken
  }, {
    skipAuthRefresh: true
  })
  const { code, data, message, msg } = resp || {}
  // 200和0都是成功状态码
  const successCodes = [200, 0]
  if (!successCodes.includes(code) || !data?.accessToken) throw new Error(message || msg || "刷新失败")

  const { accessToken, refreshToken: newRT, tokenType } = data
  store.commit("user/setAccessToken", accessToken)
  if (newRT) store.commit("user/setRefreshToken", newRT)
  store.commit("user/setTokenType", tokenType || "Bearer")
  return { accessToken, tokenType: tokenType || "Bearer" }
}

instance.interceptors.response.use(
  async (response) => {
    if (loadingInstance) loadingInstance.close()
    const { data, config } = response
    if (data == null) {
      ElMessage.error("后端接口返回数据为空")
      return Promise.reject("后端接口返回数据为空")
    }

    const code = data.code ?? null
    const msg = data.message ?? data.msg ?? "未知错误"
    const okArr = isArray(successCode) ? [...successCode] : [successCode]

    if (code !== null && okArr.includes(code)) {
      return data
    } else {
      if (config?.skipAuthRefresh || isRefreshRequest(config)) {
        return Promise.reject(new Error(msg || "刷新令牌已过期"))
      }

      // 业务token过期，尝试刷新token
      if (code === 401 || code === invalidCode) {
        const rt = store.getters?.refreshToken || store.state?.user?.refreshToken
        if (!rt) {
          handleCode(code, msg)
          return Promise.reject(
            `请求异常拦截:${JSON.stringify({ url: config?.url, code, msg })}`
          )
        }

        if (isRefreshing) {
          return queueRequest(config)
        }

        isRefreshing = true
        try {
          const { accessToken, tokenType } = await doRefreshToken()
          const replay = { ...config, headers: { ...(config.headers || {}) } }
          replay.headers.Authorization = `${tokenType} ${accessToken}`

          flushQueue(null, accessToken, tokenType)
          return instance(replay)
        } catch (e) {
          flushQueue(e)
          handleCode(code, "登录已过期，请重新登录")
          return Promise.reject(e)
        } finally {
          isRefreshing = false
        }
      }

      handleCode(code, msg)
      return Promise.reject(
        `请求异常拦截:${JSON.stringify({ url: config?.url, code, msg })}`
      )
    }
  },
  async (error) => {
    if (loadingInstance) loadingInstance.close()

    const { response, config } = error

    if (response?.status === 204) {
      return { code: 0, data: null, message: 'No Content' };
    }

    // 无响应（网络/超时）
    if (!response) return maybeRetry(error, instance)

    const status = response.status
    const serverMsg =
      response.data?.message ?? response.data?.msg ?? error.message ?? "未知错误"

    if (config?.skipAuthRefresh || isRefreshRequest(config)) {
      return Promise.reject(error)
    }

    // === 401：刷新一次 ===
    if (status === 401) {
      const rt = store.getters?.refreshToken || store.state?.user?.refreshToken
      if (!rt) {
        handleCode(401, serverMsg)
        return Promise.reject(error)
      }

      if (isRefreshing) {
        return queueRequest(config)
      }

      isRefreshing = true
      try {
        const { accessToken, tokenType } = await doRefreshToken()
        const replay = { ...config, headers: { ...(config.headers || {}) } }
        replay.headers.Authorization = `${tokenType} ${accessToken}`

        flushQueue(null, accessToken, tokenType)
        return instance(replay)
      } catch (e) {
        flushQueue(e)
        handleCode(401, "登录已过期，请重新登录")
        return Promise.reject(e)
      } finally {
        isRefreshing = false
      }
    }

    // 其它 HTTP 错误
    handleCode(status, serverMsg)
    if (status >= 500 && status < 600) {
      return maybeRetry(error, instance)
    }
    return Promise.reject(error)
  }
)

export default instance

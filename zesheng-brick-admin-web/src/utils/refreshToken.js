import { storage, tokenTableName } from "@/config"

/** refreshToken 与 accessToken 分 key 存储，避免串用 */
function key() {
  return `${tokenTableName}-refresh`
}

export function getRefreshToken() {
  const k = key()
  if (storage === "sessionStorage") {
    return sessionStorage.getItem(k) || ""
  }
  return localStorage.getItem(k) || ""
}

export function setRefreshToken(refreshToken) {
  const k = key()
  const v = refreshToken || ""
  if (!v) {
    removeRefreshToken()
    return
  }
  if (storage === "sessionStorage") {
    sessionStorage.setItem(k, v)
  } else {
    localStorage.setItem(k, v)
  }
}

export function removeRefreshToken() {
  const k = key()
  if (storage === "sessionStorage") {
    sessionStorage.removeItem(k)
  } else {
    localStorage.removeItem(k)
  }
}

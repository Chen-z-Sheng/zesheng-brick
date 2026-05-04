import { tokenTableName } from "@/config"

const STORAGE_KEY = `${tokenTableName}-remember-login`

/**
 * 读取上次登录在本机保存的状态：账号每次登录成功都会更新；密码仅在上次勾选「记住密码」并登录后存在
 */
export function loadLastLoginState() {
    try {
        const raw = localStorage.getItem(STORAGE_KEY)
        if (!raw) {
            return { username: "", password: "", rememberPassword: false }
        }
        const parsed = JSON.parse(raw)
        if (!parsed || typeof parsed.username !== "string") {
            return { username: "", password: "", rememberPassword: false }
        }
        const password =
            typeof parsed.password === "string" ? parsed.password : ""
        return {
            username: parsed.username,
            password,
            rememberPassword: password.length > 0,
        }
    } catch {
        return { username: "", password: "", rememberPassword: false }
    }
}

/**
 * 登录成功后写入：始终保存本次账号；仅 rememberPassword 为 true 时写入密码（明文 localStorage，公共电脑勿勾选）
 */
export function saveLastLoginState(username, rememberPassword, plainPassword) {
    const payload = { username }
    if (rememberPassword && plainPassword) {
        payload.password = plainPassword
    }
    localStorage.setItem(STORAGE_KEY, JSON.stringify(payload))
}

/**
 * 取消勾选「记住密码」时调用：删掉已存密码，账号保留
 */
export function clearStoredPasswordOnly() {
    try {
        const raw = localStorage.getItem(STORAGE_KEY)
        if (!raw) {
            return
        }
        const parsed = JSON.parse(raw)
        const username =
            parsed && typeof parsed.username === "string" ? parsed.username : ""
        localStorage.setItem(STORAGE_KEY, JSON.stringify({ username }))
    } catch {
        localStorage.removeItem(STORAGE_KEY)
    }
}

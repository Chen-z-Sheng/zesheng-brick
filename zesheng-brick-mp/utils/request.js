const { BASE_URL, TIMEOUT = 15000 } = require('../config/index');
const { getToken, setToken, getRefreshToken, setRefreshToken, clearAllAuth } = require('./storage');
const { setGlobalUserInfo } = require('../services/auth-state');

/**
 * 统一请求封装：自动拼接 BASE_URL、携带 token、处理错误码
 * 支持：请求前若 access token 即将过期则主动刷新；收到 401 时被动刷新并重试
 */
let isRefreshing = false;
let refreshSubscribers = [];
const UNAUTHORIZED_CODES = [401, 10004, 10005];
const UNAUTHORIZED_KEYWORDS = ['未登录', 'token过期', 'token已过期', 'token无效', '登录已过期', 'unauthorized'];

/** 解析 JWT payload 的 exp（秒），若距过期不足此毫秒数则视为即将过期 */
const EXPIRE_BUFFER_MS = 5 * 60 * 1000;

function forceLogout() {
    clearAllAuth();
    setGlobalUserInfo(null);
}

function isTokenExpiringSoon(token) {
    if (!token || typeof token !== 'string') return false;
    try {
        const parts = token.split('.');
        if (parts.length !== 3) return false;
        const base64 = parts[1].replace(/-/g, '+').replace(/_/g, '/');
        const pad = base64.length % 4;
        const padded = pad ? base64 + '===='.slice(0, 4 - pad) : base64;
        const json = decodeBase64Payload(padded);
        if (!json) return false;
        const payload = JSON.parse(json);
        const exp = payload.exp;
        if (typeof exp !== 'number') return false;
        return exp * 1000 - Date.now() < EXPIRE_BUFFER_MS;
    } catch (e) {
        return false;
    }
}

function decodeBase64Payload(base64) {
    if (!base64) return '';
    try {
        if (typeof wx !== 'undefined' && typeof wx.base64ToArrayBuffer === 'function') {
            const buffer = wx.base64ToArrayBuffer(base64);
            const bytes = new Uint8Array(buffer);
            let percentEncoded = '';
            for (let i = 0; i < bytes.length; i += 1) {
                percentEncoded += `%${(`00${bytes[i].toString(16)}`).slice(-2)}`;
            }
            return decodeURIComponent(percentEncoded);
        }
    } catch (e) {
        // ignore
    }

    try {
        if (typeof atob !== 'undefined') {
            return atob(base64);
        }
    } catch (e) {
        // ignore
    }

    try {
        if (typeof Buffer !== 'undefined') {
            return Buffer.from(base64, 'base64').toString('utf8');
        }
    } catch (e) {
        // ignore
    }
    return '';
}

function isUnauthorizedMessage(respData) {
    const message = `${respData?.msg || respData?.message || ''}`.toLowerCase();
    if (!message) return false;
    return UNAUTHORIZED_KEYWORDS.some((keyword) => message.indexOf(keyword.toLowerCase()) >= 0);
}

function shouldHandleUnauthorized(statusCode, respData) {
    if (statusCode === 401) return true;
    if (statusCode === 403 && isUnauthorizedMessage(respData)) return true;
    if (!respData || typeof respData.code === 'undefined') {
        return false;
    }
    const code = Number(respData.code);
    if (UNAUTHORIZED_CODES.indexOf(code) >= 0) {
        return true;
    }
    return isUnauthorizedMessage(respData);
}

function notifyRefreshSuccess(newToken) {
    const subscribers = refreshSubscribers.slice();
    refreshSubscribers = [];
    subscribers.forEach(function (subscriber) {
        subscriber.resolve(newToken);
    });
}

function notifyRefreshFailure(error) {
    const subscribers = refreshSubscribers.slice();
    refreshSubscribers = [];
    subscribers.forEach(function (subscriber) {
        subscriber.reject(error);
    });
}

/**
 * 保证当前用于请求的 token 有效：即将过期或仅有 refresh 时先刷新再返回 access token
 */
function ensureValidToken() {
    const token = getToken();
    const refresh = getRefreshToken();
    // 本地 access 丢失但 refresh 仍在时，必须用 refresh 换新 access，否则会误判未登录
    if (!token && refresh) {
        return refreshToken()
            .then((newToken) => newToken || getToken())
            .catch(() => '');
    }
    if (!token) return Promise.resolve('');
    if (!refresh) return Promise.resolve(token);
    if (!isTokenExpiringSoon(token)) return Promise.resolve(token);
    return refreshToken()
        .then((newToken) => newToken || getToken())
        .catch(() => getToken());
}

// 刷新token
const refreshToken = async () => {
  const refreshToken = getRefreshToken();
  if (!refreshToken) {
    return Promise.reject(new Error('No refresh token'));
  }

  return new Promise((resolve, reject) => {
    wx.request({
      url: `${BASE_URL}/auth/refresh-token`,
      method: 'POST',
      data: { refreshToken },
      timeout: TIMEOUT,
      header: {
        'Content-Type': 'application/json',
      },
      success: (res) => {
        const { statusCode, data: respData } = res;
        if (statusCode === 200 && respData.code === 200 && respData.data) {
          const { token, refreshToken: newRefreshToken } = respData.data;
          setToken(token);
          setRefreshToken(newRefreshToken);
          resolve(token);
        } else {
          forceLogout();
          reject(new Error('Refresh token failed'));
        }
      },
      fail: (err) => {
        reject(err);
      },
    });
  });
};

// 重新发送请求
const resendRequest = (config, token) => {
  const suppressBusinessToast = config.suppressBusinessToast === true;
  return new Promise((resolve, reject) => {
    wx.request({
      url: `${BASE_URL}${config.url}`,
      method: config.method,
      data: config.data,
      timeout: TIMEOUT,
      header: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...config.header,
      },
      success: (res) => {
        const { statusCode, data: respData } = res;

        if (statusCode >= 200 && statusCode < 300) {
          // 后端返回结构：{ code: 200, msg, data }，成功码为 200
          if (respData && typeof respData.code !== 'undefined' && respData.code !== 200) {
            if (!suppressBusinessToast) {
              wx.showToast({ title: respData.msg || respData.message || '请求失败', icon: 'none' });
            }
            return reject(respData);
          }
          const payload = (respData && typeof respData.data !== 'undefined')
            ? respData.data
            : respData;
          return resolve(payload);
        }

        wx.showToast({ title: respData?.msg || respData?.message || '请求失败', icon: 'none' });
        return reject(respData);
      },
      fail: (err) => {
        wx.showToast({ title: '网络错误，请重试', icon: 'none' });
        reject(err);
      },
    });
  });
};

const request = function (config) {
  var url = config.url;
  var method = config.method || 'GET';
  var data = config.data || {};
  var header = config.header || {};
  var showLoading = config.showLoading || false;
  var suppressBusinessToast = config.suppressBusinessToast === true;

  return ensureValidToken().then(function (token) {
    return new Promise(function (resolve, reject) {
      function handleUnauthorized(reqConfig) {
        if (reqConfig.url === '/auth/refresh-token') {
          forceLogout();
          reject(new Error('Refresh token expired'));
          return;
        }
        var hasRefreshToken = !!getRefreshToken();

        if (!hasRefreshToken) {
          reject(new Error('Unauthorized'));
          return;
        }

        if (!isRefreshing) {
          isRefreshing = true;
          refreshToken()
            .then(function (newToken) {
              isRefreshing = false;
              notifyRefreshSuccess(newToken);
              resendRequest(reqConfig, newToken).then(resolve).catch(reject);
            })
            .catch(function (err) {
              isRefreshing = false;
              forceLogout();
              notifyRefreshFailure(err);
              wx.showToast({ title: '登录已过期，请重新登录', icon: 'none' });
              reject(err);
            });
        } else {
          refreshSubscribers.push({
            resolve: function (newToken) {
              resendRequest(reqConfig, newToken).then(resolve).catch(reject);
            },
            reject: function (error) {
              reject(error);
            }
          });
        }
      }

      if (showLoading) {
        wx.showNavigationBarLoading();
      }

      wx.request({
        url: BASE_URL + url,
        method: method,
        data: data,
        timeout: TIMEOUT,
        header: Object.assign(
          { 'Content-Type': 'application/json' },
          token ? { Authorization: 'Bearer ' + token } : {},
          header
        ),
        success: function (res) {
          var statusCode = res.statusCode;
          var respData = res.data;
          var reqConfig = {
            url: url,
            method: method,
            data: data,
            header: header,
            showLoading: showLoading,
            suppressBusinessToast: suppressBusinessToast,
          };

          if (shouldHandleUnauthorized(statusCode, respData)) {
            handleUnauthorized(reqConfig);
            return;
          }

          if (statusCode >= 200 && statusCode < 300) {
            if (respData && typeof respData.code !== 'undefined' && respData.code !== 200) {
              if (!suppressBusinessToast) {
                wx.showToast({ title: respData.msg || respData.message || '请求失败', icon: 'none' });
              }
              reject(respData);
              return;
            }
            var payload = (respData && typeof respData.data !== 'undefined') ? respData.data : respData;
            resolve(payload);
            return;
          }

          wx.showToast({ title: (respData && (respData.msg || respData.message)) || '请求失败', icon: 'none' });
          reject(respData);
        },
        fail: function (err) {
          wx.showToast({ title: '网络错误，请重试', icon: 'none' });
          reject(err);
        },
        complete: function () {
          if (showLoading) {
            wx.hideNavigationBarLoading();
          }
          if (wx.stopPullDownRefresh) {
            wx.stopPullDownRefresh();
          }
        }
      });
    });
  });
};

const get = (url, params = {}, options = {}) => request({
  url,
  method: 'GET',
  data: params,
  ...options,
});

const post = (url, body = {}, options = {}) => request({
  url,
  method: 'POST',
  data: body,
  ...options,
});

const patch = (url, body = {}, options = {}) => request({
  url,
  method: 'PATCH',
  data: body,
  ...options,
});

module.exports = {
  request,
  get,
  post,
  patch,
  ensureValidToken,
};

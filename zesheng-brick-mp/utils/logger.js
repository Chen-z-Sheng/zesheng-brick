/* eslint-disable no-console */
/**
 * 日志工具：开发环境输出，生产环境静默
 * 替代直接使用 console，避免生产环境泄露调试信息
 */
const isDev = typeof __wxConfig !== 'undefined' && __wxConfig.envVersion !== 'release';

const logger = {
  log(...args) {
    if (isDev) {
      console.log('[LOG]', ...args);
    }
  },
  error(...args) {
    if (isDev) {
      console.error('[ERROR]', ...args);
    }
  },
  warn(...args) {
    if (isDev) {
      console.warn('[WARN]', ...args);
    }
  },

  /**
   * 统一错误提示
   * @param {string} msg - 错误消息
   * @param {'toast'|'modal'|'none'} type - 提示方式
   */
  showError(msg, type = 'toast') {
    if (type === 'none') return;
    if (type === 'modal') {
      wx.showModal({
        title: '提示',
        content: msg,
        showCancel: false,
      });
      return;
    }
    wx.showToast({
      title: msg,
      icon: 'none',
      duration: 2000,
    });
  },
};

module.exports = logger;

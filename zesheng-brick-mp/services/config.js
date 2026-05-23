const request = require('../utils/request');

/**
 * 获取首页轮播图配置
 * @returns {Promise<Object>} 配置数据
 */
const fetchIndexBanner = () => {
  return request.get('/api/client/config');
};

/**
 * 获取首页轮播图列表（从 OSS index-banner 目录读取）
 * @returns {Promise<string[]>} 轮播图 URL 数组
 */
const getBannerList = () => {
  return request.get('/banner/list').then((res) => {
    return Array.isArray(res) ? res : [];
  });
};

/** 便宜寄快递：跳转目标小程序（sys_config，JSON 含 appId/AppId 与 path） */
const CHEAP_EXPRESS_MINI_PROGRAM_KEY = 'cheap_express_mini_program';

/** 首页展示的客服微信号（sys_config 明文） */
const ADMIN_WECHAT_ACCOUNT_KEY = 'admin_wechat_account';

/** 首页商务合作 / 同款小程序联系方式（sys_config 明文） */
const BUSINESS_WECHAT_ACCOUNT_KEY = 'business_wechat_account';

/**
 * 获取公开系统配置（匿名可读，后端白名单校验 key）
 * @param {string} configKey
 * @returns {Promise<string>}
 */
const fetchPublicSysConfigByKey = (configKey) => {
  const encoded = encodeURIComponent(configKey);
  // 未配置时不弹全局 Toast，由页面自行展示占位或提示
  return request.get(`/public/sys-config/by-key/${encoded}`, {}, { suppressBusinessToast: true });
};

module.exports = {
  fetchIndexBanner,
  getBannerList,
  CHEAP_EXPRESS_MINI_PROGRAM_KEY,
  ADMIN_WECHAT_ACCOUNT_KEY,
  BUSINESS_WECHAT_ACCOUNT_KEY,
  fetchPublicSysConfigByKey,
};

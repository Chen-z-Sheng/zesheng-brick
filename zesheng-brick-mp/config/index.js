// 环境与基础配置
const envVersion = wx.getAccountInfoSync ?
  wx.getAccountInfoSync().miniProgram.envVersion :
  'develop'; // develop | trial | release

const ENV_MAP = {
  develop: {
    BASE_URL: 'http://192.168.31.35:9099/api/client', // TODO: 替换为本地/测试域名
    TIMEOUT: 15000,
  },
  trial: {
    BASE_URL: 'http://192.168.31.35:9099/api/client', // TODO: 替换为预发域名
    TIMEOUT: 15000,
  },
  release: {
    BASE_URL: 'http://192.168.31.35:9099/api/client', // TODO: 替换为生产域名
    TIMEOUT: 15000,
  },
};

function getExtConfig() {
  try {
    if (typeof wx.getExtConfigSync === 'function') {
      return wx.getExtConfigSync() || {};
    }
  } catch (error) {
    return {};
  }
  return {};
}

const currentEnv = ENV_MAP[envVersion] || ENV_MAP.develop;
const extConfig = getExtConfig();
const baseUrl = extConfig.BASE_URL || currentEnv.BASE_URL;
const timeout = Number(extConfig.TIMEOUT) || currentEnv.TIMEOUT;

module.exports = {
  BASE_URL: baseUrl,
  TIMEOUT: timeout,
  ENV: envVersion,
  // 客服配置
  CUSTOMER_SERVICE: {
    QR_CODE_URL: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=WeChat%20QR%20code%20for%20customer%20service%2C%20professional%20style%2C%20clear%20image&image_size=square_hd',
    CONTACT_INFO: 'czk666888fff',
    TIPS: '请长按图片添加管理员好友或搜索添加管理员好友'
  }
};
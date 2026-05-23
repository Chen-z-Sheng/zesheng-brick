/**
 * @description 导出默认网路配置（VUE_APP_API_BASE_URL 由 rspack DefinePlugin 在构建时注入）
 **/
const network = {
  baseURL: process.env.VUE_APP_API_BASE_URL || "http://127.0.0.1:9068/api",
  contentType: "application/json;charset=UTF-8",
  messageDuration: 3000,
  requestTimeout: 15000,
  successCode: [200, 0],
  invalidCode: 10005,
  noPermissionCode: 401,
};
module.exports = network;

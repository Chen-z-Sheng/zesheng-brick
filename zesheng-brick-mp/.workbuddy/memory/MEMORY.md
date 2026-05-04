# MEMORY.md - 项目长期记忆

## 泽晟搬砖助手小程序

### 关键信息
- 联系方式: `czk666888fff`（注意是 czk 不是 czs）
- 使用 myPlugin 插件 (v1.4.14, provider: wx8c631f7e9f2465e1) 做智能客服
- 客服插件 appid: `shVdhiau7vzPNreYOhccE63qaDhxLM`

### 代码规范
- 所有 API 请求必须走 `utils/request.js` 封装（不要直接用 wx.request）
- 所有 token 读取必须用 `utils/storage.js` 的 `getToken()`（不要 wx.getStorageSync('token')）
- 图片选择使用 `wx.chooseMedia`（不要用已废弃的 wx.chooseImage）

### 已知技术债务
- oss.ts 为 TypeScript 死代码，项目未配置 TS 编译
- 存在未注册残留页面：openaiPluginPage、originalPluginPage、rewritePluginPage

### 2026-04-30 修复记录
- 全面优化了客服模块的图片消息显示和布局问题
- 统一了联系方式和请求封装方式

### 2026-05-XX UI全面重做记录
已完成以下页面的UI深度重做，严格遵循微信官方设计规范（主色 #07c160、背景 #f7f7f7、24rpx圆角、统一间距字体）：

**本次完成页面：**
- profile（个人信息）- wxss + wxml
- payment-info（打款信息）- wxss + wxml
- help-center（帮助中心）- wxss + wxml
- feedback（问题反馈）- wxss + wxml
- feedback-history（反馈历史）- wxss + wxml
- about（关于页面）- wxss + wxml（已填充完整内容，不再是空壳）
- sms-login（验证码登录）- wxss + wxml
- detail（商品详情）- wxss + wxml（新增底部操作栏）
- announcement-history（公告历史）- index.wxss + index.wxml

**之前已优化页面（上次会话）：**
- market（修复右侧边框截断问题）
- index、form、my、order-list、order-detail
- form-schemes、sell-order、login
- customer-service（客服页面样式）

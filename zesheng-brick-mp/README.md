# 泽晟搬砖助手 · 微信小程序（zesheng-brick-mp）

微信原生小程序，对接 **`zesheng-client-service`**（上下文路径一般为 `/api/client`，具体以服务端配置为准）。

## 技术栈

- 小程序基础库（原生 WXML/WXSS/JS）
- 统一请求：`utils/request.js`
- 业务接口：`services/`
- 环境配置：`config/index.js`（或项目内等价配置文件）

## 目录说明

```
├── app.json           # 页面路由与 tabBar（仅注册业务页）
├── config/            # 环境变量（如 BASE_URL）
├── utils/             # 请求、存储、日志等
├── services/          # 按业务拆分的接口封装
├── pages/             # 页面（已在 app.json 注册的为正式页面）
├── components/        # 组件
└── images/            # 静态资源
```

## 开发步骤

1. 使用 **微信开发者工具** 打开本目录。  
2. 在 `config` 中将接口 **BASE_URL** 指向已启动的 **client-service**（含协议、域名、端口及 `/api/client` 等前缀）。  
3. 本地需合法配置小程序 AppID；涉及登录、手机号等能力时，在微信公众平台配置服务器域名与白名单。  

## 规范约定

- 变量与函数：**camelCase**  
- 避免直接使用 `console`，优先使用项目内的 **`utils/logger`**（若已提供）  
- **页面必须在 `app.json` 的 `pages` 中注册**；未注册的目录不应保留脚手架残留（已清理微信开发者工具生成的空插件示例目录）

## 与后端的关系

| 能力 | 后端模块 |
|------|-----------|
| 登录、用户、订单、表单、行情等 | `zesheng-client-service` |
| 后台运营、权限、行情维护 | `zesheng-admin-service`（本小程序不直连，除非有特殊 H5/内嵌场景） |

## 仓库与 Monorepo

与后端、管理端前端同属 **`zesheng-brick`** 仓库；本目录名为 **`zesheng-brick-mp`**。仓库总说明（密钥、启动顺序等）见上一级 **[README.md](../README.md)**。

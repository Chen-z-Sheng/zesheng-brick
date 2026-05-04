# 泽晟搬砖助手 · 管理后台前端（zesheng-brick-admin-web）

基于 **Vue 2** + **Element UI** + **Rspack** 的中后台 SPA，对接 **`zesheng-brick-parent/zesheng-admin-service`**。

> 本仓库由 vue-admin-better 类模板演进而来，当前 README **仅描述本项目落地用法**；通用模板宣传内容与合并冲突片段已移除。

## 技术栈

- Vue 2、Vue Router、Vuex  
- Element UI  
- Axios（封装见 `src/utils/request.js`）  
- Rspack 构建（`npm run serve:rspack` / `npm run build`）

## 环境变量

开发与构建依赖根目录 **`.env.development`**、**`.env.production`**、**`.env.test`**。

核心变量示例（以 `.env.development` 为准）：

- **`VUE_APP_API_BASE_URL`**：若构建链路未改写 `src/config/net.config.js`，则以 **`net.config.js`** 中的 **`baseURL`** 为准。开发环境典型值为 **`http://127.0.0.1:9068/api`**（注意：**不要**写成 `/api/admin`）；业务请求路径在 `src/api` 中以 **`/admin/...`** 开头，拼接后即为后端 **`context-path=/api/admin`** 下的完整 URL（例如 `/api` + `/admin/auth/login` → `/api/admin/auth/login`）。  
- **`VUE_APP_USE_MOCK`**：是否启用 Mock；联调真实后端时请设为 **`false`**。

修改环境变量后需 **重启** 本地开发服务。

## 常用命令

```bash
pnpm install
# 或 npm install

# 本地开发（Rspack）
npm run serve:rspack

# 生产构建
npm run build

# 构建并打 zip（若脚本可用）
npm run build:zip
```

包管理工具以团队约定为准（示例使用 pnpm）。

## 与后端协作说明

| 项目 | 说明 |
|------|------|
| 后端工程 | `zesheng-brick-parent`，启动 **`zesheng-admin-service`** |
| 典型文档地址（开发） | `http://localhost:9068/api/admin/doc.html`（端口以本地为准） |
| 认证 | JWT；前端一般在登录后写入 Storage/Cookie，并由 Axios 拦截器附带 |

批量删除等接口请求体格式以后端 OpenAPI 为准（例如批量删除使用 **`{ "ids": [1,2,3] }`** 形式）。

## Monorepo

仓库总说明见 **`zesheng-brick` 根目录** [README.md](../README.md)。

## 远程仓库（示例）

若已在 GitHub 将仓库改名为 `zesheng-brick-admin-web`，远程地址示例：

```text
https://github.com/Chen-z-Sheng/zesheng-brick-admin-web.git
```

（尚未改名时，以你本地 `git remote -v` 为准。）

## 许可证

以仓库根目录 **LICENSE** 为准（若存在）；开源模板原始协议请勿随意移除版权声明。

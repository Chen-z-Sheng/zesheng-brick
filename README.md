# 泽晟搬砖助手（zesheng-brick）

个人维护的端到端 Monorepo：**后端 API**、**管理后台 Web**、**微信小程序**同仓迭代，接口联调成本低。

## 目录结构

| 目录 | 说明 |
|------|------|
| `zesheng-brick-parent/` | Spring Boot 多模块后端（管理端 + C 端 + common + sys 子模块） |
| `zesheng-brick-admin-web/` | Vue 2 + Element 管理后台前端 |
| `zesheng-brick-mp/` | 微信原生小程序 |

各子项目详情见各自目录下的 **README.md**。若从磁盘单独打开某一子目录开发，仍建议保留本仓库根目录的 Monorepo 结构以便联调。

## 服务器环境变量一键脚本

在服务器上复制模板、填写密钥后 **`source` 进当前 shell**，再启动两个 jar 即可：

- 模板路径：`scripts/zesheng-server-env.example.sh`  
- 建议复制为 `scripts/zesheng-server-env.sh`（该文件名已在仓库根 `.gitignore` 中忽略，避免误提交）

文件内含：**生产必填变量**、**可选默认值**、以及底部**注释区**里「优化前 dev yml 曾出现的旧默认值」仅供回忆。

## 密钥与安全配置说明

- **开发环境**：仓库根 `.gitignore` 已忽略两个模块的 **`application-dev.yml`**（你可本地直接写明文，正常 `git add` 不会把它提交上去）。首次克隆或缺少该文件时，在各模块 `src/main/resources/` 下执行：  
  `cp application-dev.example.yml application-dev.yml`，再按需填写密钥（也可用环境变量覆盖示例里的占位符）。  
  若该文件**曾经进过 Git 历史**，需执行一次：  
  `git rm --cached zesheng-brick-parent/zesheng-admin-service/src/main/resources/application-dev.yml`  
  `git rm --cached zesheng-brick-parent/zesheng-client-service/src/main/resources/application-dev.yml`  
  再提交，之后忽略规则才会彻底生效。
- **生产环境**（`application-prod.yml`）：敏感项须用环境变量注入（见上文 `scripts/zesheng-server-env.example.sh`）。

本地还可选用环境变量、`application-local.yml`（自建且勿提交）等方式覆盖配置。

### 与阿里云相关的常见环境变量（示例）

命名以当前 YAML 为准，可按需增减：

| 用途 | 变量示例 |
|------|-----------|
| OSS | `OSS_ENDPOINT`、`OSS_ACCESS_KEY_ID`、`OSS_ACCESS_KEY_SECRET`、`OSS_BUCKET_NAME` |
| 短信（C 端） | `ALIYUN_SMS_ACCESS_KEY_ID`、`ALIYUN_SMS_ACCESS_KEY_SECRET` 等 |
| 微信 / 快递100 | `WX_APPID`、`WX_SECRET`、`KUAIDI100_KEY` 等（见各环境 yml） |

生产环境务必使用 **RAM 子账号 + 最小权限**，并定期轮转密钥。

## 推荐本地启动顺序

1. 创建数据库并执行 `zesheng-brick-parent` 内各 `db/init`、`db/data` 脚本（顺序见 `zesheng-brick-parent/README.md`）。  
2. 启动 Redis。  
3. 准备好 **`application-dev.yml`**（或从 `application-dev.example.yml` 复制）后启动 **admin-service** 与 **client-service**（`--spring.profiles.active=dev`）。  
4. 启动管理后台前端（`net.config.js` / 环境变量中的接口根路径需与后端一致）。  
5. 微信开发者工具打开小程序，`config` 中 **BASE_URL** 指向 **client-service**。

## 许可证与作者

子项目若单独附带 LICENSE，以该目录为准；整体归属与联系方式见各模块 README。

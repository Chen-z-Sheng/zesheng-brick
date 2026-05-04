# 泽晟搬砖助手 · 后端（zesheng-brick-parent）

企业级多模块 Spring Boot 工程：**管理端 API**、**小程序/C 端 API**、**公共能力**与 **系统域（权限/配置等）** 分层清晰；**独立部署的只有 admin-service 与 client-service**。

同仓通常还与 **`zesheng-brick-mp`**（小程序）、**`zesheng-brick-admin-web`**（管理前端）并列，总览与密钥约定见仓库根目录 **[README.md](../README.md)**。

## 模块说明

| 模块 | 说明 |
|------|------|
| `zesheng-common` | 全局异常、统一响应 `R`、JWT/Redis、MyBatis-Plus、拦截器、快递100 等公共能力 |
| `zesheng-sys-service` | **非独立进程**：无启动类、无单独 `application.yml`，作为 Jar 被 **admin-service** 依赖（权限、系统配置、待办等）。命名含 service 仅为历史习惯，语义上等价于「系统业务子模块」 |
| `zesheng-admin-service` | 管理后台 API，`spring.application.name`：**zesheng-brick-admin-service** |
| `zesheng-client-service` | 微信小程序等 C 端 API，`spring.application.name`：**zesheng-brick-client-service** |

## 技术栈（概要）

- Java **21**、Spring Boot **3.2.x**、Spring Security、JWT  
- MyBatis-Plus、MySQL 8、Redis（Lettuce + 连接池）  
- Knife4j / OpenAPI（开发环境文档；生产可关闭）  
- 阿里云 OSS、短信等（按配置启用）

## 本地运行前置

- JDK 21+、Maven 3.8+  
- MySQL、Redis  
- 开发：将各模块 `application-dev.example.yml` 复制为 **`application-dev.yml`** 并填写密钥（该文件名已在仓库根 `.gitignore` 中忽略）；也可用环境变量覆盖示例里的占位符（**勿将生产密钥提交仓库**）

## 数据库脚本路径

按顺序在目标库执行（具体库名以你的环境为准）：

1. `zesheng-sys-service/src/main/resources/db/init/sys.sql`  
2. `zesheng-sys-service/src/main/resources/db/data/sysData.sql`  
3. `zesheng-admin-service/src/main/resources/db/init/admin.sql`  
4. `zesheng-admin-service/src/main/resources/db/data/adminData.sql`  
5. `zesheng-client-service/src/main/resources/db/data/clientData.sql`（若有）

## 构建与启动

```bash
cd zesheng-brick-parent
mvn clean package -DskipTests
```

仅需启动两个可执行 Jar：

```bash
java -jar zesheng-admin-service/target/zesheng-admin-service-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
java -jar zesheng-client-service/target/zesheng-client-service-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

生产环境建议使用环境变量注入敏感配置，并开启健康检查（示例路径，含 `context-path`）：

- 管理端：`http://{host}:{port}/api/admin/actuator/health`  
- 客户端：`http://{host}:{port}/api/client/actuator/health`

## API 文档（开发环境）

- 管理端：`http://localhost:9068/api/admin/doc.html`  
- 客户端：`http://localhost:9099/api/client/doc.html`  

（端口以各自 `application-dev.yml` 为准。）

## 统一响应约定

业务接口统一封装为 `R`：`code`、`message`、`data`。全局异常由 `zesheng-common` 中 `GlobalExceptionHandler` 转换；校验失败返回 **400** 及明确文案。

## 仓库与联系

- 作者：陈泽晟  
- 后端仓库示例：`https://github.com/Chen-z-Sheng/zesheng-brick-service-java.git`  

许可证：以仓库根目录 **LICENSE** 为准（若存在）。

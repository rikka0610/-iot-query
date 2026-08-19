# IoT Query

一个基于 Spring Boot、Spring Data JPA 和 MySQL 的物联网 SIM 卡查询服务，支持查询 SIM 卡手机号、运营商、生命周期、套餐流量和使用情况，并提供收藏与统计接口。

## 目录

- [功能概览](#功能概览)
- [技术栈](#技术栈)
- [项目结构](#项目结构)
- [配置说明](#配置说明)
- [接口说明](#接口说明)
- [统一响应格式](#统一响应格式)
- [本地运行](#本地运行)
- [Docker 运行](#docker-运行)
- [构建并推送 Docker 镜像](#构建并推送-docker-镜像)
- [GitHub 上传注意事项](#github-上传注意事项)
- [数据库说明](#数据库说明)
- [平台适配器](#平台适配器)
- [常见问题](#常见问题)

## 功能概览

- 单个 SIM 卡查询
- 多个 SIM 卡批量查询
- SIM 卡收藏、取消收藏和收藏列表
- 查询次数与热门 SIM 卡统计
- 查询结果缓存到 MySQL
- 支持不同物联网平台适配器
- Docker Compose 一键启动 MySQL 和应用

## 技术栈

- Java 17
- Spring Boot 4.1.0
- Spring Web MVC
- Spring Data JPA / Hibernate
- MySQL 8.0
- Maven
- Docker / Docker Compose

## 项目结构

```text
.
├── src/
│   ├── main/java/com/baicai/demo/
│   │   ├── controller/       # HTTP 接口
│   │   ├── service/          # 业务逻辑
│   │   ├── repository/       # JPA 数据访问
│   │   ├── entity/           # 数据库实体
│   │   ├── sdk/              # 物联网平台适配器
│   │   └── config/           # 应用配置
│   ├── main/resources/
│   │   ├── application.yml   # 环境变量配置
│   │   └── sql/               # SQL 脚本
│   └── test/
├── Dockerfile
├── docker-compose.yml
├── init.sql
├── pom.xml
├── .env.example
└── .gitignore
```

## 配置说明

应用通过环境变量读取数据库和物联网平台配置。请复制示例文件创建本地配置：

```bash
cp .env.example .env
```

Windows Git Bash 也可以执行：

```bash
cp .env.example .env
```

然后编辑 `.env`，填写真实值：

```env
MYSQL_ROOT_PASSWORD=YOUR_SECURE_MYSQL_PASSWORD
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=YOUR_SECURE_MYSQL_PASSWORD
MY_IOT_PLATFORM=njcl
MY_IOT_API_ID=YOUR_IOT_API_ID
MY_IOT_API_SECRET=YOUR_IOT_API_SECRET
MY_IOT_API_URL=https://customer.iot-njcl.cn/api/v1/sim_cards/get_sim_card_detail
MY_IOT_CONNECT_TIMEOUT=20000
MY_IOT_READ_TIMEOUT=30000
```

说明：

- `MYSQL_ROOT_PASSWORD` 是 MySQL root 用户密码。
- `SPRING_DATASOURCE_USERNAME` 是应用连接 MySQL 使用的用户名。
- `SPRING_DATASOURCE_PASSWORD` 必须与对应数据库用户的密码一致；当前默认使用 root，因此必须与 `MYSQL_ROOT_PASSWORD` 一致。
- `MY_IOT_PLATFORM` 可填 `njcl`、`cmcc` 或 `ctcc`。
- `MY_IOT_API_ID` 和 `MY_IOT_API_SECRET` 是物联网平台提供的凭证。
- `MY_IOT_API_URL` 是物联网平台的 API 接口地址，不是数据库地址或普通网页首页。
- `MY_IOT_API_URL`、API ID 和 API Secret 不要随意修改为不匹配的接口配置。

`.env` 只用于本地或部署服务器，不能提交到 GitHub。`.env.example` 只放占位符，可以提交。

## 接口说明

应用默认地址：

```text
http://localhost:8080
```

所有业务接口前缀为：

```text
/api/iot
```

以下示例使用 `curl`，请将 `89860012345678901234` 替换成实际 ICCID。

### 1. 查询单张 SIM 卡

**请求：**

```http
GET /api/iot/query?iccid=89860012345678901234
```

**curl：**

```bash
curl "http://localhost:8080/api/iot/query?iccid=89860012345678901234"
```

`iccid` 为必填参数。应用收到请求后，会查询本地缓存；需要时通过配置的物联网平台 API 获取最新信息。

### 2. 批量查询 SIM 卡

**请求：**

```http
POST /api/iot/batch-query
Content-Type: application/json
```

**请求体：**

```json
{
  "iccids": [
    "89860012345678901234",
    "89860012345678905678"
  ]
}
```

**curl：**

```bash
curl -X POST "http://localhost:8080/api/iot/batch-query" \
  -H "Content-Type: application/json" \
  -d '{"iccids":["89860012345678901234","89860012345678905678"]}'
```

`iccids` 为必填数组，不能为空。

### 3. 收藏 SIM 卡

**请求：**

```http
POST /api/iot/favorite
Content-Type: application/json
```

**请求体：**

```json
{
  "iccid": "89860012345678901234",
  "remark": "测试卡"
}
```

**curl：**

```bash
curl -X POST "http://localhost:8080/api/iot/favorite" \
  -H "Content-Type: application/json" \
  -d '{"iccid":"89860012345678901234","remark":"测试卡"}'
```

`iccid` 必填，`remark` 可选。

### 4. 取消收藏

**请求：**

```http
DELETE /api/iot/favorite/{iccid}
```

**curl：**

```bash
curl -X DELETE "http://localhost:8080/api/iot/favorite/89860012345678901234"
```

### 5. 查询收藏列表

**请求：**

```http
GET /api/iot/favorites
```

**curl：**

```bash
curl "http://localhost:8080/api/iot/favorites"
```

### 6. 查询统计数据

**请求：**

```http
GET /api/iot/stats
```

**curl：**

```bash
curl "http://localhost:8080/api/iot/stats"
```

返回示例：

```json
{
  "code": 200,
  "data": {
    "totalQueries": 0,
    "todayQueries": 0,
    "top5": []
  },
  "message": "success"
}
```

## 查询结果字段

单个查询和批量查询的 `data` 中会使用以下统一字段：

| 字段 | 类型 | 说明 |
|---|---|---|
| `iccid` | string | SIM 卡 ICCID |
| `msisdn` | string | 手机号 |
| `carrierType` | string | 运营商类型 |
| `lifeCycle` | string | 生命周期状态 |
| `serviceEndTime` | string | 服务到期时间 |
| `packageName` | string | 套餐名称 |
| `packageCapacityKb` | number | 套餐总容量，单位 KB |
| `usedKb` | number | 当前周期已使用流量，单位 KB |
| `remainingKb` | number | 剩余流量，单位 KB |
| `usageRate` | number | 使用率，单位为百分比，例如 `23.45` 表示 23.45% |
| `cycleEndTime` | string | 当前流量周期结束时间 |

流量单位会统一换算为 KB。平台返回 MB、GB 等单位时，应用会自动换算。

## 统一响应格式

成功响应：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

参数错误示例：

```json
{
  "code": 400,
  "message": "iccid 参数不能为空"
}
```

说明：HTTP 状态码和业务响应中的 `code` 都应结合查看，业务数据位于 `data` 字段中。

## 本地运行

### 环境要求

- Java 17（从源码运行或打包时需要）
- Maven，或使用项目自带 Maven Wrapper
- Docker Desktop（使用 Docker MySQL 时需要）

### 方式一：使用 Docker 启动 MySQL

在项目根目录执行：

```bash
docker compose --env-file .env up -d mysql
```

查看状态：

```bash
docker compose ps
```

### 方式二：直接运行 JAR

先确保本机 MySQL 已启动，并且环境变量指向本机数据库。例如 Git Bash：

```bash
export SPRING_DATASOURCE_URL='jdbc:mysql://localhost:3306/iot_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC'
export SPRING_DATASOURCE_USERNAME='root'
export SPRING_DATASOURCE_PASSWORD='你的数据库密码'
```

然后启动：

```bash
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

### 运行测试

测试会启动 Spring 应用上下文。如果测试环境没有数据库配置，可以先跳过测试生成 JAR：

```bash
./mvnw clean package -DskipTests
```

## Docker 运行

当前 `docker-compose.yml` 使用 Docker Hub 镜像：

```yaml
image: rikka202/iot-query:1.0.0
```

确保项目根目录存在填写好配置的 `.env` 后执行：

```bash
docker compose --env-file .env up -d
```

查看状态：

```bash
docker compose ps
```

正常状态应包含：

```text
iot-mysql       Up ... (healthy)
iot-query-app   Up ...
```

查看最近日志，不会持续占用终端：

```bash
docker compose logs --tail=100 app
```

持续查看日志：

```bash
docker compose logs -f app
```

按 `Ctrl+C` 只会退出日志跟踪，不会停止容器。

停止容器但保留数据库数据：

```bash
docker compose down
```

重新启动：

```bash
docker compose --env-file .env up -d
```

应用默认访问地址：

```text
http://localhost:8080
```

当前 Docker MySQL 的宿主机映射端口为 `3307`，因为可能与本机已有 MySQL 的 `3306` 端口冲突：

```text
宿主机访问：127.0.0.1:3307
Docker 内部应用访问：mysql:3306
```

如果使用 Navicat 连接 Docker MySQL，请使用：

```text
主机：127.0.0.1
端口：3307
用户名：root
密码：.env 中的 MYSQL_ROOT_PASSWORD
数据库：iot_db
```

## 构建并推送 Docker 镜像

当前 Dockerfile 依赖已经生成的 JAR，因此从源码构建镜像前先执行：

```bash
./mvnw clean package -DskipTests
```

构建镜像：

```bash
docker build -t rikka202/iot-query:1.0.0 .
```

登录 Docker Hub：

```bash
docker login
```

推荐使用 Docker Hub Personal Access Token（PAT）作为密码。推送镜像：

```bash
docker push rikka202/iot-query:1.0.0
```

如果发布新版本，使用新的标签，例如：

```bash
docker build -t rikka202/iot-query:1.0.1 .
docker push rikka202/iot-query:1.0.1
```

并同步修改 `docker-compose.yml` 中的镜像标签。

## GitHub 上传注意事项

可以上传：

```text
src/
pom.xml
mvnw
mvnw.cmd
.mvn/
Dockerfile
docker-compose.yml
init.sql
README.txt
.env.example
.gitignore
```

不要上传：

```text
.env
target/
真实数据库密码
真实 MY_IOT_API_ID
真实 MY_IOT_API_SECRET
数据库备份
```

`.env.example` 只能包含占位符。真实凭证应只保存在本地开发环境、部署服务器环境变量或服务器私有 `.env` 中。

首次提交示例：

```bash
git init
git branch -M main
git add .gitignore .env.example Dockerfile docker-compose.yml init.sql README.txt pom.xml mvnw mvnw.cmd .mvn src
git commit -m "initial commit"
git remote add origin https://github.com/你的用户名/你的仓库名.git
git push -u origin main
```

## 数据库说明

首次创建 MySQL 数据卷时，Docker 会自动执行根目录的 `init.sql`，创建：

- `sim_card_info`：SIM 卡查询结果缓存
- `favorite`：收藏记录
- `query_log`：查询日志

数据库数据保存在命名卷：

```text
iot-mysql-data
```

初始化脚本只会在数据卷首次创建时自动执行。之后修改 `init.sql`，已有数据卷不会自动重新执行。

只有在确认不需要保留数据库数据时，才执行：

```bash
docker compose down -v
docker compose --env-file .env up -d
```

注意：`docker compose down -v` 会删除 `iot-mysql-data` 数据卷中的数据。

## 平台适配器

通过 `MY_IOT_PLATFORM` 选择适配器：

| 值 | 平台 | 状态 |
|---|---|---|
| `njcl` | 南京诚联 | 当前实现真实 API 调用 |
| `cmcc` | 中国移动 | 当前返回明确标注的模拟数据 |
| `ctcc` | 中国电信 | 当前返回明确标注的模拟数据 |

NJCL 适配器会向配置的 API 地址发送 `POST` 请求，使用表单参数和签名调用平台接口。签名依赖 `MY_IOT_API_ID` 和 `MY_IOT_API_SECRET`，不需要手动拼接请求参数。

如果切换平台，重新创建应用容器：

```bash
docker compose --env-file .env up -d --force-recreate app
```

## 常见问题

### 1. Hibernate 报 `Unable to determine Dialect`

通常是应用无法连接 MySQL。检查：

- MySQL 容器是否为 `healthy`；
- Docker 内部连接地址是否为 `jdbc:mysql://mysql:3306/iot_db`；
- `.env` 中的数据库用户名和密码是否正确；
- 连接 URL 是否包含 `allowPublicKeyRetrieval=true`；
- 应用和 MySQL 是否在同一个 Compose 网络中。

### 2. 3306 端口被占用

当前 Compose 已使用：

```yaml
- "3307:3306"
```

不需要停止本机 MySQL。应用容器内部仍然使用 `mysql:3306`。

### 3. `docker compose logs -f app` 后无法输入命令

这是正常的持续跟踪模式。按 `Ctrl+C` 退出日志查看即可，不会停止容器。也可以使用：

```bash
docker compose logs --tail=100 app
```

### 4. 访问 `/` 返回 500 或错误

项目没有定义根路径 `/` 的接口。请使用实际接口，例如：

```text
http://localhost:8080/api/iot/stats
```

### 5. API 查询失败

检查：

- `MY_IOT_PLATFORM` 是否正确；
- `MY_IOT_API_URL` 是否为平台 API 接口地址；
- `MY_IOT_API_ID` 和 `MY_IOT_API_SECRET` 是否有效；
- Docker 容器是否可以访问外网 HTTPS 服务；
- 查询使用的 ICCID 是否有效。

### 6. GitHub 或 Docker Hub 拒绝登录

Docker Hub 和 GitHub HTTPS 推送建议使用各自平台生成的 Personal Access Token（PAT），不要使用普通账号密码。Token 不要写入命令、源码、`.env.example` 或提交历史。

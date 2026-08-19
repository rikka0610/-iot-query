IoT Query Docker 部署说明
===========================

一、多平台支持
--------------
应用通过 my-iot.platform 选择 SDK 适配器：

  njcl  南京诚联（真实 API）
  cmcc  中国移动（当前为模拟数据）
  ctcc  中国电信（当前为模拟数据）

本地 application.yml 示例：

  my-iot:
    platform: njcl
    api-id: ${MY_IOT_API_ID:your-default-id}
    api-secret: ${MY_IOT_API_SECRET:your-default-secret}

Docker Compose 中修改 app.environment：

  MY_IOT_PLATFORM: njcl
  MY_IOT_API_ID: your-api-id
  MY_IOT_API_SECRET: your-api-secret

切换平台后需要重新创建或重启 app 容器：

  docker compose up -d --force-recreate app

CmccIotSdkAdapter 和 CtccIotSdkAdapter 当前只返回明确标注的模拟数据，不会调用中国移动/中国电信真实 API。接入真实平台时，请分别替换对应类的 queryDeviceInfo 方法，并保持 IotDeviceInfo 的 11 个统一字段不变。

二、敏感配置
------------
数据库密码、MY_IOT_API_ID 和 MY_IOT_API_SECRET 通过环境变量配置，不要提交真实凭证到代码仓库、Dockerfile 或公开镜像。application-docker.yml 由 Compose 挂载到应用容器，应用容器连接数据库时使用 mysql 服务名，而不是 localhost。

三、发布目录
------------
请将以下文件放在同一个目录：

  Dockerfile
  docker-compose.yml
  init.sql
  application-docker.yml
  target/demo-0.0.1-SNAPSHOT.jar

二、运行环境
------------
使用者需要安装并启动 Docker Desktop，且 Docker Compose 可用。

Windows/macOS/Linux 均可使用以下命令：

  docker compose version

应用容器使用 Java 17 运行时镜像，不需要使用者在本机安装 JDK。

三、构建 JAR
------------
如果从源代码发布，先在项目根目录执行：

  mvn clean package

确认以下文件存在：

  target/demo-0.0.1-SNAPSHOT.jar

然后在项目根目录构建 Docker 镜像：

  docker compose build

或直接启动并构建：

  docker compose up -d --build

四、首次部署前必须修改的配置
----------------------------
1. 修改 docker-compose.yml 中的 MySQL root 密码：

  MYSQL_ROOT_PASSWORD: change-this-password
  SPRING_DATASOURCE_PASSWORD: change-this-password

这两个密码必须保持一致。建议改成复杂密码，不要继续使用示例密码。

2. 修改 docker-compose.yml 中的第三方平台配置：

  MY_IOT_API_ID
  MY_IOT_API_SECRET

API Secret 不要提交到公开仓库或公开镜像中。

3. application-docker.yml 使用环境变量读取数据库和第三方平台配置，Compose 会将该文件挂载到应用容器：

  /app/config/application-docker.yml

应用容器连接 MySQL 时使用服务名 mysql，而不是 localhost：

  jdbc:mysql://mysql:3306/iot_db?useSSL=false&serverTimezone=UTC

五、启动和停止
--------------
启动应用和 MySQL：

  docker compose up -d

查看容器状态：

  docker compose ps

查看应用日志：

  docker compose logs -f app

查看 MySQL 日志：

  docker compose logs -f mysql

应用地址：

  http://localhost:8080

停止容器但保留数据库数据：

  docker compose down

重新启动：

  docker compose up -d

不要随意执行以下命令：

  docker compose down -v

该命令会删除 iot-mysql-data 数据卷，数据库缓存也会被删除。

六、数据库初始化说明
--------------------
首次创建 MySQL 数据目录时，MySQL 会自动执行 init.sql，创建以下数据库表：

  sim_card_info
  favorite
  query_log

数据库数据通过命名卷保存：

  iot-mysql-data

init.sql 只会在数据卷首次初始化时自动执行。如果之后修改了 init.sql，已有数据卷不会自动重新执行该脚本。需要手动执行新增 SQL，或在确认数据可以删除后执行：

  docker compose down -v
  docker compose up -d

七、构建并推送到 Docker Hub
--------------------------
假设 Docker Hub 用户名为 rikka202，镜像名称为 iot-query，版本标签为 1.0.0。

1. 登录 Docker Hub：

  docker login

按提示输入 Docker Hub 用户名和密码或 Access Token。

2. 确认 JAR 已生成：

  mvn clean package

3. 构建应用镜像：

  docker build -t rikka202/iot-query:1.0.0 .

4. 推送版本镜像：

  docker push rikka202/iot-query:1.0.0

5. 可选：标记并推送 latest：

  docker tag rikka202/iot-query:1.0.0 rikka202/iot-query:latest
  docker push rikka202/iot-query:latest

注意：Docker Hub 中需要有对应仓库，且当前账号必须拥有推送权限。不要将包含真实数据库密码或 API Secret 的配置文件打进公开镜像。

八、别人拉取镜像并运行
----------------------
如果使用者只获取镜像而不是从源代码构建，需要准备：

  docker-compose.yml
  init.sql
  application-docker.yml

将 docker-compose.yml 中 app 服务的：

  build: .

改成：

  image: rikka202/iot-query:1.0.0

并确认 MySQL 密码、SPRING_DATASOURCE_PASSWORD、MY_IOT_API_ID 和 MY_IOT_API_SECRET 已正确填写。

然后执行：

  docker login
  docker compose pull
  docker compose up -d

查看状态：

  docker compose ps

使用者访问：

  http://localhost:8080

说明：即使应用镜像来自 Docker Hub，MySQL 仍然需要通过本地 mysql:8.0 容器运行。init.sql 会在本地 MySQL 数据卷第一次创建时初始化表结构，每个使用者默认拥有自己独立的数据库缓存。

九、常见问题
------------
1. 应用无法连接 MySQL

检查：

  - mysql 容器是否为 healthy；
  - app 是否使用 jdbc:mysql://mysql:3306/iot_db；
  - 两处数据库密码是否一致；
  - 3306 端口是否被占用。

2. init.sql 修改后没有生效

已有 iot-mysql-data 数据卷时，MySQL 不会重复执行初始化脚本。请手动执行 SQL，或在确认可以清空数据后删除数据卷。

3. 8080 端口被占用

可以修改 docker-compose.yml 的端口映射，例如：

  ports:
    - "18080:8080"

然后访问：

  http://localhost:18080

4. API 调用失败

检查 MY_IOT_API_ID、MY_IOT_API_SECRET、MY_IOT_API_URL 是否正确，以及 Docker 是否可以访问外网 HTTPS 服务。

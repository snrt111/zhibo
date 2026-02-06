# Docker部署说明

## 环境要求

- Docker 20.10+
- Docker Compose 2.0+

## 快速开始

### 1. 克隆项目

```bash
git clone <repository-url>
cd zhibo
```

### 2. 构建项目镜像

#### 方式一：一键构建（推荐）

**Windows (PowerShell):**
```powershell
.\build-all.ps1
```

#### 方式二：分别构建

**构建后端：**
```powershell
.\build-backend.ps1
```

**构建前端：**
```powershell
.\build-frontend.ps1
```

#### 方式三：手动构建

**后端:**
```bash
cd backend
mvn clean package -DskipTests
docker build -t zhibo-backend .
cd ..
```

**前端:**
```bash
cd frontend
npm install
npm run build
docker build -t zhibo-frontend .
cd ..
```

### 3. 启动服务

```bash
docker-compose up -d
```

### 3. 查看服务状态

```bash
docker-compose ps
```

### 4. 查看日志

```bash
# 查看所有服务日志
docker-compose logs -f

# 查看特定服务日志
docker-compose logs -f backend
docker-compose logs -f frontend
```

### 5. 访问服务

- 前端应用: http://localhost
- 后端API: http://localhost:8081/api
- RabbitMQ管理界面: http://localhost:15672 (用户名/密码: guest/guest)

### 6. 停止服务

```bash
# 停止服务但保留数据
docker-compose stop

# 停止并删除容器（保留数据卷）
docker-compose down

# 停止并删除所有数据
docker-compose down -v
```

## 服务说明

### MySQL数据库

- 镜像: mysql:8.0.33
- 端口: 3306
- 数据库: zhibo
- 用户名: root
- 密码: root
- 数据持久化: mysql-data 卷

### Redis缓存

- 镜像: redis:7.0.15
- 端口: 6379
- 数据持久化: redis-data 卷

### RabbitMQ消息队列

- 镜像: rabbitmq:3.12.14-management
- 端口: 5672 (AMQP), 15672 (管理界面)
- 用户名/密码: guest/guest
- 数据持久化: rabbitmq-data 卷

### 后端服务

- 镜像: zhibo-backend (需预先构建)
- 构建方式: 使用本地Maven编译，Docker打包
- 端口: 8081
- 上下文路径: /api
- 健康检查: 每30秒检查一次

### 前端服务

- 镜像: zhibo-frontend (需预先构建)
- 构建方式: 使用本地Node.js编译，Docker打包
- 端口: 80
- 使用Nginx提供静态文件服务
- 反向代理 /api 请求到后端 (http://backend:8081/api)

## 配置说明

### 后端配置 (application.yml)

```yaml
server:
  port: 8081
  servlet:
    context-path: /api

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/zhibo
    username: root
    password: 123456
  redis:
    host: localhost
    port: 6379
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
```

### Docker环境变量覆盖

| 环境变量 | 说明 | 值 |
|---------|------|-----|
| SPRING_DATASOURCE_URL | 数据库连接URL | jdbc:mysql://mysql:3306/zhibo |
| SPRING_DATASOURCE_USERNAME | 数据库用户名 | root |
| SPRING_DATASOURCE_PASSWORD | 数据库密码 | root |
| SPRING_REDIS_HOST | Redis主机 | redis |
| SPRING_REDIS_PORT | Redis端口 | 6379 |
| SPRING_RABBITMQ_HOST | RabbitMQ主机 | rabbitmq |
| SPRING_RABBITMQ_PORT | RabbitMQ端口 | 5672 |
| SERVER_PORT | 服务端口 | 8081 |

## 开发环境

### 重建后端服务

```bash
cd backend
mvn clean package -DskipTests
docker build -t zhibo-backend .
cd ..
docker-compose up -d backend
```

### 重建前端服务

```bash
docker-compose up -d --build frontend
```

### 进入容器

```bash
# 进入后端容器
docker-compose exec backend sh

# 进入MySQL容器
docker-compose exec mysql mysql -uroot -proot zhibo
```

## 生产环境部署

### 1. 环境变量配置

创建 `.env` 文件覆盖默认配置：

```env
MYSQL_ROOT_PASSWORD=your-root-password
SPRING_DATASOURCE_PASSWORD=your-password
```

### 2. 使用生产环境配置

```bash
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

### 3. 数据库备份

```bash
# 备份数据库
docker-compose exec mysql mysqldump -uroot -proot zhibo > backup.sql

# 恢复数据库
docker-compose exec -T mysql mysql -uroot -proot zhibo < backup.sql
```

## 故障排查

### 服务无法启动

1. 检查端口是否被占用
2. 查看日志获取详细错误信息

```bash
docker-compose logs <service-name>
```

### 数据库连接失败

1. 确认MySQL服务健康检查通过
2. 检查环境变量配置
3. 查看后端日志

### 前端无法访问后端API

1. 检查Nginx配置 (frontend/nginx.conf)
2. 确认后端服务已启动
3. 查看浏览器控制台错误

## 端口映射

| 服务 | 主机端口 | 容器端口 | 说明 |
|------|---------|---------|------|
| frontend | 80 | 80 | 前端Web服务 |
| backend | 8081 | 8081 | 后端API服务 |
| mysql | 3306 | 3306 | MySQL数据库 |
| redis | 6379 | 6379 | Redis缓存 |
| rabbitmq | 5672 | 5672 | RabbitMQ AMQP |
| rabbitmq | 15672 | 15672 | RabbitMQ管理界面 |

## 性能优化

### 1. 数据库优化

- 增加连接池大小
- 启用查询缓存
- 定期清理慢查询日志

### 2. Redis优化

- 配置最大内存策略
- 启用RDB持久化
- 配置内存淘汰策略

### 3. 后端优化

- 增加JVM内存
```yaml
environment:
  JAVA_OPTS: "-Xmx512m -Xms256m"
```

### 4. 前端优化

- 启用gzip压缩（已配置）
- 配置CDN
- 启用浏览器缓存（已配置）

## 监控

### 查看资源使用情况

```bash
docker stats
```

### 查看容器日志

```bash
docker-compose logs -f --tail=100
```

## 安全建议

1. 修改默认密码
2. 限制端口访问范围
3. 启用HTTPS
4. 配置防火墙规则
5. 定期更新镜像版本

## 常见问题

### 1. 镜像拉取失败

如果遇到镜像拉取失败（size validation错误），执行以下步骤：

```bash
# 清理Docker缓存
docker system prune -af
docker builder prune -af

# 重启Docker Desktop
# 然后重新拉取
docker-compose pull
```

### 2. 健康检查失败

后端健康检查依赖 `/api/health` 接口，确保后端已实现该接口。

### 3. 前端无法连接后端

检查 `frontend/nginx.conf` 中的 `proxy_pass` 配置，确保端口正确。

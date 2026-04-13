<div align="center">

# 🎬 直播平台

一个功能完整的现代化直播平台，支持实时直播、弹幕互动、礼物打赏等核心功能

[![Vue](https://img.shields.io/badge/Vue-3.5-brightgreen.svg)](https://vuejs.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Docker](https://img.shields.io/badge/Docker-Compose-blue.svg)](https://www.docker.com/)
[![License](https://img.shields.io/badge/license-MIT-yellow.svg)](LICENSE)

[功能特性](#-功能特性) • [技术栈](#-技术栈) • [依赖说明](#-依赖库使用场景与配置) • [快速开始](#-快速开始) • [项目结构](#-项目结构) • [使用指南](#-使用指南)

</div>

## 📋 功能特性

### 核心功能
- ✅ **用户系统** - 注册、登录（密码/验证码/第三方）、角色管理（普通用户/管理员）
- ✅ **直播推流** - RTMP协议推流，支持FFmpeg
- ✅ **直播观看** - HLS协议，低延迟播放
- ✅ **弹幕系统** - 实时弹幕发送与展示
- ✅ **礼物系统** - 礼物打赏，礼物记录
- ✅ **实时统计** - 观看人数实时更新
- ✅ **直播管理** - 创建、管理直播间
- ✅ **数据分析** - 用户增长、直播排行、礼物统计

### AI智能功能
- 🤖 **AI图像生成** - 基于文本描述智能生成图片，支持多种AI模型（Ollama本地模型、第三方API）
- 🔍 **智能内容审核** - 敏感词自动检测与过滤，支持多级别风险管控
- ⚙️ **AI模型管理** - 可视化配置多种AI模型，支持动态切换与优先级管理

### 技术特性
- 🚀 前后端分离架构
- 📱 响应式设计，支持PC端
- 🔒 JWT令牌认证
- 💬 WebSocket实时通信
- 📦 Docker容器化部署
- 🔍 Elasticsearch全文搜索
- 📨 RabbitMQ消息队列
- ⚡ Redis缓存加速
- 🤖 Spring AI集成

## 🛠 技术栈

### 前端依赖

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.5.24 | 前端框架 |
| Vue Router | 5.0.2 | 路由管理 |
| TypeScript | 5.9.3 | 类型系统 |
| Vite | 7.2.4 | 构建工具 |
| Ant Design Vue | 4.2.6 | UI组件库 |
| Axios | 1.13.4 | HTTP客户端 |
| hls.js | 1.5.8 | HLS视频播放 |
| sockjs-client | 1.6.1 | WebSocket客户端 |
| stompjs | 2.3.3 | STOMP协议支持 |

### 后端依赖

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.5.6 | 后端框架 |
| Java | 17 | JDK版本 |
| MyBatis Plus | 3.5.3.2 | ORM框架 |
| MySQL Connector | 8.0.33 | 数据库驱动 |
| JWT (jjwt) | 0.11.5 | 身份认证 |
| Spring AI | 1.0.0-M6 | AI能力集成框架 |
| Spring AI OpenAI | 1.0.0-M6 | OpenAI模型接入 |

### 基础设施

| 服务 | 镜像版本 | 端口 |
|------|----------|------|
| MySQL | 8.0.33 | 3306:3306 |
| Redis | 7.0.15-alpine | 6379:6379 |
| RabbitMQ | 3.12.14-management-alpine | 5672:5672, 15672:15672 |
| Elasticsearch | 8.13.0 | 9200:9200, 9300:9300 |
| MinIO | latest | 9000:9000, 9001:9001 |
| Nginx-RTMP | alfg/nginx-rtmp | 1935:1935, 8080:80 |

## 📦 依赖库使用场景与配置

### 前端依赖详解

#### Vue 3.x
- **使用场景**：构建单页面应用(SPA)，实现响应式用户界面
- **核心功能**：
  - 组件化开发（直播列表、直播间、管理后台等组件）
  - 响应式数据绑定（实时更新观看人数、弹幕等）
  - Composition API组织代码逻辑
- **配置文件**：`frontend/src/main.ts`、`frontend/src/App.vue`

#### Vue Router 5.x
- **使用场景**：前端路由管理，实现页面导航
- **核心功能**：
  - 路由守卫（登录验证、权限控制）
  - 动态路由（直播间详情页）
  - 嵌套路由（管理后台子页面）
- **配置文件**：`frontend/src/router/index.ts`

#### Ant Design Vue
- **使用场景**：提供丰富的UI组件
- **核心功能**：
  - 表单组件（登录、注册表单）
  - 数据展示（表格、卡片、列表）
  - 弹窗、通知、加载状态等交互组件
- **配置文件**：`frontend/src/main.ts`（全局引入）

#### Axios
- **使用场景**：HTTP请求封装，与后端API通信
- **核心功能**：
  - RESTful API调用
  - 请求/响应拦截器
  - JWT Token自动附加
- **配置文件**：`frontend/src/api/request.ts`
- **使用示例**：
```typescript
// 配置请求拦截器
request.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});
```

#### hls.js
- **使用场景**：HLS协议视频流播放
- **核心功能**：
  - 解析M3U8播放列表
  - 自适应码率切换
  - 低延迟直播播放
- **使用位置**：`frontend/src/components/LiveRoom.vue`
- **使用示例**：
```typescript
const hls = new Hls();
hls.loadSource(playUrl);
hls.attachMedia(videoElement);
```

#### sockjs-client + stompjs
- **使用场景**：WebSocket双向通信
- **核心功能**：
  - 实时弹幕收发
  - 礼物消息推送
  - 观看人数实时更新
- **配置文件**：`frontend/src/utils/websocket.ts`
- **使用示例**：
```typescript
const socket = new SockJS('/api/ws');
const stompClient = Stomp.over(socket);
stompClient.connect({}, () => {
  stompClient.subscribe('/topic/danmaku/' + liveId, callback);
});
```

#### echarts + vue-echarts
- **使用场景**：数据可视化图表
- **核心功能**：
  - 用户增长趋势图
  - 礼物统计图表
  - 直播数据分析仪表盘
- **使用位置**：`frontend/src/components/admin/AnalyticsDashboard.vue`

---

### AI功能详解

#### Spring AI 核心框架
- **使用场景**：集成多种AI能力，统一AI服务调用接口
- **核心功能**：
  - 多模型支持（OpenAI、Ollama等）
  - 统一的ImageModel接口
  - 动态模型配置与切换
- **依赖版本**：`1.0.0-M6`
- **配置文件**：`backend/src/main/java/com/zhibo/backend/config/AiModelManager.java`

#### AI图像生成服务
- **使用场景**：根据用户文本描述智能生成图片
- **解决的问题**：
  - 主播需要个性化封面图但缺乏设计能力
  - 用户想要自定义表情包、头像等创意内容
  - 降低内容创作门槛，提升用户参与度
- **技术实现**：
  - 支持OpenAI DALL-E、Ollama本地模型等多种图像生成模型
  - 图片自动上传至MinIO对象存储
  - 生成历史记录持久化存储
- **核心代码**：
  - 服务层：`backend/src/main/java/com/zhibo/backend/service/ImageGenerationService.java`
  - 控制层：`backend/src/main/java/com/zhibo/backend/controller/ToolsController.java`
  - 前端组件：`frontend/src/components/tools/ImageGenerator.vue`
- **使用流程**：
  1. 用户在工具页面输入图片描述
  2. 系统调用配置的AI模型生成图片
  3. 图片上传至MinIO并返回访问链接
  4. 生成记录保存至数据库，支持历史查看

#### AI模型配置管理
- **使用场景**：灵活管理多个AI模型配置，支持动态切换
- **解决的问题**：
  - 不同场景需要不同的AI模型（本地模型保护隐私、云端模型效果更好）
  - 模型故障时自动降级切换
  - 成本控制（本地模型免费、云端模型按量付费）
- **核心功能**：
  - 支持Ollama本地模型、第三方API、图片生成模型三种类型
  - 优先级配置，自动选择最优可用模型
  - 模型状态管理（启用/禁用）
  - 缓存机制提升性能
- **数据模型**：`backend/src/main/java/com/zhibo/backend/entity/AiModelConfig.java`
- **管理界面**：`frontend/src/components/admin/AiConfigManagement.vue`
- **配置示例**：
```java
// Ollama本地模型配置
name: "本地LLaVA模型"
type: "ollama"
baseUrl: "http://localhost:11434"
modelName: "llava"

// 第三方OpenAI兼容API配置
name: "OpenAI DALL-E"
type: "image"
baseUrl: "https://api.openai.com/v1"
apiKey: "sk-..."
modelName: "dall-e-3"
```

#### 智能内容审核
- **使用场景**：自动检测和过滤用户发布的不当内容
- **解决的问题**：
  - 实时过滤弹幕中的敏感信息
  - 防止违规直播内容传播
  - 降低人工审核成本
  - 保护平台合规性
- **审核策略**：
  | 风险等级 | 处理方式 | 说明 |
  |----------|----------|------|
  | 0级（无风险） | 直接通过 | 正常内容 |
  | 1级（低风险） | 过滤后发布 | 敏感词替换为* |
  | 2级（中风险） | 待人工审核 | 需要管理员确认 |
  | 3级（高风险） | 直接拦截 | 禁止发布 |
- **核心代码**：
  - 审核服务：`backend/src/main/java/com/zhibo/backend/service/ContentAuditService.java`
  - 敏感词服务：`backend/src/main/java/com/zhibo/backend/service/SensitiveWordService.java`
- **应用场景**：
  - 弹幕发送前自动检测
  - 直播间标题、描述审核
  - 用户评论内容过滤

---

### 后端依赖详解

#### Spring Boot Starter Web
- **使用场景**：构建RESTful API服务
- **核心功能**：
  - HTTP请求处理
  - 控制器路由分发
  - JSON序列化/反序列化
- **配置文件**：`backend/src/main/resources/application.yml`
- **配置示例**：
```yaml
server:
  port: 8081
  servlet:
    context-path: /api
```

#### Spring Boot Starter Security
- **使用场景**：安全框架，身份认证与授权
- **核心功能**：
  - JWT Token验证
  - 密码加密（BCrypt）
  - 请求路径权限控制
- **配置文件**：`backend/src/main/java/com/zhibo/backend/config/WebSecurityConfig.java`
- **配置示例**：
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

#### Spring Boot Starter Data Redis
- **使用场景**：缓存与数据存储
- **核心功能**：
  - 会话缓存（用户登录状态）
  - 观看人数实时统计
  - 热点数据缓存
- **配置文件**：`backend/src/main/java/com/zhibo/backend/config/RedisConfig.java`
- **配置示例**：
```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      lettuce:
        pool:
          max-active: 20
```

#### Spring Boot Starter AMQP (RabbitMQ)
- **使用场景**：消息队列，异步处理
- **核心功能**：
  - 弹幕消息异步存储
  - 礼物记录异步处理
  - 系统解耦
- **配置示例**：
```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
```

#### Spring Boot Starter Data Elasticsearch
- **使用场景**：全文搜索与数据分析
- **核心功能**：
  - 直播标题搜索
  - 用户昵称搜索
  - 日志聚合分析
- **配置示例**：
```yaml
spring:
  elasticsearch:
    uris: http://localhost:9200
    socket-timeout: 30s
```

#### Spring Boot Starter WebSocket
- **使用场景**：WebSocket服务端
- **核心功能**：
  - 弹幕实时推送
  - 礼物消息广播
  - 直播状态同步
- **配置文件**：`backend/src/main/java/com/zhibo/backend/config/WebSocketConfig.java`
- **配置示例**：
```java
@Override
public void configureMessageBroker(MessageBrokerRegistry config) {
    config.enableSimpleBroker("/topic", "/queue");
    config.setApplicationDestinationPrefixes("/app");
}
```

#### MyBatis Plus
- **使用场景**：ORM框架，数据库操作
- **核心功能**：
  - CRUD操作封装
  - 分页查询
  - 条件构造器
- **配置文件**：`backend/src/main/java/com/zhibo/backend/config/MyBatisPlusConfig.java`
- **配置示例**：
```yaml
mybatis-plus:
  mapper-locations: classpath*:mapper/**/*.xml
  type-aliases-package: com.zhibo.backend.entity
  configuration:
    map-underscore-to-camel-case: true
```

#### JWT (jjwt)
- **使用场景**：无状态身份认证
- **核心功能**：
  - Token生成与验证
  - 用户信息加密存储
  - 过期时间控制
- **配置文件**：`backend/src/main/java/com/zhibo/backend/utils/JwtUtil.java`
- **配置示例**：
```yaml
jwt:
  secret: zhibo-secret-key-2026-change-in-production
  expiration: 86400000
  header: Authorization
  prefix: Bearer
```

#### Lombok
- **使用场景**：简化Java代码
- **核心功能**：
  - 自动生成getter/setter
  - 日志注解(@Slf4j)
  - 构造器自动生成
- **使用示例**：
```java
@Data
@Entity
public class User {
    private Long id;
    private String username;
}
```

#### Jackson JSR310
- **使用场景**：Java 8日期时间序列化
- **核心功能**：
  - LocalDateTime序列化
  - 时间格式自定义
  - 前后端日期交互
- **配置文件**：`backend/src/main/java/com/zhibo/backend/config/JacksonConfig.java`

---

### 基础设施服务详解

#### MySQL
- **使用场景**：主数据库，持久化存储
- **存储数据**：
  - 用户信息（用户名、密码、角色）
  - 直播信息（标题、描述、状态）
  - 弹幕记录、礼物记录
  - 分类数据
- **配置示例**：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/zhibo?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: 123456
```

#### Redis
- **使用场景**：缓存与实时数据
- **存储数据**：
  - 用户Session
  - 直播在线人数统计
  - 热点数据缓存
  - 分布式锁

#### RabbitMQ
- **使用场景**：消息队列
- **应用场景**：
  - 弹幕异步持久化
  - 礼物记录异步处理
  - 日志收集
  - 系统解耦

#### Elasticsearch
- **使用场景**：搜索引擎
- **应用场景**：
  - 直播搜索（按标题、分类）
  - 用户搜索（按昵称）
  - 日志分析
  - 数据统计

#### Nginx-RTMP
- **使用场景**：流媒体服务器
- **核心功能**：
  - RTMP推流接收
  - HLS切片生成
  - 直播流分发
- **配置文件**：`nginx-rtmp.conf`
- **推流地址格式**：`rtmp://localhost:1935/live/<stream-key>`
- **拉流地址格式**：`http://localhost:8080/hls/<stream-key>.m3u8`

#### MinIO
- **使用场景**：对象存储服务
- **核心功能**：
  - 头像、图片存储
  - 文件上传与管理
  - 支持S3协议
  - 内置缓存加速
- **配置文件**：`backend/src/main/java/com/zhibo/backend/config/MinioConfig.java`
- **管理界面**：`http://localhost:9001`
- **默认账号**：minioadmin / minioadmin123
- **配置示例**：
```yaml
minio:
  endpoint: http://localhost:9000
  accessKey: minioadmin
  secretKey: minioadmin123
  bucketName: zhibo-files
```

## 🚀 快速开始

### 环境要求

| 软件 | 推荐版本 | 说明 |
|------|----------|------|
| JDK | 17+ | Spring Boot 3.x 要求 |
| Node.js | 18+ | 前端开发环境 |
| Maven | 3.8+ | 后端构建工具 |
| Docker | 20.10+ | 容器化部署 |
| Docker Compose | 2.0+ | 服务编排 |

### 一键部署

```powershell
# 1. 构建镜像（后端 + 前端）
.\build-all.ps1

# 2. 启动所有服务
docker-compose up -d

# 3. 访问应用
# 前端界面: http://localhost
# 后端API:  http://localhost:8081
```

### 本地开发

```powershell
# 1. 启动基础设施服务
docker-compose up -d mysql redis rabbitmq elasticsearch minio nginx-rtmp

# 2. 启动后端（IDE或命令行）
cd backend
mvn spring-boot:run

# 3. 启动前端
cd frontend
npm install
npm run dev
```

## 📁 项目结构

```
zhibo/
├── backend/                      # 后端服务
│   ├── src/main/
│   │   ├── java/com/zhibo/backend/
│   │   │   ├── config/           # 配置类
│   │   │   │   ├── AiModelManager.java      # AI模型管理器
│   │   │   │   ├── MinioConfig.java         # MinIO对象存储配置
│   │   │   │   └── ...
│   │   │   ├── controller/       # 控制器
│   │   │   │   ├── AiModelConfigController.java   # AI模型配置管理
│   │   │   │   ├── ToolsController.java           # AI工具接口
│   │   │   │   └── ...
│   │   │   ├── entity/           # 实体类
│   │   │   │   ├── AiModelConfig.java       # AI模型配置实体
│   │   │   │   ├── GeneratedImage.java      # 生成图片记录实体
│   │   │   │   ├── ContentAudit.java        # 内容审核记录实体
│   │   │   │   └── ...
│   │   │   ├── mapper/           # 数据访问层
│   │   │   ├── service/          # 业务逻辑层
│   │   │   │   ├── ImageGenerationService.java    # AI图像生成服务
│   │   │   │   ├── ContentAuditService.java       # 内容审核服务
│   │   │   │   ├── SensitiveWordService.java      # 敏感词服务
│   │   │   │   └── ...
│   │   │   └── utils/            # 工具类
│   │   └── resources/
│   │       ├── application.yml   # 应用配置
│   │       └── sql/init.sql      # 数据库初始化脚本
│   ├── Dockerfile
│   └── pom.xml
│
├── frontend/                     # 前端服务
│   ├── src/
│   │   ├── api/                  # API接口封装
│   │   │   ├── aiConfig.ts       # AI配置管理API
│   │   │   └── tools.ts          # AI工具API
│   │   ├── components/           # Vue组件
│   │   │   ├── admin/            # 管理后台组件
│   │   │   │   └── AiConfigManagement.vue   # AI模型配置管理
│   │   │   ├── anchor/           # 主播端组件
│   │   │   ├── tools/            # AI工具组件
│   │   │   │   └── ImageGenerator.vue       # AI图像生成器
│   │   │   └── ...
│   │   ├── router/               # 路由配置
│   │   ├── types/                # TypeScript类型定义
│   │   └── utils/                # 工具函数
│   ├── dist/                     # 构建输出目录
│   ├── Dockerfile
│   ├── nginx.conf                # Nginx配置
│   └── package.json
│
├── docker-compose.yml            # Docker编排文件
├── nginx-rtmp.conf               # 流媒体配置
├── build-all.ps1                 # 构建脚本
├── build-backend.ps1             # 后端构建脚本
├── build-frontend.ps1            # 前端构建脚本
└── README.md                     # 项目说明
```

## 🔧 配置说明

### 端口映射

| 服务 | 外部端口 | 内部端口 | 说明 |
|------|----------|----------|------|
| MySQL | 3306 | 3306 | 数据库 |
| Redis | 6379 | 6379 | 缓存 |
| RabbitMQ | 5672 / 15672 | 5672 / 15672 | 消息队列 / 管理界面 |
| Elasticsearch | 9200 | 9200 | 搜索引擎 |
| 后端 | 8081 | 8081 | API服务 |
| 前端 | 80 | 80 | Web服务 |
| RTMP | 1935 | 1935 | 推流端口 |
| HLS | 8080 | 80 | 拉流端口 |

### 默认账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | 123456 |
| 普通用户 | user1 | 123456 |
| 普通用户 | user2 | 123456 |

## 📖 使用指南

### 推流配置

**FFmpeg推流：**
```bash
ffmpeg -re -i input.mp4 -c:v libx264 -preset veryfast -c:a aac -f flv rtmp://localhost:1935/live/<stream-key>
```

### 观看直播

- **HLS地址**：`http://localhost:8080/hls/<stream-key>.m3u8`
- **前端界面**：访问 `http://localhost` 进入直播列表选择观看

### API接口

```bash
# 健康检查
curl http://localhost:8081/api/health

# 获取直播列表
curl http://localhost:8081/live/list

# 获取直播详情
curl http://localhost:8081/live/detail/{liveId}

# 获取分类列表
curl http://localhost:8081/live/category/list

# 用户登录（密码）
curl -X POST http://localhost:8081/api/user/login \
     -H "Content-Type: application/json" \
     -d '{"username":"admin","password":"123456"}'

# 用户登录（手机号验证码）
curl -X POST http://localhost:8081/api/user/login-with-phone \
     -H "Content-Type: application/json" \
     -d '{"phone":"13800138000","code":"123456"}'

# 发送短信验证码
curl -X POST http://localhost:8081/api/user/send-sms-code \
     -H "Content-Type: application/json" \
     -d '{"phone":"13800138000"}'

# 微信登录
curl -X POST http://localhost:8081/api/user/oauth2/wechat \
     -H "Content-Type: application/json" \
     -d '{"code":"wechat_auth_code"}'

# QQ登录
curl -X POST http://localhost:8081/api/user/oauth2/qq \
     -H "Content-Type: application/json" \
     -d '{"code":"qq_auth_code"}'

# 微博登录
curl -X POST http://localhost:8081/api/user/oauth2/weibo \
     -H "Content-Type: application/json" \
     -d '{"code":"weibo_auth_code"}'

# 用户注册
curl -X POST http://localhost:8081/api/user/register \
     -H "Content-Type: application/json" \
     -d '{"username":"test","password":"123456","nickname":"测试用户"}'

# 创建直播（需登录）
curl -X POST http://localhost:8081/live/create \
     -H "Authorization: Bearer {token}" \
     -H "Content-Type: application/json" \
     -d '{"title":"我的直播","description":"欢迎观看","categoryId":1}'

# 获取推流地址
curl -H "Authorization: Bearer {token}" http://localhost:8081/live/push-url/{liveId}

# 获取播放地址
curl http://localhost:8081/live/play-url/{liveId}

# 发送弹幕
curl -X POST http://localhost:8081/danmaku/send \
     -H "Authorization: Bearer {token}" \
     -H "Content-Type: application/json" \
     -d '{"liveId":1,"content":"666"}'

# 获取礼物列表
curl http://localhost:8081/gift/list

### AI工具接口

```bash
# 获取可用的AI图像生成模型列表
curl http://localhost:8081/api/tools/image/models

# AI生成图片（需登录）
curl -X POST http://localhost:8081/api/tools/image/generate \
     -H "Authorization: Bearer {token}" \
     -H "Content-Type: application/json" \
     -d '{"prompt":"一只可爱的猫咪在花园里玩耍，阳光明媚，色彩鲜艳","modelId":1}'

# 获取图片生成历史（需登录）
curl -H "Authorization: Bearer {token}" http://localhost:8081/api/tools/image/history

### 管理员接口 - AI模型配置

```bash
# 获取AI模型配置列表（管理员权限）
curl -H "Authorization: Bearer {admin_token}" \
     "http://localhost:8081/api/admin/ai-config/list?page=1&size=10"

# 添加AI模型配置（管理员权限）
curl -X POST http://localhost:8081/api/admin/ai-config \
     -H "Authorization: Bearer {admin_token}" \
     -H "Content-Type: application/json" \
     -d '{
       "name": "Ollama本地模型",
       "type": "ollama",
       "baseUrl": "http://localhost:11434",
       "modelName": "llava",
       "status": 1,
       "priority": 0
     }'

# 更新AI模型配置（管理员权限）
curl -X PUT http://localhost:8081/api/admin/ai-config \
     -H "Authorization: Bearer {admin_token}" \
     -H "Content-Type: application/json" \
     -d '{
       "id": 1,
       "name": "更新后的模型名称",
       "baseUrl": "http://new-url:11434",
       "status": 1
     }'

# 删除AI模型配置（管理员权限）
curl -X DELETE http://localhost:8081/api/admin/ai-config/1 \
     -H "Authorization: Bearer {admin_token}"

# 更新模型状态（启用/禁用）
curl -X PUT http://localhost:8081/api/admin/ai-config/1/status \
     -H "Authorization: Bearer {admin_token}" \
     -H "Content-Type: application/json" \
     -d '{"status": 0}'
```

## 🐳 Docker部署

### 服务编排

```
mysql ────────┐
redis ────────┤
rabbitmq ─────┼──> backend ──┬──> frontend
elasticsearch─┘              │
nginx-rtmp ──────────────────┘
```

### 常用命令

```powershell
# 启动服务
docker-compose up -d

# 查看状态
docker-compose ps

# 查看日志
docker-compose logs -f
docker-compose logs -f backend

# 停止服务
docker-compose down

# 重启服务
docker-compose restart
```

### AI功能使用指南

#### 配置AI图像生成

1. **准备AI模型服务**
   - **方案一：Ollama本地部署**（推荐，免费）
     ```bash
     # 安装Ollama
     # 访问 https://ollama.com 下载安装
     
     # 拉取图像生成模型
     ollama pull llava
     
     # 启动Ollama服务（默认端口11434）
     ollama serve
     ```
   
   - **方案二：OpenAI API**（按量付费，效果更好）
     - 获取API Key：https://platform.openai.com
     - 确保账户有可用额度

2. **在管理后台配置模型**
   - 登录管理员账号
   - 进入"AI模型配置"页面
   - 点击"添加配置"
   - 填写模型信息：
     - **Ollama配置示例**：
       ```
       名称：本地LLaVA模型
       类型：ollama
       API地址：http://localhost:11434
       模型标识：llava
       优先级：0（数字越小优先级越高）
       ```
     - **OpenAI配置示例**：
       ```
       名称：OpenAI DALL-E
       类型：image
       API地址：https://api.openai.com/v1
       API密钥：sk-your-api-key
       模型标识：dall-e-3
       优先级：1
       ```

3. **使用AI图像生成**
   - 进入"AI工具"页面
   - 选择已配置的AI模型
   - 输入图片描述（支持中文）
   - 点击生成，等待图片生成完成
   - 生成的图片会自动保存到历史记录

#### 内容审核配置

1. **管理敏感词库**
   - 进入管理后台"内容审核"页面
   - 添加敏感词，设置风险等级：
     - 1级：低风险，自动过滤
     - 2级：中风险，人工审核
     - 3级：高风险，直接拦截

2. **审核策略说明**
   - 弹幕发送前自动检测
   - 包含敏感词的内容按等级处理
   - 审核记录可在后台查看

## 🔍 监控与维护

### 管理界面

| 服务 | 地址 | 账号/密码 |
|------|------|-----------|
| RabbitMQ | http://localhost:15672 | guest / guest |
| Elasticsearch | http://localhost:9200 | 无认证 |

### 数据备份

```bash
# 备份MySQL
docker exec zhibo-mysql mysqldump -u root -p123456 zhibo > backup.sql

# 恢复MySQL
docker exec -i zhibo-mysql mysql -u root -p123456 zhibo < backup.sql
```

### 清理资源

```bash
# 清理无用镜像和容器
docker system prune -a

# 查看资源使用
docker stats
```

## ❓ 常见问题

### 推流失败
1. 检查nginx-rtmp容器状态：`docker-compose ps nginx-rtmp`
2. 确认推流地址格式正确
3. 检查防火墙是否阻止1935端口

### 直播延迟高
调整FFmpeg推流参数：
```bash
-preset ultrafast -tune zerolatency -g 30
```

### 后端启动失败
查看日志定位问题：
```bash
docker-compose logs -f backend
```

### AI图像生成失败
1. **检查AI模型服务状态**
   - Ollama：`curl http://localhost:11434/api/tags`
   - OpenAI：检查API Key是否有效

2. **检查模型配置**
   - 确认管理后台已添加有效的AI模型配置
   - 检查baseUrl是否正确（注意末尾不要加/）
   - 确认模型状态为"启用"

3. **查看后端日志**
   ```bash
   docker-compose logs -f backend | grep -i "image\|ai\|model"
   ```

4. **常见问题**
   - **连接超时**：检查Ollama服务是否启动，防火墙是否放行11434端口
   - **API Key无效**：OpenAI账户余额不足或Key被吊销
   - **模型不存在**：执行`ollama pull llava`下载模型

### 内容审核不生效
1. 检查敏感词库是否已加载
2. 确认敏感词状态为"启用"
3. 查看审核日志：`backend/logs/`目录下

## 📄 许可证

本项目采用 MIT 许可证开源

## 🤝 贡献

欢迎提交Issue和Pull Request！

## 📧 联系方式

如有问题，请通过Issue联系

---


@echo off

:: 开发环境配置文件
:: 版本: 1.0.0
:: 日期: 2026-02-14

:: 项目根目录（自动获取）
set "PROJECT_ROOT=%~dp0"

:: 上传配置
set "UPLOAD_PATH=%PROJECT_ROOT%backend\uploads"
set "UPLOAD_URL_PREFIX=http://localhost:8081/api/uploads"
set "UPLOAD_USE_MINIO=true"

:: MinIO配置
set "MINIO_ENDPOINT=http://localhost:9000"
set "MINIO_PUBLIC_URL=http://localhost:9000"
set "MINIO_ACCESS_KEY=minioadmin"
set "MINIO_SECRET_KEY=minioadmin123"
set "MINIO_BUCKET_NAME=zhibo-files"

:: JWT配置
set "JWT_SECRET=zhibo-secret-key-2026-change-in-production"
set "JWT_EXPIRATION=86400000"

:: 日志配置
set "LOG_LEVEL_ROOT=INFO"
set "LOG_LEVEL_ZHIBO=DEBUG"

:: 数据库配置（开发环境）
set "SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/zhibo?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true"
set "SPRING_DATASOURCE_USERNAME=root"
set "SPRING_DATASOURCE_PASSWORD=123456"

:: Redis配置
set "SPRING_REDIS_HOST=localhost"
set "SPRING_REDIS_PORT=6379"

:: RabbitMQ配置
set "SPRING_RABBITMQ_HOST=localhost"
set "SPRING_RABBITMQ_PORT=5672"
set "SPRING_RABBITMQ_USERNAME=guest"
set "SPRING_RABBITMQ_PASSWORD=guest"

:: Elasticsearch配置
set "SPRING_ELASTICSEARCH_URIS=http://localhost:9200"

echo 开发环境配置已加载!
echo 项目根目录: %PROJECT_ROOT%
echo 上传路径: %UPLOAD_PATH%
echo MinIO地址: %MINIO_ENDPOINT%
echo.
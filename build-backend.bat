@echo off
echo ========================================
echo 构建后端项目
echo ========================================

cd backend

echo 1. 使用Maven构建项目...
call mvn clean package -DskipTests

if %errorlevel% neq 0 (
    echo 构建失败！
    pause
    exit /b 1
)

echo 2. 构建Docker镜像...
docker build -t zhibo-backend .

if %errorlevel% neq 0 (
    echo Docker镜像构建失败！
    pause
    exit /b 1
)

echo ========================================
echo 构建完成
echo ========================================
pause

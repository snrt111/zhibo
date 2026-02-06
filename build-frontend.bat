@echo off
echo ========================================
echo 构建前端项目
echo ========================================

cd frontend

echo 1. 安装依赖...
call npm install

if %errorlevel% neq 0 (
    echo 依赖安装失败！
    pause
    exit /b 1
)

echo 2. 构建项目...
call npm run build

if %errorlevel% neq 0 (
    echo 构建失败！
    pause
    exit /b 1
)

echo 3. 构建Docker镜像...
docker build -t zhibo-frontend .

if %errorlevel% neq 0 (
    echo Docker镜像构建失败！
    pause
    exit /b 1
)

echo ========================================
echo 构建完成
echo ========================================
pause

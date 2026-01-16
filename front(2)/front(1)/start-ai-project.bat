@echo off
echo ========================================
echo 智能教学平台 - AI功能启动脚本
echo ========================================
echo.

echo 正在检查Node.js环境...
node --version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误：未检测到Node.js，请先安装Node.js
    pause
    exit /b 1
)

echo Node.js环境检查通过
echo.

echo 正在安装项目依赖...
npm install
if %errorlevel% neq 0 (
    echo 错误：依赖安装失败
    pause
    exit /b 1
)

echo 依赖安装完成
echo.

echo 正在启动开发服务器...
echo 请确保后端服务已启动在 http://localhost:8080
echo.
echo 前端服务将启动在 http://localhost:5173
echo.

npm run dev

pause 
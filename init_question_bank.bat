@echo off
chcp 65001
echo ========================================
echo 智能出题系统 - 数据库初始化
echo ========================================
echo.

echo 请确保MySQL服务已启动！
echo.

set /p DB_HOST="请输入数据库地址 (默认: localhost): "
if "%DB_HOST%"=="" set DB_HOST=localhost

set /p DB_PORT="请输入数据库端口 (默认: 3306): "
if "%DB_PORT%"=="" set DB_PORT=3306

set /p DB_NAME="请输入数据库名称 (默认: smart_teaching): "
if "%DB_NAME%"=="" set DB_NAME=smart_teaching

set /p DB_USER="请输入数据库用户名 (默认: root): "
if "%DB_USER%"=="" set DB_USER=root

set /p DB_PASS="请输入数据库密码: "

echo.
echo ========================================
echo 开始初始化题库表...
echo ========================================
echo.

mysql -h %DB_HOST% -P %DB_PORT% -u %DB_USER% -p%DB_PASS% %DB_NAME% < question_bank_table.sql

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo ✅ 题库表初始化成功！
    echo ========================================
    echo.
    echo 已创建以下内容：
    echo - question_bank 表（题库缓存表）
    echo - 5条示例题目
    echo.
    echo 下一步：
    echo 1. 重启后端服务
    echo 2. 使用智能出题功能
    echo 3. 首次会调用AI生成题目并缓存
    echo 4. 后续相同类型题目将直接从题库获取（速度提升100倍+）
    echo.
) else (
    echo.
    echo ========================================
    echo ❌ 初始化失败！
    echo ========================================
    echo.
    echo 请检查：
    echo 1. MySQL服务是否启动
    echo 2. 数据库连接信息是否正确
    echo 3. 用户是否有足够权限
    echo.
)

pause


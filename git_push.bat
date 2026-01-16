@echo off
chcp 65001
cd /d "D:\Desktop\计算机设计大赛\CSdesign\Webstorm-experiment"

echo ===== 检查Git状态 =====
git status

echo.
echo ===== 添加所有修改的文件 =====
git add .

echo.
echo ===== 提交更改 =====
git commit -m "优化练习评测页面：添加数据持久化功能和分析报告提示"

echo.
echo ===== 推送到GitHub =====
git push

echo.
echo ===== 完成 =====
pause


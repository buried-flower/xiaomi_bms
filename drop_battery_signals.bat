@echo off
echo 正在删除battery_signals表并更新battery_warning表...

rem 设置MySQL连接信息 - 请根据实际情况修改
set MYSQL_USER=root
set MYSQL_PASSWORD=password
set MYSQL_HOST=localhost
set MYSQL_PORT=3306

rem 执行SQL脚本
mysql -u%MYSQL_USER% -p%MYSQL_PASSWORD% -h%MYSQL_HOST% -P%MYSQL_PORT% < src\main\resources\drop_battery_signals.sql

IF %ERRORLEVEL% NEQ 0 (
    echo 数据库更新失败！请检查MySQL连接信息和权限。
) ELSE (
    echo 数据库更新成功！battery_signals表已删除，battery_warning表已更新。
)

pause 
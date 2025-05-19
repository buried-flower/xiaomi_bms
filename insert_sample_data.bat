@echo off
echo 正在插入示例数据...

rem 设置MySQL连接信息 - 请根据实际情况修改
set MYSQL_USER=root
set MYSQL_PASSWORD=password
set MYSQL_HOST=localhost
set MYSQL_PORT=3306

rem 执行SQL脚本
mysql -u%MYSQL_USER% -p%MYSQL_PASSWORD% -h%MYSQL_HOST% -P%MYSQL_PORT% < src\main\resources\sample_data.sql

IF %ERRORLEVEL% NEQ 0 (
    echo 数据插入失败！请检查MySQL连接信息和权限。
) ELSE (
    echo 示例数据插入成功！
)

pause 
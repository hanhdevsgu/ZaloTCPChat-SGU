@echo off
chcp 65001 >nul
echo ===================================================
echo   [BÀI 12] KHỞI ĐỘNG HTTP SERVER (PORT 8080)
echo ===================================================
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-25.0.4.7-hotspot
cd /d "%~dp0server"
call java -Dfile.encoding=UTF-8 -cp target\classes chat.server.HttpChatServer
pause

@echo off
chcp 65001 >nul
echo ===================================================
echo   [BÀI 11] KHỞI ĐỘNG TCP CHAT SERVER (PORT 5000)
echo ===================================================
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-25.0.4.7-hotspot
cd /d "%~dp0server"
call java -Dfile.encoding=UTF-8 -cp target\classes chat.server.TcpChatServer
pause

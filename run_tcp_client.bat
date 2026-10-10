@echo off
chcp 65001 >nul
echo ===================================================
echo   [BÀI 11] CHẠY TCP CHAT CLIENT (NHIỀU CLIENT)
echo ===================================================
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-25.0.4.7-hotspot
cd /d "%~dp0server"
call java -Dfile.encoding=UTF-8 -cp target\classes chat.client.TcpChatClient %1 %2
pause

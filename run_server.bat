@echo off
chcp 65001 >nul
echo ===================================================
echo   KHOI DONG HTTP SERVER CHO BAI 12
echo ===================================================
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-25.0.4.7-hotspot
cd /d "%~dp0server"
call mvn compile
call mvn exec:java -Dexec.mainClass="chat.server.HttpChatServer"
pause

@echo off
REM Example run script for Windows
if "%1"=="" (
  echo Usage: run.bat listenPort [connectHost connectPort]
  echo Example: run.bat 5000
  goto :eof
)
set LISTEN=%1
javac WhiteboardPeer.java
start java WhiteboardPeer %LISTEN%
if NOT "%2"=="" (
  echo Waiting two seconds then opening second peer connection...
  timeout /t 2 >nul
  start java WhiteboardPeer %2%
)

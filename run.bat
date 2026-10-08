@echo off
setlocal enabledelayedexpansion
title School360 Application Launcher
echo ===================================================
echo             Starting School360 Application
echo ===================================================

cd /d "%~dp0"

if exist "%USERPROFILE%\.jdks\ms-17.0.20\bin\java.exe" (
    set "JAVA_BIN=%USERPROFILE%\.jdks\ms-17.0.20\bin\java.exe"
) else (
    set "JAVA_BIN=java"
)

if not exist "cp.txt" (
    echo Error: cp.txt not found.
    pause
    exit /b 1
)

set /p CP=<cp.txt

echo Using Java: !JAVA_BIN!
echo Starting Spring Boot...
"!JAVA_BIN!" -Xmx768m -Xms256m -cp "target\classes;!CP!" com.school360.School360Application %*

pause
endlocal

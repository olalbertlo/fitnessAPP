@echo off
setlocal enabledelayedexpansion

REM Check if Java is installed
java -version >nul 2>&1
if errorlevel 1 (
    echo Error: Java is not installed or not found in PATH
    echo Please install Java 17 or later from https://adoptium.net/
    pause
    exit /b 1
)

REM Run the application with JavaFX modules
java -jar fitness-app-1.0-SNAPSHOT.jar

if errorlevel 1 (
    echo.
    echo Application failed to start. Please make sure you have Java 17 or later installed.
    echo If the problem persists, please contact support.
)

pause 
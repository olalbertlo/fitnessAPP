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

REM Set the JAR file path
set JAR_FILE=target\fitness-app-1.0-SNAPSHOT.jar

REM Check if the JAR file exists
if not exist "%JAR_FILE%" (
    echo Error: Could not find %JAR_FILE%
    echo Please make sure you have built the project using 'mvn clean package'
    pause
    exit /b 1
)

REM Run the application
java --add-opens javafx.graphics/javafx.scene=ALL-UNNAMED ^
     --add-opens javafx.controls/javafx.scene.control=ALL-UNNAMED ^
     --add-opens javafx.controls/javafx.scene.control.skin=ALL-UNNAMED ^
     -jar "%JAR_FILE%"

if errorlevel 1 (
    echo.
    echo Application failed to start. Please make sure you have Java 17 or later installed.
    echo If the problem persists, please contact support.
)

pause 
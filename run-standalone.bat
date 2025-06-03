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

REM Set paths
set JAR_FILE=%~dp0target\fitness-app-1.0-SNAPSHOT.jar

REM Check if JavaFX SDK environment variable is set
if not defined JAVAFX_HOME (
    echo Error: JAVAFX_HOME environment variable is not set.
    echo Please download JavaFX SDK from https://gluonhq.com/products/javafx/
    echo Extract it and set JAVAFX_HOME to point to the extracted directory.
    echo Example: set JAVAFX_HOME=C:\Path\To\javafx-sdk-21.0.2
    pause
    exit /b 1
)

REM Build module path
set MODULE_PATH=%JAR_FILE%;%JAVAFX_HOME%\lib

REM Run the application
java --module-path "%MODULE_PATH%" ^
     --add-modules javafx.controls,javafx.fxml,javafx.graphics ^
     --add-opens javafx.graphics/javafx.scene=ALL-UNNAMED ^
     --add-opens javafx.controls/javafx.scene.control=ALL-UNNAMED ^
     --add-opens javafx.controls/javafx.scene.control.skin=ALL-UNNAMED ^
     -jar "%JAR_FILE%"

if errorlevel 1 (
    echo.
    echo Application failed to start. Make sure:
    echo 1. You have Java 17 or later installed
    echo 2. JAVAFX_HOME points to a valid JavaFX SDK directory
    echo 3. The JAR file exists in the target directory
)

pause 
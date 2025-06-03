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

REM Get the path to the JAR file
set JAR_FILE=%~dp0target\fitness-app-1.0-SNAPSHOT.jar

REM Get Maven repository path
for /f "tokens=*" %%a in ('mvn help:evaluate -Dexpression=settings.localRepository -q -DforceStdout') do set MVN_REPO=%%a

REM Set JavaFX paths
set JAVAFX_VERSION=21.0.2
set JAVAFX_PATH=%MVN_REPO%\org\openjfx

REM Build the module path with all required JavaFX JARs
set MODULE_PATH=%JAR_FILE%;^
%JAVAFX_PATH%\javafx-controls\%JAVAFX_VERSION%\javafx-controls-%JAVAFX_VERSION%.jar;^
%JAVAFX_PATH%\javafx-controls\%JAVAFX_VERSION%\javafx-controls-%JAVAFX_VERSION%-win.jar;^
%JAVAFX_PATH%\javafx-graphics\%JAVAFX_VERSION%\javafx-graphics-%JAVAFX_VERSION%.jar;^
%JAVAFX_PATH%\javafx-graphics\%JAVAFX_VERSION%\javafx-graphics-%JAVAFX_VERSION%-win.jar;^
%JAVAFX_PATH%\javafx-base\%JAVAFX_VERSION%\javafx-base-%JAVAFX_VERSION%.jar;^
%JAVAFX_PATH%\javafx-base\%JAVAFX_VERSION%\javafx-base-%JAVAFX_VERSION%-win.jar;^
%JAVAFX_PATH%\javafx-fxml\%JAVAFX_VERSION%\javafx-fxml-%JAVAFX_VERSION%.jar;^
%JAVAFX_PATH%\javafx-fxml\%JAVAFX_VERSION%\javafx-fxml-%JAVAFX_VERSION%-win.jar

REM Run the application with JavaFX modules
java --module-path "%MODULE_PATH%" ^
     --add-modules javafx.controls,javafx.fxml,javafx.graphics ^
     --add-opens javafx.graphics/javafx.scene=ALL-UNNAMED ^
     --add-opens javafx.controls/javafx.scene.control=ALL-UNNAMED ^
     --add-opens javafx.controls/javafx.scene.control.skin=ALL-UNNAMED ^
     -jar "%JAR_FILE%"

if errorlevel 1 (
    echo.
    echo Application failed to start. Please make sure you have Java 17 or later installed.
    echo If the problem persists, please contact support.
)

pause 
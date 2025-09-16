@echo off
echo ========================================
echo Maintainer App - Quick Build Script
echo ========================================

REM Set Java Home to Android Studio's bundled JDK
set JAVA_HOME=C:\Program Files\Android\Android Studio\jbr
set PATH=%JAVA_HOME%\bin;%PATH%

REM Set Android SDK paths
set ANDROID_HOME=%USERPROFILE%\AppData\Local\Android\Sdk
set PATH=%ANDROID_HOME%\platform-tools;%ANDROID_HOME%\build-tools\34.0.0;%PATH%

echo Using Java: %JAVA_HOME%
"%JAVA_HOME%\bin\java.exe" -version

echo Checking Android SDK...
if not exist "%ANDROID_HOME%" (
    echo ERROR: Android SDK not found at %ANDROID_HOME%
    pause
    exit /b 1
)

echo.
echo ========================================
echo Building Maintainer App...
echo ========================================

REM Stop any existing Gradle daemons
echo Stopping Gradle daemons...
.\gradlew.bat --stop

REM Clean and build
echo Building debug APK...
.\gradlew.bat clean assembleDebug

if %ERRORLEVEL% neq 0 (
    echo.
    echo ========================================
    echo BUILD FAILED!
    echo ========================================
    pause
    exit /b 1
)

echo.
echo ========================================
echo BUILD SUCCESSFUL!
echo ========================================
echo APK Location: app\build\outputs\apk\debug\app-debug.apk

echo.
echo Checking emulator connection...
adb devices

echo.
echo To install: adb install app\build\outputs\apk\debug\app-debug.apk
echo To launch: adb shell am start -n com.maintainer.app/.MainActivity

pause
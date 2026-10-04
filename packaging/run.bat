@echo off
rem VLC Skin Studio launcher for Windows.
rem
rem Runs the jar next to this file and passes every argument through. When Java
rem is missing it prints the Azul Zulu JRE download for this machine instead of
rem a Windows error. The download link matches the AZUL constant in the Java 8
rem entry point, src\launcher\java\dev\zoroaster1x\vlcskin\launch\Launcher.java.
setlocal EnableExtensions EnableDelayedExpansion

set "DIR=%~dp0"
set "JAR=%DIR%vlc-skin-studio.jar"

set "ARCH=x86-64-bit"
if /i "%PROCESSOR_ARCHITECTURE%"=="ARM64" set "ARCH=arm-64-bit"
set "AZUL=https://www.azul.com/downloads/?version=java-25-lts&package=jre&os=windows&architecture=!ARCH!#zulu"

set "JAVA="
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" set "JAVA=%JAVA_HOME%\bin\java.exe"
if not defined JAVA for /f "delims=" %%j in ('where java 2^>nul') do if not defined JAVA set "JAVA=%%j"

if not defined JAVA goto nojava
"%JAVA%" -version >nul 2>&1
if errorlevel 1 goto nojava

if not exist "%JAR%" (
    echo vlc-skin-studio.jar is missing next to this script.
    echo Unzip the whole release package and run run.bat from that folder.
    pause
    exit /b 1
)

"%JAVA%" -jar "%JAR%" %*
set "CODE=%ERRORLEVEL%"
if not "%CODE%"=="0" pause
exit /b %CODE%

:nojava
echo JAVA NOT INSTALLED. Please download from:
echo   !AZUL!
echo.
echo Java 25 (Azul Zulu JRE) for Windows:
echo   x64:    download the .msi, run the installer, then run run.bat again.
echo   ARM64:  download the ARM 64-bit .zip, unpack it, then set JAVA_HOME to
echo           the unpacked folder and run run.bat again.
echo.
echo If Java is already installed but not on your PATH, set JAVA_HOME to its
echo folder, or add its bin folder to PATH, and run this file again.
pause
exit /b 1

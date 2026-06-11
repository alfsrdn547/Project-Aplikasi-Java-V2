@echo off
setlocal
set "PROJECT_DIR=%~dp0"
cd /d "%PROJECT_DIR%"
set "LIB_JAR=lib\mysql-connector-j-9.6.0.jar"
set "SRC_DIR=src"
set "CLASSES_DIR=classes"
set "JAR_NAME=AplikasiKeuangan.jar"
set "JAR_CMD=jar"

if exist "%CLASSES_DIR%" rd /s /q "%CLASSES_DIR%"
mkdir "%CLASSES_DIR%"

where jar >nul 2>nul
if errorlevel 1 (
    if defined JAVA_HOME if exist "%JAVA_HOME%\bin\jar.exe" set "JAR_CMD=%JAVA_HOME%\bin\jar.exe"
    if "%JAR_CMD%"=="jar" (
        for %%I in ("%ProgramFiles%\Java\jdk*\bin\jar.exe" "%ProgramFiles(x86)%\Java\jdk*\bin\jar.exe") do (
            if exist %%~I set "JAR_CMD=%%~I"
        )
    )
)

javac -d "%CLASSES_DIR%" -cp "%LIB_JAR%" "%SRC_DIR%\*.java"
if errorlevel 1 goto fail
(
  echo Manifest-Version: 1.0
  echo Main-Class: LoginFrame
  echo Class-Path: lib/mysql-connector-j-9.6.0.jar
) > manifest.mf
if exist "%JAR_NAME%" del /f /q "%JAR_NAME%"
"%JAR_CMD%" --create --file "%JAR_NAME%" --manifest manifest.mf -C "%CLASSES_DIR%" .
if errorlevel 1 goto fail
 del /f /q manifest.mf
 echo Build selesai: %JAR_NAME%
 echo Jalankan dengan: java -jar %JAR_NAME%
 goto end
:fail
 echo Build gagal.
 exit /b 1
:end
endlocal

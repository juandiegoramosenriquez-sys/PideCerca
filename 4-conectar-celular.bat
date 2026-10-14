@echo off
title PideCerca - Conectar celular por cable
set "ADB=%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe"
if not exist "%ADB%" (
  echo No se encontro adb. Instala Android Studio y el Android SDK.
  pause
  exit /b
)
"%ADB%" devices
"%ADB%" reverse tcp:8080 tcp:8080
if errorlevel 1 (
  echo.
  echo No se pudo conectar. Revisa el cable y acepta "Permitir depuracion USB" en el celular.
) else (
  echo.
  echo Listo. El celular ya puede usar el backend de tu PC.
)
pause

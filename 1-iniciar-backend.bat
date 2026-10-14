@echo off
title PideCerca - Backend
cd /d "%~dp0backend"
if not defined JAVA_HOME if exist "%USERPROFILE%\.jdks\ms-21.0.12.1" set "JAVA_HOME=%USERPROFILE%\.jdks\ms-21.0.12.1"
if not exist target\tmp mkdir target\tmp
call mvnw.cmd spring-boot:run "-Dspring-boot.run.jvmArguments=-Djdk.net.unixdomain.tmpdir=%~dp0backend\target\tmp"
pause

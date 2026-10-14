@echo off
title PideCerca - Web del negocio
cd /d "%~dp0web\negocio"
if not exist node_modules call npm install
call npm run dev
pause

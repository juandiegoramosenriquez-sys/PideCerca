@echo off
title PideCerca - Panel admin
cd /d "%~dp0web\admin"
if not exist .venv python -m venv .venv
call .venv\Scripts\python -m pip install -q -r requirements.txt
.venv\Scripts\python manage.py runserver 8001
pause

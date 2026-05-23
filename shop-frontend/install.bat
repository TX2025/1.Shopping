@echo off
set PATH=E:\52.Node;%PATH%
cd /d "E:\53.Project_AI\1、Shop\shop-frontend"
rmdir /s /q node_modules 2>nul
del package-lock.json 2>nul
call npm install
echo DONE

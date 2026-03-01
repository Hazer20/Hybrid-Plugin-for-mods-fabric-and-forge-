@echo off
setlocal EnableExtensions EnableDelayedExpansion

REM =============================================================
REM Minecraft Pack Updater Launcher (Windows)
REM Checks datapack/resourcepack folders and runs full conversion.
REM =============================================================

set "SCRIPT_DIR=%~dp0"
set "PY_SCRIPT=%SCRIPT_DIR%update_mc_pack.py"

if not exist "%PY_SCRIPT%" (
    echo [ERROR] Не найден файл update_mc_pack.py рядом с батником:
    echo         %PY_SCRIPT%
    exit /b 1
)

where python >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Python не найден в PATH. Установите Python 3.10+ и добавьте в PATH.
    exit /b 1
)

echo.
echo ===== Minecraft Datapack/Resourcepack Updater =====
echo Этот скрипт проверит папки и запустит полное переписывание в новую структуру.
echo.

set /p DATAPACK_PATH=Введите путь к старому датапаку (папка): 
if "%DATAPACK_PATH%"=="" (
    echo [ERROR] Путь к датапаку не введён.
    exit /b 1
)

set /p RESOURCEPACK_PATH=Введите путь к старому ресурс-паку (папка): 
if "%RESOURCEPACK_PATH%"=="" (
    echo [ERROR] Путь к ресурс-паку не введён.
    exit /b 1
)

set /p OUTPUT_PATH=Введите путь к выходной папке проекта (например C:\mc_update_out): 
if "%OUTPUT_PATH%"=="" (
    echo [ERROR] Выходная папка не введена.
    exit /b 1
)

if not exist "%DATAPACK_PATH%" (
    echo [ERROR] Папка датапака не существует: "%DATAPACK_PATH%"
    exit /b 1
)

if not exist "%RESOURCEPACK_PATH%" (
    echo [ERROR] Папка ресурс-пака не существует: "%RESOURCEPACK_PATH%"
    exit /b 1
)

if not exist "%DATAPACK_PATH%\pack.mcmeta" (
    echo [WARNING] В папке датапака нет pack.mcmeta: "%DATAPACK_PATH%"
    echo           Скрипт продолжит работу, но проверьте корректность структуры.
)

if not exist "%DATAPACK_PATH%\data" (
    echo [WARNING] В папке датапака нет папки data: "%DATAPACK_PATH%"
    echo           Скрипт продолжит работу, но возможно структура неверная.
)

if not exist "%RESOURCEPACK_PATH%\pack.mcmeta" (
    echo [WARNING] В папке ресурс-пака нет pack.mcmeta: "%RESOURCEPACK_PATH%"
    echo           Скрипт продолжит работу, но проверьте корректность структуры.
)

if not exist "%RESOURCEPACK_PATH%\assets" (
    echo [WARNING] В папке ресурс-пака нет папки assets: "%RESOURCEPACK_PATH%"
    echo           Скрипт продолжит работу, но возможно структура неверная.
)

echo.
echo [INFO] Запуск конвертации...
echo [INFO] Команда:
echo python "%PY_SCRIPT%" --datapack "%DATAPACK_PATH%" --resourcepack "%RESOURCEPACK_PATH%" --output "%OUTPUT_PATH%" --backup --log
echo.

python "%PY_SCRIPT%" --datapack "%DATAPACK_PATH%" --resourcepack "%RESOURCEPACK_PATH%" --output "%OUTPUT_PATH%" --backup --log
set "EXIT_CODE=%ERRORLEVEL%"

echo.
if "%EXIT_CODE%"=="0" (
    echo [OK] Конвертация успешно завершена.
    echo [OK] Результат находится в: "%OUTPUT_PATH%"
) else (
    echo [ERROR] Конвертация завершилась с кодом: %EXIT_CODE%
)

echo.
pause
exit /b %EXIT_CODE%

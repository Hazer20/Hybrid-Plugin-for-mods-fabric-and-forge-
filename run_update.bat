@echo off
setlocal EnableExtensions EnableDelayedExpansion

REM ==================================================================
REM Minecraft Pack Updater Launcher (Windows)
REM 1) On first run creates helper folders where user should place packs.
REM 2) Validates datapack/resourcepack folders.
REM 3) Runs full conversion and writes readable progress log.
REM ==================================================================

set "SCRIPT_DIR=%~dp0"
set "PY_SCRIPT=%SCRIPT_DIR%update_mc_pack.py"
set "INPUT_ROOT=%SCRIPT_DIR%packs_input"
set "DEFAULT_DP=%INPUT_ROOT%\old_datapack"
set "DEFAULT_RP=%INPUT_ROOT%\old_resourcepack"
set "DEFAULT_OUTPUT=%SCRIPT_DIR%mc_output"
set "LOG_DIR=%SCRIPT_DIR%logs"
set "BAT_LOG=%LOG_DIR%\bat_progress.log"

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

if not exist "%INPUT_ROOT%" mkdir "%INPUT_ROOT%"
if not exist "%DEFAULT_DP%" mkdir "%DEFAULT_DP%"
if not exist "%DEFAULT_RP%" mkdir "%DEFAULT_RP%"
if not exist "%DEFAULT_OUTPUT%" mkdir "%DEFAULT_OUTPUT%"
if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"

call :log "============================================================"
call :log "Запуск run_update.bat"
call :log "Базовые папки готовы:"
call :log "  DATAPACK: %DEFAULT_DP%"
call :log "  RESOURCEPACK: %DEFAULT_RP%"
call :log "  OUTPUT: %DEFAULT_OUTPUT%"

REM First-run helper: if both folders are empty, ask user to place packs and exit.
dir /b "%DEFAULT_DP%" >nul 2>&1
set "DP_EMPTY=%ERRORLEVEL%"
dir /b "%DEFAULT_RP%" >nul 2>&1
set "RP_EMPTY=%ERRORLEVEL%"

if "%DP_EMPTY%"=="1" if "%RP_EMPTY%"=="1" (
    call :log "Первый запуск: созданы папки для входных паков."
    call :log "Скопируйте ваш ДАТАПАК в: %DEFAULT_DP%"
    call :log "Скопируйте ваш РЕСУРС-ПАК в: %DEFAULT_RP%"
    call :log "После этого запустите run_update.bat еще раз."
    echo.
    echo [INFO] Первый запуск завершен: созданы папки для входных паков.
    echo [INFO] Положите файлы в:
    echo        %DEFAULT_DP%
    echo        %DEFAULT_RP%
    echo [INFO] Затем запустите батник повторно.
    echo.
    pause
    exit /b 0
)

echo.
echo ===== Minecraft Datapack/Resourcepack Updater =====
echo По умолчанию используются папки:
echo   Datapack:      %DEFAULT_DP%
echo   Resourcepack:  %DEFAULT_RP%
echo   Output:        %DEFAULT_OUTPUT%
echo.

set /p DATAPACK_PATH=Введите путь к датапаку (Enter = по умолчанию): 
if "%DATAPACK_PATH%"=="" set "DATAPACK_PATH=%DEFAULT_DP%"

set /p RESOURCEPACK_PATH=Введите путь к ресурс-паку (Enter = по умолчанию): 
if "%RESOURCEPACK_PATH%"=="" set "RESOURCEPACK_PATH=%DEFAULT_RP%"

set /p OUTPUT_PATH=Введите выходную папку (Enter = по умолчанию): 
if "%OUTPUT_PATH%"=="" set "OUTPUT_PATH=%DEFAULT_OUTPUT%"

call :log "Выбраны пути:"
call :log "  DATAPACK: %DATAPACK_PATH%"
call :log "  RESOURCEPACK: %RESOURCEPACK_PATH%"
call :log "  OUTPUT: %OUTPUT_PATH%"

if not exist "%DATAPACK_PATH%" (
    call :log "[ERROR] Папка датапака не существует: %DATAPACK_PATH%"
    echo [ERROR] Папка датапака не существует: "%DATAPACK_PATH%"
    pause
    exit /b 1
)

if not exist "%RESOURCEPACK_PATH%" (
    call :log "[ERROR] Папка ресурс-пака не существует: %RESOURCEPACK_PATH%"
    echo [ERROR] Папка ресурс-пака не существует: "%RESOURCEPACK_PATH%"
    pause
    exit /b 1
)

if not exist "%DATAPACK_PATH%\pack.mcmeta" (
    call :log "[WARNING] В датапаке нет pack.mcmeta: %DATAPACK_PATH%"
    echo [WARNING] В папке датапака нет pack.mcmeta: "%DATAPACK_PATH%"
)

if not exist "%DATAPACK_PATH%\data" (
    call :log "[WARNING] В датапаке нет data: %DATAPACK_PATH%"
    echo [WARNING] В папке датапака нет папки data: "%DATAPACK_PATH%"
)

if not exist "%RESOURCEPACK_PATH%\pack.mcmeta" (
    call :log "[WARNING] В ресурс-паке нет pack.mcmeta: %RESOURCEPACK_PATH%"
    echo [WARNING] В папке ресурс-пака нет pack.mcmeta: "%RESOURCEPACK_PATH%"
)

if not exist "%RESOURCEPACK_PATH%\assets" (
    call :log "[WARNING] В ресурс-паке нет assets: %RESOURCEPACK_PATH%"
    echo [WARNING] В папке ресурс-пака нет папки assets: "%RESOURCEPACK_PATH%"
)

echo.
echo [STEP 1/4] Проверка путей завершена.
call :log "[STEP 1/4] Проверка путей завершена"

if not exist "%OUTPUT_PATH%" mkdir "%OUTPUT_PATH%"
echo [STEP 2/4] Выходная папка готова: %OUTPUT_PATH%
call :log "[STEP 2/4] Выходная папка готова: %OUTPUT_PATH%"

echo [STEP 3/4] Запуск Python-конвертера...
call :log "[STEP 3/4] Запуск Python-конвертера"
call :log "Команда: python \"%PY_SCRIPT%\" --datapack \"%DATAPACK_PATH%\" --resourcepack \"%RESOURCEPACK_PATH%\" --output \"%OUTPUT_PATH%\" --backup --log"

python "%PY_SCRIPT%" --datapack "%DATAPACK_PATH%" --resourcepack "%RESOURCEPACK_PATH%" --output "%OUTPUT_PATH%" --backup --log
set "EXIT_CODE=%ERRORLEVEL%"

if "%EXIT_CODE%"=="0" (
    echo [STEP 4/4] Готово: конвертация успешно завершена.
    echo [OK] Результат: "%OUTPUT_PATH%"
    echo [OK] Логи Python: "%OUTPUT_PATH%\logs\changes.log" и "%OUTPUT_PATH%\logs\errors.log"
    echo [OK] Лог батника: "%BAT_LOG%"
    call :log "[STEP 4/4] Успешно. Результат: %OUTPUT_PATH%"
) else (
    echo [STEP 4/4] Ошибка выполнения. Код: %EXIT_CODE%
    echo [ERROR] Проверьте логи:
    echo         "%OUTPUT_PATH%\logs\errors.log"
    echo         "%BAT_LOG%"
    call :log "[STEP 4/4] Ошибка. Код: %EXIT_CODE%"
)

echo.
pause
exit /b %EXIT_CODE%

:log
echo [%date% %time%] %~1
echo [%date% %time%] %~1>>"%BAT_LOG%"
exit /b 0

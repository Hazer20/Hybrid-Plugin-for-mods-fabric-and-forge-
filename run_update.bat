@echo off
setlocal EnableExtensions EnableDelayedExpansion
chcp 65001 >nul

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
    echo [ERROR] update_mc_pack.py not found near BAT file:
    echo         %PY_SCRIPT%
    exit /b 1
)

where python >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Python not found in PATH. Install Python 3.10+ and add it to PATH.
    exit /b 1
)

if not exist "%INPUT_ROOT%" mkdir "%INPUT_ROOT%"
if not exist "%DEFAULT_DP%" mkdir "%DEFAULT_DP%"
if not exist "%DEFAULT_RP%" mkdir "%DEFAULT_RP%"
if not exist "%DEFAULT_OUTPUT%" mkdir "%DEFAULT_OUTPUT%"
if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"

call :log "============================================================"
call :log "run_update.bat started"
call :log "Base folders are ready:"
call :log "  DATAPACK: %DEFAULT_DP%"
call :log "  RESOURCEPACK: %DEFAULT_RP%"
call :log "  OUTPUT: %DEFAULT_OUTPUT%"

REM First-run helper: if both folders are empty, ask user to place packs and exit.
dir /b "%DEFAULT_DP%" >nul 2>&1
set "DP_EMPTY=%ERRORLEVEL%"
dir /b "%DEFAULT_RP%" >nul 2>&1
set "RP_EMPTY=%ERRORLEVEL%"

if "%DP_EMPTY%"=="1" if "%RP_EMPTY%"=="1" (
    call :log "First run detected: input folders created."
    call :log "Copy your DATAPACK to: %DEFAULT_DP%"
    call :log "Copy your RESOURCEPACK to: %DEFAULT_RP%"
    call :log "Then run run_update.bat again."
    echo.
    echo [INFO] First run completed: input folders created.
    echo [INFO] Put your files here:
    echo        %DEFAULT_DP%
    echo        %DEFAULT_RP%
    echo [INFO] Then run BAT again.
    echo.
    pause
    exit /b 0
)

echo.
echo ===== Minecraft Datapack/Resourcepack Updater =====
echo Default folders:
echo   Datapack:      %DEFAULT_DP%
echo   Resourcepack:  %DEFAULT_RP%
echo   Output:        %DEFAULT_OUTPUT%
echo.

set /p DATAPACK_PATH=Enter datapack path or .zip file (Enter = default): 
if "%DATAPACK_PATH%"=="" set "DATAPACK_PATH=%DEFAULT_DP%"

set /p RESOURCEPACK_PATH=Enter resourcepack path or .zip file (Enter = default): 
if "%RESOURCEPACK_PATH%"=="" set "RESOURCEPACK_PATH=%DEFAULT_RP%"

set /p OUTPUT_PATH=Enter output folder (Enter = default): 
if "%OUTPUT_PATH%"=="" set "OUTPUT_PATH=%DEFAULT_OUTPUT%"

call :log "Selected paths:"
call :log "  DATAPACK: %DATAPACK_PATH%"
call :log "  RESOURCEPACK: %RESOURCEPACK_PATH%"
call :log "  OUTPUT: %OUTPUT_PATH%"

if not exist "%DATAPACK_PATH%" (
    call :log "[ERROR] Datapack folder does not exist: %DATAPACK_PATH%"
    echo [ERROR] Datapack folder does not exist: "%DATAPACK_PATH%"
    pause
    exit /b 1
)

if not exist "%RESOURCEPACK_PATH%" (
    call :log "[ERROR] Resourcepack folder does not exist: %RESOURCEPACK_PATH%"
    echo [ERROR] Resourcepack folder does not exist: "%RESOURCEPACK_PATH%"
    pause
    exit /b 1
)

set "DP_IS_ZIP=0"
set "RP_IS_ZIP=0"
if /I "%DATAPACK_PATH:~-4%"==".zip" set "DP_IS_ZIP=1"
if /I "%RESOURCEPACK_PATH:~-4%"==".zip" set "RP_IS_ZIP=1"

if "%DP_IS_ZIP%"=="1" (
    call :log "Datapack is provided as ZIP: %DATAPACK_PATH%"
) else (
    if not exist "%DATAPACK_PATH%\pack.mcmeta" (
        call :log "[WARNING] pack.mcmeta is missing in datapack: %DATAPACK_PATH%"
        echo [WARNING] pack.mcmeta is missing in datapack: "%DATAPACK_PATH%"
    )
    if not exist "%DATAPACK_PATH%\data" (
        call :log "[WARNING] data folder is missing in datapack: %DATAPACK_PATH%"
        echo [WARNING] data folder is missing in datapack: "%DATAPACK_PATH%"
    )
)

if "%RP_IS_ZIP%"=="1" (
    call :log "Resourcepack is provided as ZIP: %RESOURCEPACK_PATH%"
) else (
    if not exist "%RESOURCEPACK_PATH%\pack.mcmeta" (
        call :log "[WARNING] pack.mcmeta is missing in resourcepack: %RESOURCEPACK_PATH%"
        echo [WARNING] pack.mcmeta is missing in resourcepack: "%RESOURCEPACK_PATH%"
    )
    if not exist "%RESOURCEPACK_PATH%\assets" (
        call :log "[WARNING] assets folder is missing in resourcepack: %RESOURCEPACK_PATH%"
        echo [WARNING] assets folder is missing in resourcepack: "%RESOURCEPACK_PATH%"
    )
)

echo.
echo [STEP 1/4] Path validation completed.
call :log "[STEP 1/4] Path validation completed"

if not exist "%OUTPUT_PATH%" mkdir "%OUTPUT_PATH%"
echo [STEP 2/4] Output folder is ready: %OUTPUT_PATH%
call :log "[STEP 2/4] Output folder is ready: %OUTPUT_PATH%"

echo [STEP 3/4] Running Python converter...
call :log "[STEP 3/4] Running Python converter"
call :log "Command: python \"%PY_SCRIPT%\" --datapack \"%DATAPACK_PATH%\" --resourcepack \"%RESOURCEPACK_PATH%\" --output \"%OUTPUT_PATH%\" --backup --log --ai-assist --ai-min-score 90"

python "%PY_SCRIPT%" --datapack "%DATAPACK_PATH%" --resourcepack "%RESOURCEPACK_PATH%" --output "%OUTPUT_PATH%" --backup --log --ai-assist --ai-min-score 90
set "EXIT_CODE=%ERRORLEVEL%"

if "%EXIT_CODE%"=="0" (
    echo [STEP 4/4] Done: conversion completed successfully.
    echo [OK] Result: "%OUTPUT_PATH%"
    echo [OK] Python logs: "%OUTPUT_PATH%\logs\changes.log" and "%OUTPUT_PATH%\logs\errors.log"
    echo [OK] AI report: "%OUTPUT_PATH%\logs\ai_assurance_report.txt"
    echo [OK] BAT log: "%BAT_LOG%"
    call :log "[STEP 4/4] Success. Result: %OUTPUT_PATH%"
) else (
    echo [STEP 4/4] Execution failed. Exit code: %EXIT_CODE%
    echo [ERROR] Check logs:
    echo         "%OUTPUT_PATH%\logs\errors.log"
    echo         "%BAT_LOG%"
    call :log "[STEP 4/4] Failed. Exit code: %EXIT_CODE%"
)

echo.
pause
exit /b %EXIT_CODE%

:log
echo [%date% %time%] %~1
echo [%date% %time%] %~1>>"%BAT_LOG%"
exit /b 0

@echo off
setlocal enabledelayedexpansion
::
:: Botica Director - Windows Launcher & Updater
::

set "SCRIPT_DIR=%~dp0"
set "BOTICA_HOME=%SCRIPT_DIR%.botica"
set "LIB_DIR=%BOTICA_HOME%\lib"
set "JAR_FILE=%LIB_DIR%\botica-director.jar"
set "UPDATE_FILE=%LIB_DIR%\botica-director.jar.update"
set "LATEST_RELEASE_URL=https://api.github.com/repos/isa-group/botica/releases/latest"

set "UPDATE_EXIT_CODE=99"

:check_java_version
    set "JAVA_FOUND="
    set "JAVA_VERSION_NUMBER="
    for /f "tokens=*" %%a in ('where java 2^>nul') do (
        set "JAVA_FOUND=true"
        goto :found_java
    )
    if not defined JAVA_FOUND (
        echo Error: Java is not installed or not in your PATH.
        echo Please install Java 11 or higher and try again.
        exit /b 1
    )

:found_java
    for /f "tokens=2 delims==" %%i in ('java -version 2^>^&1 ^| findstr /r "version" ^| findstr "1[1-9]"') do (
        set "JAVA_VERSION_NUMBER=%%i"
    )
    if not defined JAVA_VERSION_NUMBER (
        echo Error: You need at least Java 11 to run Botica.
        echo Please install Java 11 or higher and try again.
        exit /b 1
    )
    exit /b 0

:download_director
    echo Botica Director not found. Downloading the latest version...
    set "LATEST_TAG="
    for /f "tokens=3 delims=: " %%i in ('curl -sL %LATEST_RELEASE_URL% ^| findstr /i "tag_name"') do (
        set "LATEST_TAG=%%i"
    )
    set "LATEST_TAG=!LATEST_TAG:,=!"
    set "LATEST_TAG=!LATEST_TAG:"=!"

    if "!LATEST_TAG!"=="" (
        echo Error: Could not determine the latest version from GitHub.
        echo Please check your internet connection and GitHub API status.
        exit /b 1
    )

    set "DOWNLOAD_URL=https://github.com/isa-group/botica/releases/download/!LATEST_TAG!/botica-director.jar"
    echo Downloading Botica Director version !LATEST_TAG!...

    if not exist "%LIB_DIR%" mkdir "%LIB_DIR%"
    curl -# -L -o "%JAR_FILE%" "!DOWNLOAD_URL!"
    if !errorlevel! neq 0 (
        echo Error: Failed to download Botica Director.
        echo Please check your internet connection.
        if exist "%JAR_FILE%" del "%JAR_FILE%"
        exit /b 1
    )
    echo Download complete.
    exit /b 0

call :check_java_version
if !errorlevel! neq 0 exit /b !errorlevel!

if not exist "%JAR_FILE%" (
    call :download_director
    if !errorlevel! neq 0 exit /b !errorlevel!
)

:main_loop
set "BOTICA_WRAPPER_ACTIVE=true"
java -jar "%JAR_FILE%" %*
set "EXIT_CODE=%ERRORLEVEL%"

if !EXIT_CODE! equ !UPDATE_EXIT_CODE! (
    if exist "%UPDATE_FILE%" (
        DEL /F /Q "%JAR_FILE%"
        REN "%UPDATE_FILE%" "%JAR_FILE%"
        goto :main_loop
    ) else (
        echo Error: Update requested, but the update file was not found (expected: %UPDATE_FILE%).
        exit /b 1
    )
) else (
    exit /b !EXIT_CODE!
)

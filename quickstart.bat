@echo off

rem Function to display help message
:show_help
echo Usage: %0 [OPTIONS]
echo.
echo Options:
echo     -h         Display this help message
echo     -b         Trigger build
echo     -a         Enable all processes
echo     -o         Enable ocaml process
echo     -j         Enable all java processes
echo     -d         Enable db-service
echo     -r         Enable model-generator
echo     -t         Enable all typescript processes.
echo     -e         Enable all backend processes.
echo     -l         Enable logging (disable detached mode)
goto :eof

rem Initialize variables
set BUILD=false
set OCAML=false
set SPRING=false
set DB_SERVICE=false
set MODEL_GENERATOR=false
set TS=false
set DETACH=true

rem Parse command-line options
:parse_options
if "%1" == "-h" goto show_help
if "%1" == "-b" (
    echo enabling build flag...
    set BUILD=true
) else if "%1" == "-a" (
    echo enabling all processes...
    set OCAML=true
    set SPRING=true
    set TS=true
) else if "%1" == "-o" (
    echo enabling ocaml process...
    set OCAML=true
) else if "%1" == "-j" (
    echo enabling all java processes...
    set SPRING=true
) else if "%1" == "-d" (
    echo enabling db-service...
    set DB_SERVICE=true
) else if "%1" == "-r" (
    echo enabling model-generator...
    set MODEL_GENERATOR=true
) else if "%1" == "-t" (
    echo enabling all typescript processes...
    set TS=true
) else if "%1" == "-e" (
    echo enabling all backend processes...
    set OCAML=true
    set SPRING=true
) else if "%1" == "-l" (
    echo log enabled, not detaching...
    set DETACH=false
) else if not "%1" == "" (
    echo Invalid option: %1
    goto show_help
    exit /b 1
)
shift
if not "%1" == "" goto parse_options

rem Check if any services were selected
if "%OCAML%" == "false" if "%SPRING%" == "false" if "%DB_SERVICE%" == "false" if "%MODEL_GENERATOR%" == "false" if "%TS%" == "false" (
    echo No services selected. Use -h for help.
    exit /b 1
)

rem Start core services
echo Starting services from third party images...
docker compose up --detach rabbitmq-server database

rem Function to start a service with or without build
:start_service
set service=%1
set build_flag=
set detach_flag=
if "%BUILD%" == "true" set build_flag=--build
if "%DETACH%" == "true" set detach_flag=--detach
docker compose up %detach_flag% %build_flag% %service%
goto :eof

rem Start services based on flags
if "%OCAML%" == "true" call :start_service otel-to-declare-converter
if "%SPRING%" == "true" call :start_service "db-service model-generator"
if "%DB_SERVICE%" == "true" call :start_service db-service
if "%MODEL_GENERATOR%" == "true" call :start_service model-generator
if "%TS%" == "true" call :start_service dashboard

exit /b 0
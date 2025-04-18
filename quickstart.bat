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
echo     -r         Enable trace-receiver
echo     -t         Enable all typescript processes.
echo     -e         Enable all backend processes.
goto :eof

rem Initialize variables
set BUILD=false
set OCAML=false
set SPRING=false
set DB_SERVICE=false
set TRACE_RECEIVER=false
set TS=false

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
    echo enabling trace-receiver...
    set TRACE_RECEIVER=true
) else if "%1" == "-t" (
    echo enabling all typescript processes...
    set TS=true
) else if "%1" == "-e" (
    echo enabling all backend processes...
    set OCAML=true
    set SPRING=true
) else if not "%1" == "" (
    echo Invalid option: %1
    goto show_help
    exit /b 1
)
shift
if not "%1" == "" goto parse_options

rem Start core services
echo Starting services from third party images...
docker compose up --detach rabbitmq-server database

rem Function to start a service with or without build
:start_service
set service=%1
set build_flag=
if "%BUILD%" == "true" set build_flag=--build
docker compose up --detach %build_flag% %service%
goto :eof

rem Start services based on flags
if "%OCAML%" == "true" call :start_service otel-to-probdeclare-converter
if "%SPRING%" == "true" call :start_service "db-service trace-receiver"
if "%DB_SERVICE%" == "true" call :start_service db-service
if "%TRACE_RECEIVER%" == "true" call :start_service trace-receiver
if "%TS%" == "true" call :start_service frontend

exit /b 0

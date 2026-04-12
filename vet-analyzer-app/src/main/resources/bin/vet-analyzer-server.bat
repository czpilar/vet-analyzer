@echo off
setlocal

set SCRIPT_DIR=%~dp0
set BASE_DIR=%SCRIPT_DIR%..

echo Starting Vet Analyzer Server v${project.version}...
java -jar "%BASE_DIR%\lib\vet-analyzer-server-${project.version}.jar" --spring.config.additional-location=file:%BASE_DIR%/config/

endlocal

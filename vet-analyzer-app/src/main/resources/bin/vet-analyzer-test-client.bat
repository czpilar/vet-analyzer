@echo off
setlocal

set SCRIPT_DIR=%~dp0
set BASE_DIR=%SCRIPT_DIR%..

echo Starting Vet Analyzer Test Client v${project.version}...
java -jar "%BASE_DIR%\lib\vet-analyzer-test-client-${project.version}.jar"

endlocal

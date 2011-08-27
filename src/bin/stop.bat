@ echo off

rem Change directory to the directory where the script is located
cd /d %~dp0

rem Create file to flag server to stop
dir >> .\STOP_ME

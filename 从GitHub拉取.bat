@echo off
cd /d "%~dp0"

echo ============================================
echo   Pull from GitHub
echo   Repo: https://github.com/Guayio2026/shopping
echo ============================================
echo.

for /f "delims=" %%b in ('git rev-parse --abbrev-ref HEAD') do set "BRANCH=%%b"

echo Current branch: %BRANCH%
echo.

set NO_PROXY=*
set no_proxy=*
set GIT_TERMINAL_PROMPT=0

echo [1/2] Fetching latest info from GitHub...
git -c http.proxy= -c https.proxy= -c http.sslVerify=false -c http.version=HTTP/1.1 fetch origin
if errorlevel 1 goto failed

echo.
echo [2/2] Pulling branch "%BRANCH%"...
git -c http.proxy= -c https.proxy= -c http.sslVerify=false -c http.version=HTTP/1.1 pull
if errorlevel 1 goto failed

echo.
echo  [OK] Up to date!  Branch: %BRANCH%
echo.
echo  Recent commits:
git log --oneline -5
echo.
pause
exit /b 0

:failed
echo.
echo  [FAILED] Pull failed!
echo   1. Turn OFF your VPN / proxy software completely
echo   2. Check your network connection
echo   3. If it says "local changes would be overwritten",
echo      commit or discard your changes first.
echo.
pause
exit /b 1

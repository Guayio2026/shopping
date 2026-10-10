@echo off
cd /d "%~dp0"

echo ============================================
echo   Push to GitHub
echo   Repo: https://github.com/Guayio2026/shopping
echo ============================================
echo.
echo Current changes:
git status --short
echo.

set "MSG="
set /p MSG="Commit message (Enter = skip commit, just push): "

if defined MSG (
    echo.
    echo [1/3] Staging changes...
    git add -A
    echo [2/3] Committing...
    git commit -m "%MSG%"
) else (
    echo.
    echo (No commit, pushing existing commits only)
)

echo.
echo [3/3] Pushing to GitHub...
echo.
set NO_PROXY=*
set no_proxy=*
set GIT_TERMINAL_PROMPT=0

git -c http.proxy= -c https.proxy= -c http.sslVerify=false -c http.postBuffer=104857600 -c http.version=HTTP/1.1 push

if errorlevel 1 (
    echo.
    echo  [FAILED] Push failed!
    echo   1. Turn OFF your VPN / proxy software completely
    echo   2. Check your network connection
    echo   3. Token may be expired - generate a new one on GitHub
) else (
    echo.
    echo  [OK] Done!
    echo  View: https://github.com/Guayio2026/shopping
)
echo.
pause

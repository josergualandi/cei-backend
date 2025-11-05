@echo off
setlocal
REM Push only the backend/ subtree to the 'backend' remote main branch
git subtree split --prefix=backend -b tmp-backend-split
if errorlevel 1 goto :error
git push backend tmp-backend-split:main
if errorlevel 1 goto :error
git branch -D tmp-backend-split >nul 2>&1
echo Backend subtree pushed to 'backend' remote (main).
goto :eof

:error
echo Error while pushing backend subtree. Please check git output above.
exit /b 1

endlocal


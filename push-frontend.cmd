@echo off
setlocal
REM Push only the frontend/ subtree to the 'frontend' remote main branch
git subtree split --prefix=frontend -b tmp-frontend-split
if errorlevel 1 goto :error
git push frontend tmp-frontend-split:main
if errorlevel 1 goto :error
git branch -D tmp-frontend-split >nul 2>&1
echo Frontend subtree pushed to 'frontend' remote (main).
goto :eof

:error
echo Error while pushing frontend subtree. Please check git output above.
exit /b 1

endlocal


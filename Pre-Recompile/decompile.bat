@echo off
xcopy /q /i /e copy C:\Users\jbras\AppData\Roaming\.minecraft
runtime\bin\python\python_mcp runtime\decompile.py %*
pause
rmdir /s /q C:\Users\jbras\AppData\Roaming\.minecraft
rmdir /s /q copy
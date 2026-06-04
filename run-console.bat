@echo off
chcp 65001 >nul
title Console Workout App
echo ========================================
echo   ЗАПУСК CONSOLE ВЕРСИИ
echo ========================================
echo.

java -cp "target/classes;target/lib/*" com.workout.app.MainConsole

echo.
echo [Завершено. Нажмите любую клавишу...]
pause >nul
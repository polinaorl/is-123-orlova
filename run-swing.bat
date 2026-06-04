@echo off
chcp 65001 >nul
title Swing Workout App
echo ========================================
echo     ЗАПУСК SWING ВЕРСИИ
echo ========================================
echo.

java -cp "target/classes;target/lib/*" com.workout.app.Main

echo.
echo [Завершено. Нажмите любую клавишу...]
pause >nul
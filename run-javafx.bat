@echo off
chcp 65001 >nul
title JavaFX Workout App
echo ========================================
echo   ЗАПУСК JAVAFX ВЕРСИИ
echo ========================================
echo.

java --add-modules javafx.controls,javafx.fxml -cp "target/classes;target/lib/*" com.workout.app.MainJavaFX

echo.
echo [Завершено. Нажмите любую клавишу...]
pause >nul
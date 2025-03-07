@echo off
echo 🧪 Ejecutando pruebas y generando informe de cobertura...
call mvn clean test

if %ERRORLEVEL% == 0 (
    echo ✅ Pruebas ejecutadas correctamente
    
    if exist "target\site\jacoco\index.html" (
        echo 📊 Informe de cobertura generado en: target\site\jacoco\index.html
        start "" "target\site\jacoco\index.html"
    ) else (
        echo ❌ Error: No se encontró el informe de cobertura
    )
) else (
    echo ❌ Error: Las pruebas fallaron
)

pause 
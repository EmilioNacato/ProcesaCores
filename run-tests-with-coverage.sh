#!/bin/bash

# Script para ejecutar pruebas y generar informe de cobertura con JaCoCo

echo "🧪 Ejecutando pruebas y generando informe de cobertura..."
mvn clean test

# Verificar si las pruebas fueron exitosas
if [ $? -eq 0 ]; then
    echo "✅ Pruebas ejecutadas correctamente"
    
    # Verificar si el informe de cobertura existe
    if [ -f "target/site/jacoco/index.html" ]; then
        echo "📊 Informe de cobertura generado en: target/site/jacoco/index.html"
        
        # Abrir el informe en el navegador predeterminado (funciona en Windows, Linux y macOS)
        case "$(uname -s)" in
            Linux*)     xdg-open target/site/jacoco/index.html ;;
            Darwin*)    open target/site/jacoco/index.html ;;
            CYGWIN*|MINGW*|MSYS*) start target/site/jacoco/index.html ;;
            *)          echo "Abra manualmente el archivo: target/site/jacoco/index.html" ;;
        esac
    else
        echo "❌ Error: No se encontró el informe de cobertura"
    fi
else
    echo "❌ Error: Las pruebas fallaron"
fi 
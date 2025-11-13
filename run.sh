#!/bin/bash

# Script de ejecución para el Sistema de Gestión de Pedidos y Envíos
# Asegúrese de tener el driver MySQL Connector/J en el directorio lib/

echo "Ejecutando el Sistema de Gestión de Pedidos y Envíos..."

# Verificar que las clases estén compiladas
if [ ! -d "classes" ]; then
    echo "Error: Las clases no están compiladas. Ejecute primero: ./compile.sh"
    exit 1
fi

# Ejecutar el programa
java -cp ".:lib/mysql-connector-j-8.2.0.jar:classes" tfi.main.Main

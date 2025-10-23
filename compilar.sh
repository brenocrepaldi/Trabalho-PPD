#!/bin/bash

# Script para compilar todos os arquivos Java

echo "=== Compilando projeto ==="
echo ""

# Cria diretório de saída
mkdir -p bin

# Compila todas as classes
echo "Compilando classes de comunicação..."
javac -d bin src/comunicacao/*.java

echo "Compilando Receptor..."
javac -d bin -cp bin src/servidor/*.java

echo "Compilando Distribuidor..."
javac -d bin -cp bin src/cliente/*.java

echo "Compilando programa sequencial..."
javac -d bin src/sequencial/*.java

echo "Compilando utilitários..."
javac -d bin src/util/*.java

echo ""
echo "=== Compilação concluída ==="
echo "Arquivos compilados em: bin/"

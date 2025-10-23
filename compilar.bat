@echo off
REM Script para compilar todos os arquivos Java no Windows

echo === Compilando projeto ===
echo.

REM Cria diretório de saída
if not exist bin mkdir bin
if not exist bin\comunicacao mkdir bin\comunicacao
if not exist bin\servidor mkdir bin\servidor
if not exist bin\cliente mkdir bin\cliente
if not exist bin\sequencial mkdir bin\sequencial
if not exist bin\util mkdir bin\util
if not exist bin\teste mkdir bin\teste

REM Compila todas as classes
echo Compilando classes de comunicacao...
javac -d bin src\comunicacao\*.java

echo Compilando Receptor...
javac -d bin -cp bin src\servidor\*.java

echo Compilando Distribuidor...
javac -d bin -cp bin src\cliente\*.java

echo Compilando programa sequencial...
javac -d bin src\sequencial\*.java

echo Compilando utilitarios...
javac -d bin src\util\*.java

echo.
echo === Compilacao concluida ===
echo Arquivos compilados em: bin\

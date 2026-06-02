@echo off
setlocal
cd /d %~dp0

if not exist bin mkdir bin
dir /s /b src\*.java > sources.txt
javac -encoding UTF-8 -d bin @sources.txt
del sources.txt

set CONNECTOR=
if exist lib\mysql-connector-j.jar set CONNECTOR=lib\mysql-connector-j.jar
if exist lib\mysql-connector-java.jar set CONNECTOR=lib\mysql-connector-java.jar

if "%CONNECTOR%"=="" (
  echo WARNING: MySQL Connector/J jar was not found in java-web\lib.
  echo Download it and place it as java-web\lib\mysql-connector-j.jar before connecting to MySQL.
  java -cp bin bjut.baseball.Main
) else (
  java -cp "bin;%CONNECTOR%" bjut.baseball.Main
)

endlocal

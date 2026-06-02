#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"

mkdir -p bin
find src -name "*.java" > sources.txt
javac -encoding UTF-8 -d bin @sources.txt
rm sources.txt

CONNECTOR=""
for candidate in lib/mysql-connector-j.jar lib/mysql-connector-java.jar lib/mysql-connector*.jar; do
  if compgen -G "$candidate" > /dev/null; then
    CONNECTOR="$(ls $candidate | head -n 1)"
    break
  fi
done

if [[ -z "$CONNECTOR" ]]; then
  echo "WARNING: MySQL Connector/J jar was not found in java-web/lib."
  echo "Download it and place it as java-web/lib/mysql-connector-j.jar before connecting to MySQL."
  echo "The application will start, but database pages will fail until the driver is available."
  java -cp bin bjut.baseball.Main
else
  java -cp "bin:$CONNECTOR" bjut.baseball.Main
fi

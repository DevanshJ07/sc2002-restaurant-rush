#!/bin/sh
set -eu
cd "$(dirname "$0")"
mkdir -p out out-test
javac --release 17 -d out src/main/java/restaurantrush/*.java
javac --release 17 -cp out -d out-test src/test/java/restaurantrush/*.java
case "$(uname -s)" in MINGW*|MSYS*|CYGWIN*) sep=';' ;; *) sep=':' ;; esac
for file in src/test/java/restaurantrush/*Checks.java; do
  name=$(basename "$file" .java)
  java -cp "out${sep}out-test" "restaurantrush.$name"
done

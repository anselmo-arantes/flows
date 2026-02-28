#!/usr/bin/env bash
set -euo pipefail

BUILD_DIR="build/offline-tests"
rm -rf "$BUILD_DIR"
mkdir -p "$BUILD_DIR"

javac -d "$BUILD_DIR" \
  src/main/java/com/anselmo/flows/domain/model/*.java \
  src/main/java/com/anselmo/flows/application/port/in/*.java \
  src/main/java/com/anselmo/flows/application/port/out/*.java \
  src/main/java/com/anselmo/flows/application/service/FlowRequestHandlerService.java \
  offline-tests/FlowRequestHandlerOfflineTest.java

java -cp "$BUILD_DIR" FlowRequestHandlerOfflineTest

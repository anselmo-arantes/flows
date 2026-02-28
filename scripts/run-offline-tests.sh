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
  src/main/java/com/anselmo/flows/adapters/out/crypto/CryptoOperationException.java \
  src/main/java/com/anselmo/flows/adapters/out/crypto/FlowCryptoServiceAdapter.java \
  offline-tests/*.java

java -cp "$BUILD_DIR" FlowRequestHandlerOfflineTest
java -cp "$BUILD_DIR" FlowCryptoAdapterOfflineTest

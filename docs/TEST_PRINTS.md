# Test Results (JUnit real x Offline smoke)

## 1) JUnit real (Maven Surefire) — tentativa direta

```bash
mvn -U test
```

```text
[ERROR] Plugin org.apache.maven.plugins:maven-resources-plugin:3.3.1 ... 403 Forbidden
```

## 2) JUnit real com workaround do resources plugin

```bash
./scripts/install-local-resources-plugin-fallback.sh
mvn -U test
```

```text
Installed local fallback plugin at: /root/.m2/repository/org/apache/maven/plugins/maven-resources-plugin/3.3.1
[ERROR] Plugin org.apache.maven.plugins:maven-compiler-plugin:3.13.0 ... 403 Forbidden
```

**Status JUnit real:** ainda bloqueado no ambiente por 403 ao baixar plugins Maven.

## 3) Offline smoke tests (sem Maven)

```bash
./scripts/run-offline-tests.sh
```

```text
PASS: FlowRequestHandlerOfflineTest
```

**Status offline smoke:** executado com sucesso.

## Leitura correta

- `mvn test` = validação JUnit real (quando dependências/plugins Maven estão acessíveis).
- `run-offline-tests.sh` = smoke check mínimo e não substitui a suíte JUnit.

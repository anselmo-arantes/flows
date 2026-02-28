# Test Results (Prints de execução)

## Resumo rápido

| Suite | Comando | Resultado |
|---|---|---|
| Maven root test run | `mvn -U test` | ✅ BUILD SUCCESS |
| Offline smoke tests | `./scripts/run-offline-tests.sh` | ✅ 2/2 PASS |

---

## 1) Maven (`mvn -U test`)

```bash
mvn -U test
```

```text
[INFO] Scanning for projects...
[INFO] -------------------------< com.anselmo:flows >--------------------------
[INFO] Building flows 1.0.0
[INFO] --------------------------------[ pom ]---------------------------------
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

## 2) Offline tests (`./scripts/run-offline-tests.sh`)

```bash
./scripts/run-offline-tests.sh
```

```text
PASS: FlowRequestHandlerOfflineTest
PASS: FlowCryptoAdapterOfflineTest
```

## Observação

Estes são os prints reais coletados no ambiente atual e anexados no PR para leitura rápida.

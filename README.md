# WhatsApp Flows Data Exchange API

API Java 17 com Spring Boot 3.x para processar **WhatsApp Flows Data Exchange** com arquitetura **hexagonal (Ports & Adapters)**.

## Stack
- Java 17
- Spring Boot 3.3.x
- Maven
- AES/CBC/PKCS5Padding + RSA OAEP SHA-256

## Estrutura

```text
src/main/java/com/anselmo/flows
├── domain
│   └── model
├── application
│   ├── port/in
│   ├── port/out
│   └── service
├── adapters
│   ├── in/http
│   └── out/crypto
└── config
```

## Gerar chave RSA (OpenSSL)

```bash
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out private_key.pem
openssl rsa -pubout -in private_key.pem -out public_key.pem
```

Exporte a private key como variável de ambiente:

```bash
export FLOWS_RSA_PRIVATE_KEY_PEM="$(cat private_key.pem)"
```

Opcional para trocar transformação RSA:

```bash
export FLOWS_RSA_TRANSFORMATION="RSA/ECB/OAEPWithSHA-256AndMGF1Padding"
```

## Executar localmente

```bash
mvn spring-boot:run
```

A API sobe em `http://localhost:8080`.

## Endpoint

`POST /v1/flows/data`

Request JSON:

```json
{
  "encrypted_flow_data": "<base64>",
  "encrypted_aes_key": "<base64>",
  "initial_vector": "<base64>"
}
```

Response JSON:

```json
{
  "encrypted_flow_data": "<base64>"
}
```

## Exemplo de teste com curl

O script abaixo gera AES key + IV, criptografa um payload de exemplo e chama o endpoint:

```bash
AES_KEY_HEX=$(openssl rand -hex 16)
IV_HEX=$(openssl rand -hex 16)

cat > payload.json <<'JSON'
{"version":"1.0","user_locale":"pt_BR","action":"init","screen":"START","data":{},"flow_token":"token-123"}
JSON

ENCRYPTED_FLOW_DATA=$(openssl enc -aes-128-cbc -K "$AES_KEY_HEX" -iv "$IV_HEX" -base64 -A -in payload.json)

echo "$AES_KEY_HEX" | xxd -r -p > aes_key.bin
ENCRYPTED_AES_KEY=$(openssl pkeyutl -encrypt -pubin -inkey public_key.pem \
  -pkeyopt rsa_padding_mode:oaep -pkeyopt rsa_oaep_md:sha256 \
  -in aes_key.bin | base64 -w 0)

IV_B64=$(echo "$IV_HEX" | xxd -r -p | base64 -w 0)

curl -sS -X POST http://localhost:8080/v1/flows/data \
  -H 'Content-Type: application/json' \
  -d "{\"encrypted_flow_data\":\"$ENCRYPTED_FLOW_DATA\",\"encrypted_aes_key\":\"$ENCRYPTED_AES_KEY\",\"initial_vector\":\"$IV_B64\"}"
```

## Testes

```bash
mvn test
```


## Ambiente restrito (erro 403 no Maven Central)

Se aparecer erro `Could not transfer artifact ... 403 Forbidden`, execute com um mirror interno:

```bash
export MAVEN_MIRROR_URL="https://SEU_MIRROR_MAVEN/repository/maven-public"
mvn -s .mvn/settings-mirror.xml -U test
```

Sem mirror, o Maven usa o repositório default (`repo.maven.apache.org`).


### Workaround local para `maven-resources-plugin` (403 no Central)

Se o erro travar especificamente em `maven-resources-plugin:3.3.1`, rode:

```bash
./scripts/install-local-resources-plugin-fallback.sh
mvn -U test
```

Esse script instala um plugin fallback **somente local** em `~/.m2` para destravar essa etapa em ambientes com bloqueio de download.


### Execução de testes sem acesso a repositórios Maven

Quando o ambiente bloqueia download de artefatos (`403`), rode os testes offline:

```bash
./scripts/run-offline-tests.sh
```

Saída esperada:

```text
PASS: FlowRequestHandlerOfflineTest
```


### Diferença entre JUnit real e offline smoke

- `mvn -U test`: executa a suíte JUnit real (`src/test/java`) via Surefire.
- `./scripts/run-offline-tests.sh`: executa smoke test local mínimo e **não substitui** a suíte JUnit completa.

Use os dois quando possível: primeiro Maven JUnit, depois offline smoke como sanity check rápido.

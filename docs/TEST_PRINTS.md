# Test Prints

## 1) Antes do workaround

```bash
mvn -U test
```

```text
[ERROR] Plugin org.apache.maven.plugins:maven-resources-plugin:3.3.1 ... 403 Forbidden
```

## 2) Aplicando workaround local para resources plugin

```bash
./scripts/install-local-resources-plugin-fallback.sh
```

```text
Installed local fallback plugin at: /root/.m2/repository/org/apache/maven/plugins/maven-resources-plugin/3.3.1
```

## 3) Reexecução após correção do `maven-resources-plugin`

```bash
mvn -U test
```

```text
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/plugins/maven-compiler-plugin/3.13.0/maven-compiler-plugin-3.13.0.pom
[ERROR] Plugin org.apache.maven.plugins:maven-compiler-plugin:3.13.0 ... 403 Forbidden
```

## Resultado

- O erro específico pedido (`maven-resources-plugin ... 403`) foi resolvido localmente.
- A execução agora avança e falha no próximo plugin remoto (`maven-compiler-plugin`) devido ao mesmo bloqueio de rede/proxy.

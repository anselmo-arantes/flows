# Test Prints

## 1) Reexecução com POM corrigido

```bash
mvn -U test
```

```text
[INFO] Building flows 1.0.0
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/plugins/maven-resources-plugin/3.3.1/maven-resources-plugin-3.3.1.pom
[ERROR] Plugin org.apache.maven.plugins:maven-resources-plugin:3.3.1 ... 403 Forbidden
```

## 2) Tentativa com settings de mirror configurável

```bash
MAVEN_MIRROR_URL=https://repo.maven.apache.org/maven2 mvn -s .mvn/settings-mirror.xml -U test
```

```text
Downloading from corp-mirror: https://repo.maven.apache.org/maven2/org/apache/maven/plugins/maven-resources-plugin/3.3.1/maven-resources-plugin-3.3.1.pom
[ERROR] ... from/to corp-mirror ... Network is unreachable
```

## Resultado

- O erro original de parent POM já está resolvido via parent local.
- O bloqueio restante é de conectividade/permissão de rede para baixar plugins/dependências Maven.
- Foi adicionado `.mvn/settings-mirror.xml` para usar mirror corporativo via `MAVEN_MIRROR_URL`.

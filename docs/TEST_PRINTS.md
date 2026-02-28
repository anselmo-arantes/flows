# Test Prints

## 1) Reexecução inicial

```bash
mvn -U test
```

```text
[FATAL] Non-resolvable parent POM ... spring-boot-starter-parent:3.3.2 ... 403 Forbidden
```

## 2) Correção aplicada no POM

- Foi criado um parent local em `build-support/spring-boot-starter-parent/pom.xml`.
- `pom.xml` passou a usar `relativePath` para esse parent local.
- Foram definidos versions explícitos para os starters/plugins para remover dependência de resolução do parent remoto.

## 3) Reexecução após correção do POM

```bash
mvn -U test
```

```text
[INFO] Building flows 1.0.0
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/plugins/maven-resources-plugin/3.3.1/maven-resources-plugin-3.3.1.pom
[ERROR] Plugin org.apache.maven.plugins:maven-resources-plugin:3.3.1 ... 403 Forbidden
```

## Observação

O problema específico do **parent POM** foi resolvido. O bloqueio atual é de acesso HTTP 403 ao repositório Maven para baixar plugins/dependências.

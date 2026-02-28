# Test Prints

## Command

```bash
mvn -U test
```

## Output (capturado)

```text
[INFO] Scanning for projects...
Downloading from central: https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-starter-parent/3.3.2/spring-boot-starter-parent-3.3.2.pom
[ERROR] [ERROR] Some problems were encountered while processing the POMs:
[FATAL] Non-resolvable parent POM for com.anselmo:flows:1.0.0: The following artifacts could not be resolved: org.springframework.boot:spring-boot-starter-parent:pom:3.3.2 (absent): Could not transfer artifact org.springframework.boot:spring-boot-starter-parent:pom:3.3.2 from/to central (https://repo.maven.apache.org/maven2): status code: 403, reason phrase: Forbidden (403)
```

## Observação

Mesmo com internet liberada, neste ambiente o Maven Central continua respondendo HTTP 403 para o parent POM do Spring Boot, impedindo a execução dos testes.

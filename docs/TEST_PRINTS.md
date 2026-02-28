# Test Prints

## Command

```bash
mvn test -DskipTests=false
```

## Output (capturado)

```text
[INFO] Scanning for projects...
Downloading from central: https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-starter-parent/3.3.2/spring-boot-starter-parent-3.3.2.pom
[ERROR] [ERROR] Some problems were encountered while processing the POMs:
[FATAL] Non-resolvable parent POM for com.anselmo:flows:1.0.0: The following artifacts could not be resolved: org.springframework.boot:spring-boot-starter-parent:pom:3.3.2 (absent): Could not transfer artifact org.springframework.boot:spring-boot-starter-parent:pom:3.3.2 from/to central (https://repo.maven.apache.org/maven2): status code: 403, reason phrase: Forbidden (403)
```

## Observação

No ambiente atual, os testes não executam porque o Maven não consegue resolver o parent POM do Spring Boot devido a HTTP 403 no repositório central.

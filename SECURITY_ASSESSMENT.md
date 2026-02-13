# Security Vulnerability Assessment Report

**Application:** Eviden Retail Banking Portal (baseapp)
**Date:** 2026-02-13
**Assessor:** Devin AI (Automated Security Review)

---

## Stack Detection Summary

| Component            | Detected Value                                      |
|----------------------|-----------------------------------------------------|
| Java Version         | 21 (pom.xml `<java.version>21</java.version>`)      |
| Spring Boot          | 3.4.2 (pom.xml parent)                              |
| Build Tool           | Maven                                                |
| Packaging            | JAR (`baseapp-0.0.1-SNAPSHOT.jar`)                   |
| Deployment           | Docker (multi-stage Dockerfile)                      |
| Database             | H2 in-memory (`jdbc:h2:mem:testdb`)                  |
| Frontend             | React.js + Webpack (bundled into static resources)   |
| Message Queue        | RabbitMQ (spring-rabbit, raw AMQP client)            |
| Logging Sink         | Elasticsearch (hardcoded IP, HTTP)                   |

---

## 1. Executive Summary

This application is a retail banking portal that **lacks fundamental security controls** required for any production deployment handling financial data. The most critical finding is the **complete absence of authentication and authorization** -- every endpoint, including fund transfers and the H2 database console, is publicly accessible without credentials.

**27 distinct security findings** were identified across all assessment categories:

| Severity | Count |
|----------|-------|
| Critical | 6     |
| High     | 9     |
| Medium   | 7     |
| Low      | 5     |

The application is **not suitable for production deployment** in its current state. Immediate remediation of Critical and High findings is required before any exposure to users or networks.

---

## 2. Vulnerability Table

| # | Severity | OWASP 2021 | File / Path | Description | CVE / CWE | Fix |
|---|----------|------------|-------------|-------------|-----------|-----|
| 1 | **CRITICAL** | A07 - Auth Failures | `pom.xml` | **No Spring Security dependency.** No authentication or authorization exists anywhere in the application. All endpoints (including fund transfers, account data, H2 console) are completely open. | CWE-306 (Missing AuthN) | Add `spring-boot-starter-security`, implement `SecurityFilterChain`, configure user authentication (OAuth2/JWT), add method-level `@PreAuthorize`. |
| 2 | **CRITICAL** | A01 - Broken Access Control | `application.properties:7-8` | **H2 Console enabled and exposed** at `/h2-console` with empty password (`sa`/blank). Allows full database access, arbitrary SQL execution, and potential RCE via H2's `RUNSCRIPT` or `CREATE ALIAS`. | CWE-284 (Improper Access Control), CVE-2022-23221 (historical) | Set `spring.h2.console.enabled=false` (or remove entirely). Never expose H2 console in production. Use a production database (PostgreSQL/MySQL). |
| 3 | **CRITICAL** | A01 - Broken Access Control | `BankAccountController.java:97-110` | **Path traversal in `createZip()`**. Takes user-supplied `sourceDir` and `zipFile` via `@PathVariable` with zero sanitization. An attacker can read arbitrary files from the server filesystem. | CWE-22 (Path Traversal) | Remove this endpoint or restrict to an allow-listed directory with path canonicalization: `file.getCanonicalPath().startsWith(allowedBase)`. |
| 4 | **CRITICAL** | A05 - Security Misconfig | `pom.xml:36-39` | **`spring-boot-devtools` included** as a non-scoped dependency. Enables remote debugging, auto-restart, and LiveReload in production. This is flagged by OWASP as a misconfiguration risk. | CWE-16 (Configuration) | Remove `spring-boot-devtools` entirely or scope it: `<scope>runtime</scope>` with `<optional>true</optional>`. DevTools auto-disables in packaged JARs, but the dependency should still be excluded from production builds. |
| 5 | **CRITICAL** | A06 - Vuln Components | `Dockerfile:17` | **Docker runtime image `openjdk:21-ea-10-jdk-slim`** is an Early Access build with **78+ known vulnerabilities** (1 Critical: CVE-2023-45853 zlib integer overflow, 8 High). Also uses full JDK instead of JRE, increasing attack surface. | CVE-2023-45853 (Critical), multiple others | Replace with `eclipse-temurin:21-jre-alpine` (stable GA, minimal, JRE-only). |
| 6 | **CRITICAL** | A03 - Injection | `BankAccountController.java:61-64` | **Log injection via `/logmessage` endpoint.** `logger.info(logmsg)` writes arbitrary user input directly to application logs, enabling log forging, CRLF injection, and potential exploitation of log analysis tools. | CWE-117 (Log Injection) | Remove this endpoint. If logging user input is required, sanitize by stripping newlines/control characters and use parameterized logging. |
| 7 | **HIGH** | A01 - Broken Access Control | `HomeController.java:12` | **`@RequestMapping` without HTTP method restriction** allows all HTTP methods (GET, POST, PUT, DELETE, etc.) on `/`. SonarQube flags this as CSRF-vulnerable (java:S3752). | CWE-352 (CSRF) | Change to `@GetMapping("/")`. |
| 8 | **HIGH** | A04 - Insecure Design | `TransferDTO.java`, `Transfer.java:16-25` | **No input validation on fund transfers.** `@NotNull` and `@Size` annotations are commented out in `Transfer.java`. No `@Valid` on controller `@RequestBody`. Amount can be negative, zero, or exceed balance. Account numbers are unvalidated strings. | CWE-20 (Improper Input Validation) | Uncomment and add Bean Validation annotations. Add `@Valid` to controller parameter. Validate: amount > 0, account number format (regex), sender != receiver. Use `BigDecimal` for monetary amounts. |
| 9 | **HIGH** | A02 - Crypto Failures | `application.properties:4-5` | **Default/empty database credentials.** `spring.datasource.username=sa` with blank password. While H2 in-memory is for dev, this pattern teaches insecure defaults and will carry to production if template is reused. | CWE-798 (Hardcoded Credentials) | Use externalized secrets (environment variables, Vault, K8s Secrets). Never commit credentials, even for dev databases. |
| 10 | **HIGH** | A05 - Security Misconfig | `InfoAppender.java:20` | **Hardcoded internal IP** `http://10.128.0.45:9200` for Elasticsearch. Exposes internal infrastructure topology. Logs sent over plain HTTP (no TLS). | CWE-798 (Hardcoded Credentials), CWE-319 (Cleartext Transmission) | Externalize to `application.properties` or environment variable. Use HTTPS with TLS. Add authentication to Elasticsearch connection. |
| 11 | **HIGH** | A07 - Auth Failures | Spring Data REST auto-config | **Spring Data REST exposes repository at `/api`** via `spring-boot-starter-data-rest`. `BankAccountRepository` is auto-exposed, allowing unauthenticated CRUD operations (including DELETE, PATCH) on all bank accounts. | CWE-306 (Missing AuthN) | Either remove `spring-boot-starter-data-rest` or annotate repository with `@RepositoryRestResource(exported = false)`. Add Spring Security to protect all endpoints. |
| 12 | **HIGH** | A04 - Insecure Design | `TransferServiceImpl.java:27-52` | **Transfer business logic defects.** (1) Returns `null` instead of empty map on failure (NullPointerException risk). (2) Puts `receiverAccount` in both map entries (line 42-43 bug). (3) Catches generic `Exception`. (4) `Optional.of()` on nullable `findByAccountNumber()` result will throw NPE. | CWE-754 (Improper Check for Exceptional Conditions) | Return `Optional.empty()` or throw domain-specific exceptions. Fix the sender/receiver map bug. Use `Optional.ofNullable()`. Add `@Transactional` for atomicity. |
| 13 | **HIGH** | A08 - Integrity Failures | `BankAccountController.java:67` | **JPA entity used as request parameter** (SonarQube java:S4684, CRITICAL). `Transfer` entity is constructed from DTO but still persisted -- the entity has `@Id` and `@GeneratedValue` which could be manipulated if entity were used directly. | CWE-915 (Mass Assignment) | Already partially fixed with `TransferDTO`, but ensure entity fields like `id` are never settable from external input. Add `@JsonIgnore` on `id` field or use the DTO pattern consistently. |
| 14 | **HIGH** | A05 - Security Misconfig | Entire application | **No HTTPS/TLS configuration.** Application runs on plain HTTP (port 8080). No `server.ssl.*` properties. No HSTS. All data (including account numbers, balances, transfer amounts) transmitted in cleartext. | CWE-319 (Cleartext Transmission) | Configure `server.ssl.key-store`, `server.ssl.key-store-password`, etc. Add HSTS header. Redirect HTTP to HTTPS. In Kubernetes, terminate TLS at ingress. |
| 15 | **HIGH** | A06 - Vuln Components | `pom.xml:10` | **Spring Boot 3.4.2 affected by CVE-2025-22235** (Medium CVSS). `EndpointRequest.to()` creates wrong matcher if actuator endpoint is not exposed, potentially leaving paths unprotected. | CVE-2025-22235 | Upgrade Spring Boot to `3.4.5` or later. |
| 16 | **MEDIUM** | A05 - Security Misconfig | Entire application | **No security headers configured.** Missing `X-Content-Type-Options`, `X-Frame-Options`, `X-XSS-Protection`, `Content-Security-Policy`, `Referrer-Policy`, `Permissions-Policy`. | CWE-693 (Protection Mechanism Failure) | Add Spring Security (which sets most headers by default) or configure via `WebMvcConfigurer`. Add CSP header for React SPA. |
| 17 | **MEDIUM** | A04 - Insecure Design | All API endpoints | **No rate limiting or throttling.** Transfer endpoint can be called unlimited times. No protection against brute-force or abuse. | CWE-770 (Allocation without Limits) | Add `spring-boot-starter-actuator` with rate limiting. Use a library like Bucket4j or Spring Cloud Gateway rate limiter. Implement per-IP and per-account limits on sensitive operations. |
| 18 | **MEDIUM** | A05 - Security Misconfig | `ConsumerService.java:32` | **Hardcoded RabbitMQ host** `localhost`. No authentication configured for RabbitMQ connection (`ConnectionFactory` defaults). | CWE-798 (Hardcoded Credentials) | Externalize RabbitMQ configuration to `application.properties`. Use Spring AMQP auto-configuration instead of raw `ConnectionFactory`. Configure credentials via environment variables. |
| 19 | **MEDIUM** | A09 - Logging Failures | `ConsumerService.java:22,25`, `FileService.java:39` | **`System.out.println()` and `e.printStackTrace()` used instead of proper logging.** Output bypasses logging framework, misses log levels, and cannot be monitored. `e.printStackTrace()` in FileService may leak stack traces. | CWE-778 (Insufficient Logging) | Replace all `System.out.println()` with `logger.info()` / `logger.debug()`. Replace `e.printStackTrace()` with `logger.error("message", e)`. |
| 20 | **MEDIUM** | A08 - Integrity Failures | `Dockerfile:12` | **Tests skipped in Docker build** (`-Dskiptest=true`). Note: the flag is also incorrect -- Maven uses `-DskipTests=true` (capital T). This means tests never run during container builds. | CWE-1068 (Inconsistency Between Implementation and Specification) | Fix to `-DskipTests=true` for consistency, but ideally run tests during CI/CD before the Docker build stage, not skip them. |
| 21 | **MEDIUM** | A05 - Security Misconfig | `Dockerfile:17-21` | **Container runs as root.** No `USER` directive in Dockerfile. No `HEALTHCHECK`. Duplicate `COPY pom.xml` (line 6-7). Unnecessary `target/classes` copy. | CWE-250 (Execution with Unnecessary Privileges) | Add `RUN addgroup -S app && adduser -S app -G app` and `USER app`. Add `HEALTHCHECK`. Remove duplicate COPY and unnecessary classes copy. |
| 22 | **MEDIUM** | A02 - Crypto Failures | `BankAccount.java:15`, `Transfer.java:25` | **`Double` used for monetary values.** Floating-point arithmetic causes precision errors in financial calculations (e.g., `0.1 + 0.2 != 0.3`). This can lead to rounding errors in transfers. | CWE-682 (Incorrect Calculation) | Use `java.math.BigDecimal` for all monetary fields. Use `BigDecimal.ZERO` comparisons. Set scale and rounding mode explicitly. |
| 23 | **LOW** | A09 - Logging Failures | `README.md:32-34` | **SonarQube authentication tokens exposed in README.** `sqp_1d0e5fc2db8d5607c3d2c149cfab971a5de52e6c` and `sqp_32adab78b184c38dc10bc0cd674e86cf07528a7c` are committed to the repository. | CWE-312 (Cleartext Storage of Sensitive Info) | Remove tokens from README. Rotate the exposed tokens immediately. Use environment variables or CI/CD secrets for SonarQube authentication. |
| 24 | **LOW** | A04 - Insecure Design | `MapHolder.java:12-17` | **Non-thread-safe singleton.** `create()` method has a race condition -- multiple threads can create separate instances. Uses raw types (no generics). Unbounded ConcurrentHashMap acts as a memory leak (entries never evicted). | CWE-362 (Race Condition) | Use `enum` singleton pattern or `volatile` + double-checked locking. Add generic type parameters properly. Implement eviction/size limits. |
| 25 | **LOW** | A04 - Insecure Design | `BankAccountController.java:112-115` | **Debug/internal endpoint exposed.** `GET /api/bankaccounts/connect` connects to RabbitMQ. `GET /api/bankaccounts/createzip` performs file operations. These are debug/utility features that should not be in production. | CWE-489 (Active Debug Code) | Remove debug endpoints or gate them behind admin authentication and feature flags. |
| 26 | **LOW** | A05 - Security Misconfig | `webpack.config.js:5,7` | **Webpack `source-map` devtool and `development` mode** in build config. Source maps expose original source code in production. Development mode disables optimizations. | CWE-540 (Information Exposure Through Source Code) | Set `mode: 'production'` and `devtool: false` (or `'hidden-source-map'`) for production builds. Use environment-based config. |
| 27 | **LOW** | A04 - Insecure Design | `Components.java:3-16` | **9 unused/duplicate imports.** While not a direct vulnerability, code quality issues indicate lack of review processes and increase maintenance burden. RestTemplate bean created without timeout configuration (DoS risk via slow responses). | CWE-1164 (Irrelevant Code) | Remove unused imports. Configure timeouts on RestTemplate: `setConnectTimeout()`, `setReadTimeout()`. Consider using `WebClient` (already in deps) instead. |

---

## 3. Secure Code Recommendations

### 3.1 Add Spring Security (Critical Priority)

```xml
<!-- pom.xml: Add dependency -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

```java
// SecurityConfig.java: Minimal SecurityFilterChain
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/built/**", "/main.css", "/bootstrap.min.css").permitAll()
                .requestMatchers("/api/bankaccounts/transfer").hasRole("USER")
                .requestMatchers("/api/bankaccounts/**").authenticated()
                .anyRequest().denyAll()
            )
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            )
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'"))
                .frameOptions(frame -> frame.deny())
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

### 3.2 Add Input Validation to Transfer

```java
// TransferDTO.java: Add validation annotations
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Pattern;

public class TransferDTO {

    @NotBlank(message = "Sender account is required")
    @Pattern(regexp = "^\\d{12}$", message = "Account number must be 12 digits")
    private String senderAccount;

    @NotBlank(message = "Receiver account is required")
    @Pattern(regexp = "^\\d{12}$", message = "Account number must be 12 digits")
    private String receiverAccount;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    // getters/setters...
}
```

```java
// BankAccountController.java: Add @Valid
@PostMapping("/transfer")
public ResponseEntity<String> transfer(@Valid @RequestBody TransferDTO transferDTO) {
    // ...
}
```

### 3.3 Fix TransferServiceImpl Logic

```java
@Service
@Transactional
public class TransferServiceImpl {

    public Map<String, BankAccount> transfer(Transfer transfer) {
        BankAccount sender = bankAccountRepository.findByAccountNumber(transfer.getSenderAccount());
        BankAccount receiver = bankAccountRepository.findByAccountNumber(transfer.getReceiverAccount());

        if (sender == null || receiver == null) {
            throw new AccountNotFoundException("One or both accounts not found");
        }
        if (sender.getBalance().compareTo(transfer.getAmount()) < 0) {
            throw new InsufficientFundsException("Insufficient balance");
        }

        sender.setBalance(sender.getBalance().subtract(transfer.getAmount()));
        receiver.setBalance(receiver.getBalance().add(transfer.getAmount()));

        bankAccountRepository.save(sender);
        bankAccountRepository.save(receiver);

        return Map.of("senderAccount", sender, "receiverAccount", receiver);
    }
}
```

### 3.4 Fix Dockerfile

```dockerfile
# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build
COPY src /home/app/src
COPY pom.xml /home/app
COPY ./package.json /home/app
COPY ./webpack.config.js /home/app
RUN mvn -f /home/app/pom.xml clean install -DskipTests=true

# Runtime stage - use stable GA JRE image
FROM eclipse-temurin:21-jre-alpine

# Run as non-root
RUN addgroup -S app && adduser -S app -G app

COPY --from=build /home/app/target/baseapp-0.0.1-SNAPSHOT.jar /usr/local/lib/baseapp.jar

RUN chown app:app /usr/local/lib/baseapp.jar
USER app

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s \
  CMD wget -q --spider http://localhost:8080/ || exit 1

ENTRYPOINT ["java", "-jar", "/usr/local/lib/baseapp.jar"]
```

### 3.5 Remove/Secure Debug Endpoints

Remove these endpoints from `BankAccountController.java`:
- `POST /api/bankaccounts/logmessage` (log injection vector)
- `GET /api/bankaccounts/createzip` (path traversal vector)
- `GET /api/bankaccounts/connect` (debug endpoint)

### 3.6 Externalize Secrets and Configuration

```properties
# application.properties - use environment variable placeholders
spring.datasource.url=${DB_URL:jdbc:h2:mem:testdb}
spring.datasource.username=${DB_USERNAME:sa}
spring.datasource.password=${DB_PASSWORD:}
spring.h2.console.enabled=false

# Elasticsearch (externalized)
app.elasticsearch.host=${ELASTICSEARCH_HOST:http://localhost:9200}
app.elasticsearch.index=${ELASTICSEARCH_INDEX:delivery-logs}

# RabbitMQ (use Spring AMQP auto-config)
spring.rabbitmq.host=${RABBITMQ_HOST:localhost}
spring.rabbitmq.port=${RABBITMQ_PORT:5672}
spring.rabbitmq.username=${RABBITMQ_USER:guest}
spring.rabbitmq.password=${RABBITMQ_PASS:guest}
```

### 3.7 Use BigDecimal for Monetary Values

```java
// BankAccount.java
@Entity
public class BankAccount {
    // ...
    @Column(precision = 19, scale = 4)
    private BigDecimal balance;
    // ...
}
```

---

## 4. Dependency Upgrade Recommendations

| Dependency | Current Version | Recommended Version | Reason |
|------------|----------------|---------------------|--------|
| `spring-boot-starter-parent` | 3.4.2 | **3.4.5+** | Fixes CVE-2025-22235 (actuator endpoint matcher). Compatible with Java 21. |
| `spring-boot-devtools` | (managed) | **REMOVE** | Security risk in production. Not needed for deployed apps. |
| `com.h2database:h2` | (managed ~2.2.x) | Keep (managed), **disable console** | Version managed by Spring Boot is safe, but console must be disabled. Migrate to PostgreSQL/MySQL for production. |
| `org.projectlombok:lombok` | 1.18.34 | **1.18.36** | Latest version, no CVEs but good practice to stay current. |
| `org.json:json` | 20240303 | **20250107** (latest) | Keep updated. No critical CVEs but follow latest releases. |
| `spring-boot-starter-security` | Not present | **ADD** | Mandatory for any production application. |
| `spring-boot-starter-validation` | Not present | **ADD** | Required for Bean Validation (`@Valid`, `@NotNull`, etc.). |
| Docker: `openjdk:21-ea-10-jdk-slim` | EA-10 | **`eclipse-temurin:21-jre-alpine`** | EA image has 78+ CVEs. Use stable GA JRE-only Alpine image. |
| Docker: `maven:3.9-eclipse-temurin-21` | 3.9 | Keep (build-stage only) | Acceptable for build stage; not included in runtime image. |
| Webpack/Node | Node v18.20.4, npm 10.7.0 | Keep | Acceptable for build-time tooling. |

### Recommended pom.xml changes

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.4.5</version>  <!-- upgrade from 3.4.2 -->
    <relativePath/>
</parent>

<dependencies>
    <!-- ADD: Security -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>

    <!-- ADD: Validation -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <!-- REMOVE: devtools -->
    <!-- <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-devtools</artifactId>
    </dependency> -->

    <!-- existing dependencies... -->
</dependencies>
```

---

## 5. Configuration Hardening Checklist

### Application Properties

- [ ] **Disable H2 console:** `spring.h2.console.enabled=false`
- [ ] **Externalize all credentials** to environment variables or secret manager
- [ ] **Configure HTTPS/TLS:** Add `server.ssl.*` properties or terminate at load balancer
- [ ] **Enable HSTS:** Via Spring Security headers configuration
- [ ] **Set server error handling:** `server.error.include-stacktrace=never`, `server.error.include-message=never`
- [ ] **Configure session:** `server.servlet.session.cookie.http-only=true`, `server.servlet.session.cookie.secure=true`, `server.servlet.session.cookie.same-site=strict`
- [ ] **Restrict actuator endpoints:** `management.endpoints.web.exposure.include=health,info` (if actuator is added)
- [ ] **Set production profile:** `spring.profiles.active=production`

### Spring Security

- [ ] **Add `spring-boot-starter-security` dependency**
- [ ] **Configure `SecurityFilterChain`** with least-privilege access
- [ ] **Enable CSRF protection** for stateful sessions (or disable for pure stateless API with JWT)
- [ ] **Configure CORS** with explicit allowed origins (not `*`)
- [ ] **Add method-level security** (`@PreAuthorize`) on service methods
- [ ] **Configure password encoder** (BCrypt or Argon2)
- [ ] **Implement rate limiting** on sensitive endpoints (login, transfer)

### Input Validation

- [ ] **Add `spring-boot-starter-validation` dependency**
- [ ] **Add `@Valid`** on all `@RequestBody` parameters
- [ ] **Add Bean Validation annotations** on all DTOs
- [ ] **Validate monetary amounts** (positive, max limit, BigDecimal)
- [ ] **Validate account numbers** (format, existence)
- [ ] **Add global `@ControllerAdvice`** exception handler (no stack traces in responses)

### Docker / Container

- [ ] **Replace Docker base image** with `eclipse-temurin:21-jre-alpine`
- [ ] **Add non-root user** (`USER app`)
- [ ] **Add `HEALTHCHECK`** instruction
- [ ] **Remove duplicate `COPY pom.xml`** line
- [ ] **Remove unnecessary `target/classes` copy**
- [ ] **Fix test skip flag** (`-DskipTests` not `-Dskiptest`)
- [ ] **Scan images** with Trivy/Grype in CI/CD

### Logging & Monitoring

- [ ] **Remove `System.out.println()`** -- use SLF4J logger
- [ ] **Remove `e.printStackTrace()`** -- use `logger.error(msg, e)`
- [ ] **Remove `/logmessage` endpoint** (log injection vector)
- [ ] **Externalize Elasticsearch config** (remove hardcoded IP)
- [ ] **Use HTTPS for Elasticsearch** connection
- [ ] **Sanitize log output** (no PII, no account numbers in info-level logs)
- [ ] **Remove SonarQube tokens** from `README.md` and rotate them

### Code Cleanup

- [ ] **Remove debug endpoints** (`/connect`, `/createzip`, `/logmessage`)
- [ ] **Remove unused imports** in `Components.java` (9 unused)
- [ ] **Remove commented-out code** across all files
- [ ] **Fix `@RequestMapping`** to use specific HTTP method annotations
- [ ] **Use `BigDecimal`** for all monetary fields
- [ ] **Add `@Transactional`** to `TransferServiceImpl.transfer()`
- [ ] **Fix sender/receiver map bug** in `TransferServiceImpl.java:42-43`

### Webpack / Frontend

- [ ] **Set `mode: 'production'`** in `webpack.config.js` for production builds
- [ ] **Disable source maps** (`devtool: false`) in production
- [ ] **Add CSP headers** for React SPA

---

## Appendix: Evidence References

### Finding #1 -- No Spring Security

**Evidence:** `pom.xml` -- no `spring-boot-starter-security` dependency present.

```xml
<!-- pom.xml lines 23-76: Full dependency list, no security starter -->
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-thymeleaf</artifactId>
    </dependency>
    <!-- ... no spring-boot-starter-security ... -->
</dependencies>
```

No `SecurityConfig`, `SecurityFilterChain`, `@EnableWebSecurity`, `@PreAuthorize`, or `@Secured` annotations found anywhere in the codebase.

### Finding #2 -- H2 Console Exposed

**Evidence:** `application.properties:7-8`

```properties
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

The H2 console is accessible at `http://<host>:8080/h2-console` with username `sa` and empty password.

### Finding #3 -- Path Traversal in createZip

**Evidence:** `BankAccountController.java:97-110`

```java
@GetMapping("/createzip")
public String createZip(@PathVariable String sourceDir, @PathVariable String zipFile) {
    // sourceDir and zipFile are user-controlled with NO sanitization
    File directory = new File(sourceDir);  // arbitrary filesystem access
    fileService.addFilesToZip(directory, directory.getName(), zos);
}
```

### Finding #5 -- Vulnerable Docker Image

**Evidence:** `Dockerfile:17`

```dockerfile
FROM openjdk:21-ea-10-jdk-slim
```

Snyk scan of `openjdk:21-ea-jdk-slim` shows: 1 Critical, 8 High, 14 Medium, 55 Low vulnerabilities.

### Finding #6 -- Log Injection

**Evidence:** `BankAccountController.java:61-64`

```java
@PostMapping("/logmessage")
public void saveLogs(@RequestParam String logmsg) {
    logger.info(logmsg);  // user input directly injected into logs
}
```

### Finding #10 -- Hardcoded Elasticsearch IP

**Evidence:** `InfoAppender.java:20-22`

```java
private static final String ELASTIC_SEARCH_API_HOST = "http://10.128.0.45:9200";
private static final String ELASTIC_SEARCH_INDEX_NAME = "delivery-logs";
private static final WebClient webClient = WebClient.create(ELASTIC_SEARCH_API_HOST);
```

### Finding #12 -- Transfer Logic Bugs

**Evidence:** `TransferServiceImpl.java:41-43`

```java
// BUG: receiverAccount put in BOTH entries (should be senderAccount on line 42)
accountMap.put("senderAccount", receiverAccount);   // WRONG - should be senderAccount
accountMap.put("receiverAccount", receiverAccount);  // correct
```

### Finding #23 -- SonarQube Tokens in README

**Evidence:** `README.md:32-34`

```
mvn clean verify sonar:sonar ... -Dsonar.login=sqp_1d0e5fc2db8d5607c3d2c149cfab971a5de52e6c
sonar-scanner.bat ... -Dsonar.login=sqp_32adab78b184c38dc10bc0cd674e86cf07528a7c
```

---

*Report generated by automated security analysis. Manual penetration testing is recommended to validate exploitability of identified findings.*

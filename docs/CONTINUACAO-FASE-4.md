# Continuação Minibank — Fase 4 (Partes 3 a 7)

Guia para retomar o estudo a partir de **MapStruct**, **Logs**, **Profiles**, **Flyway** e **refatoração final**.

**Pré-requisito (já feito ou quase):**
- `ApiResponse` nos controllers principais
- `BusinessException` + handler em `GlobalExceptionHandler`
- Status HTTP semânticos nos services

---

## PARTE 3 — Mapper (MapStruct)

### Objetivo

Tirar métodos como `responseDTO(Account account)` e `new UserResponseDTO(...)` dos services.

### Passo 3.1 — Dependências no `pom.xml`

Adicione nas `<properties>`:

```xml
<mapstruct.version>1.6.3</mapstruct.version>
<lombok-mapstruct-binding.version>0.2.0</lombok-mapstruct-binding.version>
```

Dependência:

```xml
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct</artifactId>
    <version>${mapstruct.version}</version>
</dependency>
```

No `maven-compiler-plugin`, em `annotationProcessorPaths`, **nesta ordem**:

1. `lombok`
2. `lombok-mapstruct-binding`
3. `mapstruct-processor`

Sem o binding, Lombok + MapStruct quebram no compile.

### Passo 3.2 — Primeiro mapper: Account

Crie: `account/mapper/AccountMapper.java`

```java
package com.example.minibank.account.mapper;

import com.example.minibank.account.dto.AccountResponseDTO;
import com.example.minibank.account.entity.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    @Mapping(source = "user.id", target = "userId")
    AccountResponseDTO toResponse(Account account);

    List<AccountResponseDTO> toResponseList(List<Account> accounts);
}
```

O Spring gera a implementação em `target/generated-sources`.

### Passo 3.3 — Usar no `AccountService`

- Injete: `private final AccountMapper accountMapper;`
- Substitua `responseDTO(account)` por `accountMapper.toResponse(account)`.
- Substitua `.map(this::responseDTO)` por `accountMapper::toResponse` ou `toResponseList`.
- Apague o método privado `responseDTO`.

### Passo 3.4 — UserMapper

`user/mapper/UserMapper.java`:

```java
@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponseDTO toResponse(User user);
    List<UserResponseDTO> toResponseList(List<User> users);
}
```

Refatore `UserService` da mesma forma.

**Não** mapeie `password` para response (`UserResponseDTO` já não tem — ok).

### Passo 3.5 — TransactionResponseDTO + TransactionMapper

1. Crie `transaction/dto/TransactionResponseDTO.java` com os campos da entidade (sem relações JPA).
2. `transaction/mapper/TransactionMapper.java`:

```java
@Mapper(componentModel = "spring")
public interface TransactionMapper {
    TransactionResponseDTO toResponse(Transaction transaction);
    List<TransactionResponseDTO> toResponseList(List<Transaction> transactions);
}
```

3. `TransactionService`: retorne DTOs, não entidades.
4. `TransactionController`:

```java
public ApiResponse<List<TransactionResponseDTO>> getStatement(...) {
    List<TransactionResponseDTO> data =
        transactionService.getStatement(accountId, type);
    return ApiResponse.success("Extrato obtido com sucesso", data);
}
```

### Passo 3.6 — Compilar e conferir

```bash
mvn clean compile
```

- Se der erro `"unknown property"`, ajuste `@Mapping` ou nomes dos campos.
- Implementação gerada fica em `target/generated-sources/annotations/`.

### Como testar — Parte 3

- Mesmos endpoints de antes; JSON de `data` igual ao que era.
- Extrato **não** deve expor estrutura interna da entidade JPA.

---

## PARTE 4 — Logs

### Objetivo

Rastrear o que aconteceu em produção sem vazar dados sensíveis.

### Passo 4.1 — Onde logar

| Camada            | Logar?                                      |
|-------------------|---------------------------------------------|
| Controller        | Só se necessário; prefira service           |
| Service           | Sim — operações de negócio                  |
| Repository        | Não (JPA já tem `show-sql` em dev)          |
| Exception handler | Sim — erros 500                             |

### Passo 4.2 — Adicionar `@Slf4j` nos services

Exemplo em `AccountService`:

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {
```

No início de métodos importantes:

```java
public AccountResponseDTO transfer(UUID from, UUID to, BigDecimal amount) {
    log.info("Transferência iniciada: from={}, to={}, amount={}", from, to, amount);
    // ... lógica ...
    log.info("Transferência concluída: from={}, to={}", from, to);
}
```

Em falhas esperadas (antes de lançar exceção):

```java
log.warn("Saque negado por saldo insuficiente: accountId={}", accountId);
throw new BusinessException(HttpStatus.BAD_REQUEST, "Saldo insuficiente");
```

### Passo 4.3 — AuthService

```java
log.info("Tentativa de login: email={}", request.getEmail());
// nunca logue a senha
```

### Passo 4.4 — Configuração de log (junto com profile — Parte 5)

Em `application-dev.properties`:

```properties
logging.level.com.example.minibank=DEBUG
logging.level.org.springframework.security=INFO
```

Em `application-prod.properties`:

```properties
logging.level.com.example.minibank=INFO
logging.level.org.springframework.security=WARN
```

### Como testar — Parte 4

1. Faça login, depósito, transferência.
2. No console deve aparecer `INFO` com IDs, **sem** senha/token completo.

---

## PARTE 5 — Profiles dev e prod

### Objetivo

Config diferente por ambiente; segredos fora do Git.

### Passo 5.1 — Reorganizar arquivos

Em `src/main/resources/`:

**`application.properties`** (só o comum):

```properties
spring.application.name=minibank
spring.profiles.active=dev
```

**`application-dev.properties`:**

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/minibank
spring.datasource.username=postgres
spring.datasource.password=9674

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
jwt.expiration=86400000
jwt.refresh-expiration=604800000

logging.level.com.example.minibank=DEBUG
```

**`application-prod.properties`:**

```properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}

spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false

jwt.secret=${JWT_SECRET}
jwt.expiration=${JWT_EXPIRATION:86400000}

logging.level.com.example.minibank=INFO
```

### Passo 5.2 — `.gitignore`

- Garanta que **não** commite senhas reais de prod.
- Em dev pode ficar local; em prod só variável de ambiente.
- Opcional: `application-dev-local.properties` no `.gitignore` para cada dev ter sua senha.

### Passo 5.3 — Rodar com profile

**IDE:** Active profiles = `dev`.

**Terminal:**

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

**Testar prod local (Windows):**

```cmd
set DB_URL=jdbc:postgresql://localhost:5432/minibank
set DB_USER=postgres
set DB_PASSWORD=9674
set JWT_SECRET=sua-chave-longa
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### Passo 5.4 — Depois do Flyway (Parte 6)

Em dev, quando Flyway estiver ativo:

```properties
spring.jpa.hibernate.ddl-auto=validate
spring.flyway.enabled=true
```

Hibernate só valida; Flyway cria/altera tabelas.

---

## PARTE 6 — Flyway (migrations)

### Objetivo

Versionar o banco com SQL versionado, como empresa real.

### Passo 6.1 — Dependência

No `pom.xml`:

```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-postgresql</artifactId>
</dependency>
```

(Spring Boot parent já gerencia versões compatíveis.)

### Passo 6.2 — Config

Em `application-dev.properties` (depois que tiver os SQLs):

```properties
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.jpa.hibernate.ddl-auto=validate
```

### Passo 6.3 — Criar migrations

Pasta: `src/main/resources/db/migration/`

**`V1__create_users_table.sql`:**

```sql
CREATE TABLE users (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL
);
```

**`V2__create_accounts_table.sql`:**

```sql
CREATE TABLE accounts (
    id UUID PRIMARY KEY,
    account_number VARCHAR(255) NOT NULL UNIQUE,
    balance NUMERIC(19, 2) NOT NULL,
    type VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMP,
    user_id UUID NOT NULL REFERENCES users(id)
);
```

**`V3__create_transactions_table.sql`:**

```sql
CREATE TABLE transactions (
    id UUID PRIMARY KEY,
    from_account_id UUID,
    to_account_id UUID,
    amount NUMERIC(19, 2) NOT NULL,
    type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP
);
```

Ajuste tipos se o Postgres já tiver schema diferente — exporte com `\d users` no `psql`.

### Passo 6.4 — Primeira subida com Flyway

1. **Backup** do banco se tiver dados importantes.
2. **Opção A (banco novo):** drop database `minibank`, create de novo.
3. **Opção B (banco existente):** `spring.flyway.baseline-on-migrate=true` **uma vez** se tabelas já existem.
4. Suba a app e veja no log: `Migrating schema ... to version 1`, `2`, `3`.

### Passo 6.5 — Regra de ouro

- Migration **aplicada** → **nunca** edite o arquivo.
- Nova coluna → `V4__add_phone_to_users.sql`:

```sql
ALTER TABLE users ADD COLUMN phone VARCHAR(20);
```

### Como testar — Parte 6

- [ ] Tabela `flyway_schema_history` existe no Postgres.
- [ ] App sobe sem erro de `validate`.
- [ ] `mvn test` passa (testes podem usar H2 + profile `test` depois, opcional).

---

## PARTE 7 — Refatoração final (checklist)

### 7.1 — Auth

- [ ] `ApiResponse` nos 3 endpoints
- [ ] `@Valid` nos DTOs
- [ ] Logs no `AuthService`
- [ ] Sem `RuntimeException` genérico

### 7.2 — User

- [ ] `UserMapper`
- [ ] `ApiResponse` no controller
- [ ] Email duplicado → `BusinessException(CONFLICT, ...)`
- [ ] `isUserReq` → `BusinessException(FORBIDDEN, ...)` (não 404)

### 7.3 — Account

- [ ] `AccountMapper`
- [ ] `ApiResponse` em todos endpoints
- [ ] Logs em deposit, withdraw, transfer, block
- [ ] Status corretos (saldo → `BAD_REQUEST`, conta bloqueada → `FORBIDDEN`)

### 7.4 — Transaction

- [ ] `TransactionResponseDTO` + mapper
- [ ] `ApiResponse` no extrato
- [ ] Extrair `isUserReq` duplicado → `AuthorizationService`

Sugestão:

```java
@Service
public class AuthorizationService {
    public void ensureUserOwnsResource(Authentication auth, UUID resourceUserId) {
        // mesma lógica do isUserReq com ADMIN
    }
}
```

### 7.5 — Pacotes

- [ ] Unificar `common` e `shared` (ex.: `common/exception`, `common/response`)
- [ ] README curto documentando formato da API (opcional, portfólio)

---

## Cronograma sugerido (7 dias)

| Dia | Foco | Entrega |
|-----|------|---------|
| 1 | Parte 1 — Auth + 1 endpoint Account | Postman mostra envelope |
| 2 | Parte 1 — resto controllers + Parte 2 validação | 400 com `errors` |
| 3 | Parte 2 — BusinessException + status HTTP | 409 email, 400 saldo |
| 4 | **Parte 3** — Account + User mapper | `mvn compile` ok |
| 5 | **Parte 3** Transaction + **Parte 4** logs | Extrato sem entidade |
| 6 | **Parte 5** profiles | dev/prod separados |
| 7 | **Parte 6** Flyway + **Parte 7** checklist | Banco versionado |

---

## Ordem se travar

1. `ApiResponse` só no Auth → testa → expande.
2. MapStruct só no Account → compila → User → Transaction.
3. Flyway **por último** — mexe no banco; faça quando API e erros estiverem estáveis.

---

## Comandos úteis

```bash
mvn clean compile          # após MapStruct
mvn test                   # após mudanças grandes
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

---

## Critério “Fase 4 concluída”

- [ ] Todos endpoints com `ApiResponse` no sucesso
- [ ] Erros via `BusinessException` + handlers (Security + validação + 500)
- [ ] Sem mapeamento manual repetido nos services (MapStruct)
- [ ] Logs em operações críticas
- [ ] `dev` / `prod` separados
- [ ] Schema só via Flyway

---

## GlobalExceptionHandler — lembrete

Manter **junto** com `BusinessException`:

- `BadCredentialsException` → 401
- `AccessDeniedException` → 403
- `ExpiredJwtException` → 401
- `MethodArgumentNotValidException` → 400 com `errors`
- `Exception` → 500 genérico (com `log.error`)

---

*Documento gerado para continuidade do estudo — minibank Fase 4.*

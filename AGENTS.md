# AGENTS.md

## Project Snapshot
- Stack: Java 17, Spring Boot 3.5.x, Maven (`mvnw.cmd`), Spring Data JPA, Spring Security, MapStruct, Lombok.
- Main entrypoint: `src/main/java/com/viettelsoftware/intern_management/InternManagementApplication.java`.
- Runtime config lives in `src/main/resources/config/application.yaml` (server port `8082`, PostgreSQL datasource via env vars).
- Test profile config is `src/test/resources/application-test.yml` (in-memory H2, `ddl-auto: create-drop`).
- Public/private API prefixes are config-driven: `${application.path.base.public}` and `${application.path.base.private}`.

## Architecture and Boundaries
- Package layering follows ports/adapters style:
- `infrastructure/primary/restful` = HTTP operators/controllers (example: `AuthOperator`).
- `domain/port/primary` = use-case interfaces (example: `AuthService`).
- `domain/service` = use-case implementations (example: `AuthServiceImpl`).
- `domain/port/secondary` = outbound contracts (example: `UserPort`, `PasswordPort`, `JwtProviderPort`).
- `infrastructure/secondary/jpa/adapter` = outbound implementations (example: `UserJpaAdapter`).
- `infrastructure/secondary/jpa/repository` = Spring Data repositories (example: `UserRepository`).
- Typical request flow: `AuthOperator` -> `AuthServiceImpl` -> secondary ports -> adapters/repositories.

## Security and Error Handling Conventions
- Security rules are centralized in `src/main/java/com/viettelsoftware/intern_management/wire/security/SecurityConfig.java`.
- `publicPath/**` is permit-all, `privatePath/**` requires authentication, sessions are stateless.
- JWT is handled by `JwtAuthenticationFilter` reading `Authorization: Bearer <token>`.
- `JwtUtil` creates an in-memory signing key at startup, so tokens are not stable across restarts.
- Domain auth failures throw `AuthException` with `AuthErrorKey` values.
- `GlobalExceptionHandler` returns HTTP 400 for `AuthException` and validation errors (map-shaped JSON).

## Data, Mapping, and Domain Patterns
- Entities use DTO mapping through MapStruct (`domain/mapper/BaseMapper`, `UserMapper`).
- JPA adapters commonly extend `BaseJPAAdapterVer2<...>` and expose DTO-based methods.
- `UserEntity` generates UUIDs in `@PrePersist` using `UuidCreator.getTimeOrderedEpoch()`.
- Audit fields (`createdAt`, `updatedAt`, `createdBy`, `updatedBy`) come from `BaseAuditing` + `CustomeAuditingEntityListener`.
- Shared package marker is present via `@SharedKernel` in `shared/enumeration/package-info.java`.

## Developer Workflows (Verified)
- Run tests: `./mvnw.cmd test -DskipITs` (verified passing locally: 7 tests).
- Build jar: `./mvnw.cmd clean package`.
- Run app locally: set `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, optional `SERVER_CTX_PATH`, then `./mvnw.cmd spring-boot:run`.
- Integration tests use MockMvc in `src/test/java/com/viettelsoftware/intern_management/infrastructure/primary/restful/AuthOperatorIntegrationTest.java`.
- Docker image build is multi-stage in `Dockerfile` (`mvn package -DskipTests` then run jar).
- Note: `Dockerfile` exposes `8080` while app config default is `8082`; align with env/runtime overrides when containerizing.

## Existing AI Guidance Files
- No existing AI-specific instruction files were found via the requested glob search (including `AGENTS.md`, `AGENT.md`, `CLAUDE.md`, `.cursorrules`, and `README.md`).


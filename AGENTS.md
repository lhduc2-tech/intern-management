# AGENTS.md

## Project Snapshot
- Stack: Java 17, Spring Boot 3.5.x, Maven (`mvnw.cmd`), Spring Data JPA, Spring Security, MapStruct, Lombok.
- Main entrypoint: `src/main/java/com/viettelsoftware/intern_management/InternManagementApplication.java`.
- Runtime config lives in `src/main/resources/config/application.yaml` (server port `8082`, PostgreSQL datasource via env vars).
- Test profile config is `src/test/resources/application-test.yml` (in-memory H2, `ddl-auto: create-drop`).
- Runtime JPA is `ddl-auto: none`, so production databases must already contain the mapped tables (for example `interns`); the test profile still builds schema from entities.
- Public/private API prefixes are config-driven: `${application.path.base.public}` and `${application.path.base.private}`.
- Spring virtual threads are enabled in runtime config (`spring.threads.virtual.enabled: true`).

## Architecture and Boundaries
- Package layering follows ports/adapters style:
- `infrastructure/primary/restful` = HTTP operators/controllers (example: `AuthOperator`).
- `domain/port/primary` = use-case interfaces (example: `AuthService`).
- `domain/service` = use-case implementations (example: `AuthServiceImpl`).
- `domain/port/secondary` = outbound contracts (example: `UserPort`, `PasswordPort`, `JwtProviderPort`).
- `infrastructure/secondary/jpa/adapter` = outbound implementations (example: `UserJpaAdapter`).
- `infrastructure/secondary/jpa/repository` = Spring Data repositories (example: `UserRepository`).
- `wire/database/DatabaseConfiguration.java` enables JPA auditing and resolves the current auditor from the authenticated principal map.
- `wire/WebConfig.java` enables Spring Data Web support, permissive CORS on `/**`, and `AcceptHeaderLocaleResolver`.
- `AuthOperator` keeps public auth endpoints under `${application.path.base.public}/auth`; `RolePermissionOperator` keeps RBAC endpoints under `${application.path.base.private}/rbac`, including admin-only `POST /roles` for supported role creation.
- `InternOperator` keeps private intern endpoints under `${application.path.base.private}/interns`; `POST /interns/search` is a paginated `searchInterns` flow with `InternSearchRequest` filters and `PageResponse`, and the flow is `InternOperator` -> `InternServiceImpl` -> `InternPort` -> `InternJpaAdapter` -> `InternRepository`.
- `RolePermissionOperator` and `InternOperator` map domain DTOs to dedicated REST response DTOs (`RoleResponse`, `PermissionResponse`, `InternResponse`) via private mapper methods in the operator layer.
- Typical request flow: `AuthOperator` -> `AuthServiceImpl` -> secondary ports -> adapters/repositories.
- Registration keeps the raw password in `UserDTO` only long enough for `AuthServiceImpl` to encode it, and supported roles are normalized/created there if missing.
- RBAC mutations are handled by `RolePermissionServiceImpl`, which uppercases role/permission codes, rejects unsupported or missing codes with `IllegalArgumentException`, and creates admin-managed roles with `status = ACTIVE`.
- Intern mutations are handled by `InternServiceImpl`, which defaults status to `ACTIVE`, auto-creates an `INTERN` user on `createIntern` (username derived from `fullName`, email `${username}@viettelsoftware.com`, default password `Intern@2026`), treats `userId`/`companyAccount`/`identityNumber` as unique business keys, and leaves `InternRequest` as a thin REST-layer DTO.

## Security and Error Handling Conventions
- Security rules are centralized in `src/main/java/com/viettelsoftware/intern_management/wire/security/SecurityConfig.java`.
- `publicPath/**` is permit-all, `privatePath/**` requires authentication, sessions are stateless.
- JWT is handled by `JwtAuthenticationFilter` reading `Authorization: Bearer <token>`.
- `JwtUtil` creates an in-memory signing key at startup, so tokens are not stable across restarts.
- `JwtAuthenticationFilter` accepts `roles`, `permissions`, and legacy `role` claims, builds a principal map with `username` and `userId`, and exposes authorities as `ROLE_<ROLE>` plus uppercased permission codes.
- Domain auth failures throw `AuthException` with `AuthErrorKey` values.
- `GlobalExceptionHandler` returns map-shaped JSON for `AuthException`, validation, malformed payload, data-integrity, and access-denied failures (400/403/500 as appropriate), and translates common username/email uniqueness violations into friendly messages.

## Data, Mapping, and Domain Patterns
- Entities use DTO mapping through MapStruct (`domain/mapper/BaseMapper`, `UserMapper`).
- JPA adapters commonly extend `BaseJPAAdapterVer2<...>` and expose DTO-based methods.
- `BaseMapper` is the shared MapStruct contract; concrete mappers such as `UserMapper` use `@Mapper(componentModel = "spring")` and derive `roleCodes`/`permissionCodes` from `RoleEntity` sets.
- Role management uses `RoleDTO` as the domain DTO contract (renamed from the older `RoleManagementDTO`).
- `UserEntity` generates UUIDs in `@PrePersist` using `UuidCreator.getTimeOrderedEpoch()`.
- `InternEntity` maps the legacy `interns` table and keeps FK-like relations as UUID columns (`userId`, `projectId`, `mentorId`) rather than entity associations in the current CRUD/search slice.
- Audit fields (`createdAt`, `updatedAt`, `createdBy`, `updatedBy`) come from `BaseAuditing` + `CustomeAuditingEntityListener`.
- Shared package marker is present via `@SharedKernel` in `shared/enumeration/package-info.java`.
- `DatabaseConfiguration` enables auditing from `SecurityContextHolder`; if no authenticated principal is present, audit fields fall back to `system`.

## Developer Workflows (Verified)
- Run tests: `./mvnw.cmd test -DskipITs` (verified passing locally: 26 tests, including `InternOperatorIntegrationTest` and `RolePermissionOperatorIntegrationTest`).
- Build jar: `./mvnw.cmd clean package`.
- Run app locally: set `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, optional `SERVER_CTX_PATH`, then `./mvnw.cmd spring-boot:run`.
- Integration tests use MockMvc in `src/test/java/com/viettelsoftware/intern_management/infrastructure/primary/restful/AuthOperatorIntegrationTest.java`, `RolePermissionOperatorIntegrationTest.java`, and `InternOperatorIntegrationTest.java`, all against the H2 `test` profile in `src/test/resources/application-test.yml`; the intern test covers filtered pagination on `POST /interns/search`, and RBAC tests cover admin-only role creation.
- Docker image build is multi-stage in `Dockerfile` (`mvn package -DskipTests` then run jar).
- Note: `Dockerfile` exposes `8080` while app config default is `8082`; align with env/runtime overrides when containerizing.

## Existing AI Guidance Files
- No existing AI-specific instruction files were found via the requested glob search (including `AGENTS.md`, `AGENT.md`, `CLAUDE.md`, `.cursorrules`, and `README.md`).


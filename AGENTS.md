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
- `UserOperator` keeps private user endpoints under `${application.path.base.private}/users`; it exposes admin-only search/get/update/delete flows and intentionally does not map `POST /users`.
- `InternOperator` keeps private intern endpoints under `${application.path.base.private}/interns`; `POST /interns/search` is a paginated `searchInterns` flow with `InternSearchRequest` filters and `PageResponse`, and the flow is `InternOperator` -> `InternServiceImpl` -> `InternPort` -> `InternJpaAdapter` -> `InternRepository`.
- `AttendanceOperator` keeps private attendance endpoints under `${application.path.base.private}/attendances`; search/get/create/update allow `ADMIN`, `HR`, `MENTOR`, `MANAGER`, and `INTERN`, while `DELETE /attendances/{id}` is limited to `ADMIN`, `HR`, and `MANAGER`.
- `ProjectOperator` keeps private project endpoints under `${application.path.base.private}/projects`; `POST /projects/search` is a paginated `searchProjects` flow with `ProjectSearchRequest` filters and `PageResponse`, and the flow is `ProjectOperator` -> `ProjectServiceImpl` -> `ProjectPort` -> `ProjectJpaAdapter` -> `ProjectRepository`.
- `TaskOperator` keeps private task endpoints under `${application.path.base.private}/tasks`; search/get/create/update allow `ADMIN`, `HR`, `MANAGER`, `MENTOR`, and `INTERN` (search/get only), while `POST /tasks` and `PUT /tasks/{id}` are limited to `ADMIN`, `HR`, `MANAGER`, and `MENTOR`, and `DELETE /tasks/{id}` is limited to `ADMIN`, `HR`, and `MANAGER`; `POST /tasks/search` is a paginated `searchTasks` flow with `TaskSearchRequest` filters and `PageResponse`, and the flow is `TaskOperator` -> `TaskServiceImpl` -> `TaskPort` -> `TaskJpaAdapter` -> `TaskRepository`.
- `RolePermissionOperator`, `UserOperator`, `AttendanceOperator`, `InternOperator`, `ProjectOperator`, and `TaskOperator` map domain DTOs to dedicated REST response DTOs (`RoleResponse`, `PermissionResponse`, `UserResponse`, `AttendanceResponse`, `InternResponse`, `ProjectResponse`, `TaskResponse`) via private mapper methods in the operator layer.
- Typical request flow: `AuthOperator` -> `AuthServiceImpl` -> secondary ports -> adapters/repositories.
- Registration keeps the raw password in `UserDTO` only long enough for `AuthServiceImpl` to encode it, and supported roles are normalized/created there if missing.
- RBAC mutations are handled by `RolePermissionServiceImpl`, which uppercases role/permission codes, rejects unsupported or missing codes with `IllegalArgumentException`, and creates admin-managed roles with `status = ACTIVE`.
- Intern mutations are handled by `InternServiceImpl`, which defaults status to `ACTIVE`, auto-creates an `INTERN` user on `createIntern` (username derived from `fullName`, email `${username}@viettelsoftware.com`, default password `Intern@2026`), treats `userId`/`companyAccount`/`identityNumber` as unique business keys, and leaves `InternRequest` as a thin REST-layer DTO.
- User mutations are handled by `UserServiceImpl`, which trims identifiers, uppercases status and role codes, requires at least one role on create, auto-creates missing roles with `status = ACTIVE`, and encodes raw passwords before persistence.
- Attendance mutations are handled by `AttendanceServiceImpl`, which requires `internId` and `date`, verifies the intern exists, trims notes, and rejects duplicate attendance rows for the same intern/date.
- Project mutations are handled by `ProjectServiceImpl`, which trims search name filters, requires non-blank `name` on create/update, trims optional `description`, and throws `IllegalArgumentException` when payloads are invalid or a project is missing.
- Task mutations are handled by `TaskServiceImpl`, which requires `projectId` on create, verifies the project exists, trims `title` and `description`, uppercases `priority` and `status` (defaults status to `TODO`), validates `startDate` is before or equal to `dueDate`, and throws `IllegalArgumentException` when payloads are invalid, tasks are missing, or project constraints are violated.

## Security and Error Handling Conventions
- Security rules are centralized in `src/main/java/com/viettelsoftware/intern_management/wire/security/SecurityConfig.java`.
- `publicPath/**` is permit-all, `privatePath/**` requires authentication, sessions are stateless.
- JWT is handled by `JwtAuthenticationFilter` reading `Authorization: Bearer <token>`.
- `JwtUtil` creates an in-memory signing key at startup, so tokens are not stable across restarts.
- `JwtAuthenticationFilter` accepts `roles`, `permissions`, and legacy `role` claims, builds a principal map with `username` and `userId`, and exposes authorities as `ROLE_<ROLE>` plus uppercased permission codes.
- Domain auth failures throw `AuthException` with `AuthErrorKey` values.
- `GlobalExceptionHandler` returns map-shaped JSON for `AuthException`, validation, malformed payload, data-integrity, and access-denied failures (400/403/500 as appropriate), and translates common username/email uniqueness violations into friendly messages.
- `UserServiceImpl`, `AttendanceServiceImpl`, `ProjectServiceImpl`, and `TaskServiceImpl` use `IllegalArgumentException` for business-rule failures, which `GlobalExceptionHandler` maps to `400 INVALID_ARGUMENT`.

## Data, Mapping, and Domain Patterns
- Entities use DTO mapping through MapStruct (`domain/mapper/BaseMapper`, `UserMapper`, `AttendanceMapper`, `ProjectMapper`, `TaskMapper`).
- JPA adapters commonly extend `BaseJPAAdapterVer2<...>` and expose DTO-based methods.
- `BaseMapper` is the shared MapStruct contract; concrete mappers such as `UserMapper`, `AttendanceMapper`, `ProjectMapper`, and `TaskMapper` use `@Mapper(componentModel = "spring")`, and `UserMapper` derives `roleCodes`/`permissionCodes` from `RoleEntity` sets.
- Role management uses `RoleDTO` as the domain DTO contract (renamed from the older `RoleManagementDTO`).
- `UserEntity` generates UUIDs in `@PrePersist` using `UuidCreator.getTimeOrderedEpoch()`.
- `InternEntity` maps the legacy `interns` table and keeps FK-like relations as UUID columns (`userId`, `projectId`, `mentorId`) rather than entity associations in the current CRUD/search slice.
- `ProjectEntity` maps the `projects` table (`name` required, `description` text) and generates UUIDs in `@PrePersist` using `UuidCreator.getTimeOrderedEpoch()`.
- Audit fields (`createdAt`, `updatedAt`, `createdBy`, `updatedBy`) come from `BaseAuditing` + `CustomeAuditingEntityListener`.
- Shared package marker is present via `@SharedKernel` in `shared/enumeration/package-info.java`.
- `DatabaseConfiguration` enables auditing from `SecurityContextHolder`; if no authenticated principal is present, audit fields fall back to `system`.

## Developer Workflows (Verified)
- Run tests: `./mvnw.cmd test -DskipITs` (current surefire reports under `target/surefire-reports` include `AttendanceOperatorIntegrationTest`, `InternOperatorIntegrationTest`, `ProjectOperatorIntegrationTest`, `RolePermissionOperatorIntegrationTest`, `UserOperatorIntegrationTest`, and `TaskOperatorIntegrationTest`).
- Build jar: `./mvnw.cmd clean package`.
- Run app locally: set `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, optional `SERVER_CTX_PATH`, then `./mvnw.cmd spring-boot:run`.
- Integration tests use MockMvc in `src/test/java/com/viettelsoftware/intern_management/infrastructure/primary/restful/AuthOperatorIntegrationTest.java`, `AttendanceOperatorIntegrationTest.java`, `InternOperatorIntegrationTest.java`, `ProjectOperatorIntegrationTest.java`, `RolePermissionOperatorIntegrationTest.java`, `UserOperatorIntegrationTest.java`, and `TaskOperatorIntegrationTest.java`, all against the H2 `test` profile in `src/test/resources/application-test.yml`; the intern test covers filtered pagination on `POST /interns/search`, the attendance test covers CRUD plus duplicate intern/date rejection, project tests cover CRUD + filtered search + forbidden create for `INTERN`, the user test covers admin-only CRUD over `/users`, RBAC tests cover admin-only role creation, and task tests cover CRUD + filtered search + date validation + permission boundaries.
- Docker image build is multi-stage in `Dockerfile` (`mvn package -DskipTests` then run jar).
- Note: `Dockerfile` exposes `8080` while app config default is `8082`; align with env/runtime overrides when containerizing.

## Existing AI Guidance Files
- Single-file result from the requested glob search: `AGENTS.md` at project root; no additional files matched (`.github/copilot-instructions.md`, `AGENT.md`, `CLAUDE.md`, `.cursorrules`, `.windsurfrules`, `.clinerules`, `.cursor/rules/**`, `.windsurf/rules/**`, `.clinerules/**`, `README.md`).

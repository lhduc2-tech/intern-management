# AGENTS.md

## Project Snapshot
- Stack: Java 17, Spring Boot 3.5.x, Maven (`mvnw.cmd`), Spring Data JPA, Spring Security, MapStruct, Lombok, Springdoc OpenAPI.
- Main entrypoint: `src/main/java/com/viettelsoftware/intern_management/InternManagementApplication.java`.
- Runtime config lives in `src/main/resources/config/application.yaml` (server port `8082`, PostgreSQL datasource via env vars).
- Test profile config is `src/test/resources/application-test.yml` (in-memory H2, `ddl-auto: create-drop`).
- Runtime JPA is `ddl-auto: none`, so production databases must already contain the mapped tables (for example `interns`); the test profile still builds schema from entities.
- Public/private API prefixes are config-driven: `${application.path.base.public}` and `${application.path.base.private}`.
- Spring virtual threads are enabled in runtime config (`spring.threads.virtual.enabled: true`).
- `InternManagementApplication.java` excludes `UserDetailsServiceAutoConfiguration` and logs startup traces via `ApplicationStartupTraces.of(env)` when info logging is enabled.
- API docs are served by Springdoc at `/v3/api-docs` and `/swagger-ui.html` (toggle with `SWAGGER_ENABLED`, default `true`), and `wire/openapi/OpenApiConfiguration.java` registers a global `bearerAuth` JWT security scheme.

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
- `AuthOperator` keeps public auth endpoints under `${application.path.base.public}/auth`; `RolePermissionOperator` keeps RBAC endpoints under `${application.path.base.private}/rbac`, including authenticated `GET /roles`/`GET /roles/{roleCode}`/`GET /permissions`, admin-only `POST /roles`/`POST /permissions`/`PUT /permissions/{permissionCode}`, and admin-only `PUT /roles/{roleCode}/permissions` for role-permission assignment.
- `UserOperator` keeps private user endpoints under `${application.path.base.private}/users`; it exposes admin-only search/get/update/delete flows and intentionally does not map `POST /users`.
- `InternOperator` keeps private intern endpoints under `${application.path.base.private}/interns`; `POST /interns/search` is a paginated `searchInterns` flow with `InternSearchRequest` filters and `PageResponse`, and the flow is `InternOperator` -> `InternServiceImpl` -> `InternPort` -> `InternJpaAdapter` -> `InternRepository`.
- `AttendanceOperator` keeps private attendance endpoints under `${application.path.base.private}/attendances`; search/get/create/update allow `ADMIN`, `HR`, `MENTOR`, `MANAGER`, and `INTERN`, while `DELETE /attendances/{id}` is limited to `ADMIN`, `HR`, and `MANAGER`.
- `ProjectOperator` keeps private project endpoints under `${application.path.base.private}/projects`; `POST /projects/search` is a paginated `searchProjects` flow with `ProjectSearchRequest` filters and `PageResponse`, and the flow is `ProjectOperator` -> `ProjectServiceImpl` -> `ProjectPort` -> `ProjectJpaAdapter` -> `ProjectRepository`.
- `TaskOperator` keeps private task endpoints under `${application.path.base.private}/tasks`; search/get/create/update allow `ADMIN`, `HR`, `MANAGER`, `MENTOR`, and `INTERN` (search/get only), while `POST /tasks` and `PUT /tasks/{id}` are limited to `ADMIN`, `HR`, `MANAGER`, and `MENTOR`, and `DELETE /tasks/{id}` is limited to `ADMIN`, `HR`, and `MANAGER`; `POST /tasks/search` is a paginated `searchTasks` flow with `TaskSearchRequest` filters and `PageResponse`, and the flow is `TaskOperator` -> `TaskServiceImpl` -> `TaskPort` -> `TaskJpaAdapter` -> `TaskRepository`.
- `TaskAssignmentOperator` keeps private task assignment endpoints under `${application.path.base.private}/task-assignments`; search/get allow `ADMIN`, `HR`, `MANAGER`, `MENTOR`, and `INTERN`, while `POST /task-assignments` and `PUT /task-assignments/{id}` are limited to `ADMIN`, `HR`, `MANAGER`, and `MENTOR`, and `DELETE /task-assignments/{id}` is limited to `ADMIN`, `HR`, and `MANAGER`; `POST /task-assignments/search` is a paginated `searchTaskAssignments` flow with `TaskAssignmentSearchRequest` filters and `PageResponse`, and the flow is `TaskAssignmentOperator` -> `TaskAssignmentServiceImpl` -> `TaskAssignmentPort` -> `TaskAssignmentJpaAdapter` -> `TaskAssignmentRepository`.
- `InternshipSessionOperator` keeps private internship session endpoints under `${application.path.base.private}/internship-sessions`; `POST /internship-sessions/search` is a paginated `searchInternshipSessions` flow with `InternshipSessionSearchRequest` filters and `PageResponse`, and the flow is `InternshipSessionOperator` -> `InternshipSessionServiceImpl` -> `InternshipSessionPort` -> `InternshipSessionJpaAdapter` -> `InternshipSessionRepository`.
- `RolePermissionOperator`, `UserOperator`, `AttendanceOperator`, `InternOperator`, `ProjectOperator`, `TaskOperator`, `TaskAssignmentOperator`, and `InternshipSessionOperator` map domain DTOs to dedicated REST response DTOs (`RoleResponse`, `PermissionResponse`, `UserResponse`, `AttendanceResponse`, `InternResponse`, `ProjectResponse`, `TaskResponse`, `TaskAssignmentResponse`, `InternshipSessionResponse`) via private mapper methods in the operator layer.
- Typical request flow: `AuthOperator` -> `AuthServiceImpl` -> secondary ports -> adapters/repositories.
- Registration keeps the raw password in `UserDTO` only long enough for `AuthServiceImpl` to encode it, and supported roles are normalized/created there if missing.
- RBAC mutations are handled by `RolePermissionServiceImpl`, which uppercases role/permission codes, rejects unsupported or missing codes with `IllegalArgumentException`, and creates admin-managed roles with `status = ACTIVE`.
- Intern mutations are handled by `InternServiceImpl`, which defaults status to `ACTIVE`, auto-creates an `INTERN` user on `createIntern` (username derived from `fullName`, email `${username}@viettelsoftware.com`, default password `Intern@2026`), treats `userId`/`companyAccount`/`identityNumber` as unique business keys, validates `internshipSessionId` exists when provided, and leaves `InternRequest` as a thin REST-layer DTO.
- Internship session mutations are handled by `InternshipSessionServiceImpl`, which trims search-name filters, requires non-blank `name` on create/update, validates `startFrom <= startTo` and `endFrom <= endTo` in search, validates `startDate <= endDate` on create/update, and throws `IllegalArgumentException` when payloads are invalid or a session is missing.
- User mutations are handled by `UserServiceImpl`, which trims identifiers, uppercases status and role codes, requires at least one role on create, auto-creates missing roles with `status = ACTIVE`, and encodes raw passwords before persistence.
- Attendance mutations are handled by `AttendanceServiceImpl`, which requires `internId` and `date`, verifies the intern exists, trims notes, and rejects duplicate attendance rows for the same intern/date.
- Project mutations are handled by `ProjectServiceImpl`, which trims search name filters, requires non-blank `name` on create/update, trims optional `description`, and throws `IllegalArgumentException` when payloads are invalid or a project is missing.
- Task mutations are handled by `TaskServiceImpl`, which requires `projectId` on create, verifies the project exists, trims `title` and `description`, uppercases `priority` and `status` (defaults status to `TODO`), validates `startDate` is before or equal to `dueDate`, and throws `IllegalArgumentException` when payloads are invalid, tasks are missing, or project constraints are violated.
- Task assignment mutations are handled by `TaskAssignmentServiceImpl`, which requires `taskId` and `internId` on create/update, verifies both task and intern exist, defaults `assignedAt` to current time if omitted, enforces unique task-intern pairs, and throws `IllegalArgumentException` when payloads are invalid, required entities are missing, or a duplicate assignment already exists.

## Security and Error Handling Conventions
- Security rules are centralized in `src/main/java/com/viettelsoftware/intern_management/wire/security/SecurityConfig.java`.
- `SecurityConfig` explicitly permits `/v3/api-docs/**`, `/swagger-ui/**`, and `/swagger-ui.html` in addition to `publicPath/**`.
- `publicPath/**` is permit-all, `privatePath/**` requires authentication, sessions are stateless.
- JWT is handled by `JwtAuthenticationFilter` reading `Authorization: Bearer <token>`.
- `JwtUtil` signs tokens with `app.jwt.secret` (must be at least 32 bytes for HS256); startup fails fast when the secret is missing/too short.
- `JwtAuthenticationFilter` accepts `roles`, `permissions`, and legacy `role` claims, builds a principal map with `username`, `userId`, and `fullName`, and exposes authorities as `ROLE_<ROLE>` plus uppercased permission codes.
- Domain auth failures throw `AuthException` with `AuthErrorKey` values.
- `GlobalExceptionHandler` returns map-shaped JSON for `AuthException`, validation, malformed payload, data-integrity, and access-denied failures (400/403/500 as appropriate), and translates common username/email uniqueness violations into friendly messages.
- `UserServiceImpl`, `InternServiceImpl`, `AttendanceServiceImpl`, `ProjectServiceImpl`, `TaskServiceImpl`, `TaskAssignmentServiceImpl`, and `InternshipSessionServiceImpl` use `IllegalArgumentException` for business-rule failures, which `GlobalExceptionHandler` maps to `400 INVALID_ARGUMENT`.

## Data, Mapping, and Domain Patterns
- Entities use DTO mapping through MapStruct (`domain/mapper/BaseMapper`, `UserMapper`, `InternMapper`, `AttendanceMapper`, `ProjectMapper`, `TaskMapper`, `TaskAssignmentMapper`, `InternshipSessionMapper`, `RoleMapper`, `PermissionMapper`).
- JPA adapters commonly extend `BaseJPAAdapterVer2<...>` and expose DTO-based methods.
- `BaseMapper` is the shared MapStruct contract; concrete mappers such as `UserMapper`, `AttendanceMapper`, `ProjectMapper`, and `TaskMapper` use `@Mapper(componentModel = "spring")`, and `UserMapper` derives `roleCodes`/`permissionCodes` from `RoleEntity` sets.
- Role management uses `RoleDTO` as the domain DTO contract (renamed from the older `RoleManagementDTO`).
- Search request DTOs such as `InternSearchRequest`, `ProjectSearchRequest`, `TaskSearchRequest`, `TaskAssignmentSearchRequest`, `AttendanceSearchRequest`, `UserSearchRequest`, and `InternshipSessionSearchRequest` extend `BaseRequest`, which centralizes `pageIndex`/`pageSize`/`sort`/`direction` and defaults invalid paging to `Constant.DEFAULT_PAGE = 0` and `Constant.DEFAULT_SIZE = 10`; `CreatePermissionRequest` and `UpdatePermissionRequest` share `BasePermissionRequest` for the `description` field.
- `UserEntity` generates UUIDs in `@PrePersist` using `UuidCreator.getTimeOrderedEpoch()`.
- `InternEntity` maps the legacy `interns` table and keeps FK-like relations as UUID columns (`userId`, `projectId`, `internshipSessionId`, `mentorId`) rather than entity associations in the current CRUD/search slice.
- `ProjectEntity` maps the `projects` table (`name` required, `description` text) and generates UUIDs in `@PrePersist` using `UuidCreator.getTimeOrderedEpoch()`.
- `InternshipSessionEntity` maps the `internship_session` table (`name`, `startDate`, `endDate`, `totalIntern`) and generates UUIDs in `@PrePersist` using `UuidCreator.getTimeOrderedEpoch()`.
- Audit fields (`createdAt`, `updatedAt`, `createdBy`, `updatedBy`) come from `BaseAuditing` + `CustomeAuditingEntityListener`.
- Shared package marker is present via `@SharedKernel` in `shared/enumeration/package-info.java`.
- `DatabaseConfiguration` enables auditing from `SecurityContextHolder`; if no authenticated principal is present, audit fields fall back to `system`.

## Developer Workflows (Verified)
- Run tests: `./mvnw.cmd test -DskipITs` (current surefire reports under `target/surefire-reports` include `AuthOperatorIntegrationTest`, `AttendanceOperatorIntegrationTest`, `InternOperatorIntegrationTest`, `InternshipSessionOperatorIntegrationTest`, `ProjectOperatorIntegrationTest`, `RolePermissionOperatorIntegrationTest`, `UserOperatorIntegrationTest`, `TaskOperatorIntegrationTest`, `TaskAssignmentOperatorIntegrationTest`, and `SwaggerIntegrationTest`).
- Build jar: `./mvnw.cmd clean package`.
- Run app locally: set `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET` (>= 32 bytes), optional `JWT_EXPIRATION_MS` and `SERVER_CTX_PATH`, then `./mvnw.cmd spring-boot:run`.
- Integration tests use MockMvc in `src/test/java/com/viettelsoftware/intern_management/infrastructure/primary/restful/AuthOperatorIntegrationTest.java`, `AttendanceOperatorIntegrationTest.java`, `InternOperatorIntegrationTest.java`, `InternshipSessionOperatorIntegrationTest.java`, `ProjectOperatorIntegrationTest.java`, `RolePermissionOperatorIntegrationTest.java`, `UserOperatorIntegrationTest.java`, `TaskOperatorIntegrationTest.java`, `TaskAssignmentOperatorIntegrationTest.java`, and `SwaggerIntegrationTest.java`, all against the H2 `test` profile in `src/test/resources/application-test.yml`; the intern test covers filtered pagination on `POST /interns/search`, the internship session test covers CRUD + filtered search + forbidden create for `INTERN`, the attendance test covers CRUD plus duplicate intern/date rejection, project tests cover CRUD + filtered search + forbidden create for `INTERN`, the user test covers admin-only CRUD over `/users`, RBAC tests cover admin-only role creation, permission CRUD, role-permission assignment, and authenticated reads of roles/permissions, task tests cover CRUD + filtered search + date validation + permission boundaries, task assignment tests cover CRUD + filtered search + duplicate task-intern rejection + permission boundaries, and the Swagger test verifies `/v3/api-docs` plus `/swagger-ui/index.html` accessibility.
- Docker image build is multi-stage in `Dockerfile` (`mvn package -DskipTests` then run jar).
- Note: `Dockerfile` exposes `8080` while app config default is `8082`; align with env/runtime overrides when containerizing.

## Existing AI Guidance Files
- Single-file result from the requested glob search: `AGENTS.md` at project root; no additional files matched (`.github/copilot-instructions.md`, `AGENT.md`, `CLAUDE.md`, `.cursorrules`, `.windsurfrules`, `.clinerules`, `.cursor/rules/**`, `.windsurf/rules/**`, `.clinerules/**`, `README.md`).

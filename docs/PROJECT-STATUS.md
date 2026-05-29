# Project Status

> Last updated: 2026-03-02 | By: @hoidanit | Session: #3
>
> AI: update this file at the end of every session when asked.
> Follow this exact format. Keep it concise — under 80 lines.

---

## Completed
- ✅ Project skeleton (Spring Boot 4, Maven, application.yml)
- ✅ Documentation setup (CLAUDE.md, PROJECT-RULES, ARCHITECTURE, DATABASE, API_SPEC)
- ✅ ADR-001: Refresh token strategy decided (Cookie + Body)
- ✅ ADR-002: File upload strategy decided (Local Storage + Static Resource Serving)
- ✅ AI workflow setup (.claude/commands/)
- ✅ Base exception classes (ResourceNotFoundException, DuplicateResourceException)
- ✅ GlobalExceptionHandler (@RestControllerAdvice)
- ✅ ApiResponse wrapper (class with factory methods)
- ✅ SecurityConfig (permitAll on `/`, `/auth/**`; JWT enforced on rest)
- ✅ JwtConfig (JwtEncoder, JwtDecoder — ready, enforced)
- ✅ User CRUD (early-stage, no DTOs — to be refactored in Phase 3)
- ✅ Auth login endpoint (JWT token generation)
- ✅ **[2026-03-01]** Permission CRUD — entity, repository, DTOs (records), service, controller
- ✅ **[2026-03-01]** Permission unit tests — 11 tests, all passing
- ✅ **[2026-03-01]** Permission integration tests — 11 tests (requires MySQL)
- ✅ **[2026-03-01]** Permission CONTEXT.md
- ✅ **[2026-03-01]** Company CRUD — entity, repository, DTOs (records), service, controller
- ✅ **[2026-03-01]** Company unit tests — 11 tests, all passing
- ✅ **[2026-03-01]** Company integration tests — 13 tests (requires MySQL)
- ✅ **[2026-03-02]** Role CRUD — entity (ManyToMany Permission), repository, DTOs (records), service, controller
- ✅ **[2026-03-02]** Role unit tests — 14 tests
- ✅ **[2026-03-02]** Role integration tests — 13 tests (requires MySQL)
- ✅ **[2026-03-02]** Role CONTEXT.md
- ✅ **[2026-03-02]** User CRUD refactor — entity (ManyToOne Company, ManyToMany Role), DTOs (records), service, controller (pagination)
- ✅ **[2026-03-02]** User unit tests — 13 tests
- ✅ **[2026-03-02]** User integration tests — 16 tests (requires MySQL)
- ✅ **[2026-03-02]** User CONTEXT.md

## In Progress
_Nothing._

## Deferred Issues
- **[P2]** `module` field has no enum validation — accepts any string silently.
  Defer to Phase 7 when RBAC middleware is built.

## Warnings
- ⚠️ **Spring Boot 4 / Jackson 3**: `ObjectMapper` is now `tools.jackson.databind.ObjectMapper`
  (not `com.fasterxml.jackson.databind`). Update all test imports accordingly.
- ⚠️ **Spring Boot 4 / MockMvc**: `@AutoConfigureMockMvc` moved to
  `org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc`.
- ⚠️ Integration tests require a live MySQL at `localhost:3306/hr_management` — CI must
  provision a MySQL container. H2 is banned per PROJECT-RULES.md.
## Next Tasks
1. **[P1]** Auth endpoints — POST /auth/login, POST /auth/register, GET /auth/me (Phase 4)

## Milestones

### Phase 0 — Foundation
- [x] Base exception classes
- [x] GlobalExceptionHandler
- [x] ApiResponse wrapper
- [x] SecurityConfig
- [x] JwtConfig

### Phase 1 — Independent Entities
- [x] Permission CRUD + unit test + integration test + CONTEXT.md
- [x] Company CRUD + unit test + integration test

### Phase 2 — Role (depends on Permission)
- [x] Role CRUD + ManyToMany Permission + test + CONTEXT.md

### Phase 3 — User (depends on Role + Company)
- [x] User CRUD + ManyToOne Company + ManyToMany Role + test + CONTEXT.md

### Phase 4 — Authentication
- [ ] CustomUserDetailsService
- [ ] POST /auth/login + POST /auth/register + test
- [ ] Enable JWT enforce in SecurityConfig
- [ ] GET /auth/me + test

### Phase 5 — Refresh Token (ADR-001)
- [ ] RefreshToken entity + repository
- [ ] POST /auth/refresh + POST /auth/logout + test

### Phase 6 — File Upload (ADR-002)
- [ ] StorageService + POST /api/v1/files/upload + validation + test

### Phase 7 — RBAC
- [ ] Middleware: path + method → Permission → Role → allow/deny
- [ ] Integrate into SecurityFilterChain + 401/403 tests

### Phase 8 — Polish
- [ ] Pagination + sorting for all list endpoints
- [ ] Search / filter
- [ ] Scheduled job: cleanup expired refresh tokens
- [ ] Full review (/review-pr) + final docs update

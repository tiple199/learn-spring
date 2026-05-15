# Permission — Implementation Context
> Written: 2026-03-01 | Author: @hoidanit

## Business Context
Permission is the lowest-level building block of the RBAC system. It maps a specific HTTP
method + API path combination to a named action (e.g., `POST /api/v1/users` → `CREATE_USER`).
Roles aggregate permissions; users are assigned roles — so nothing else in the auth system
can work until this module exists.

## Technical Decisions
- **Java records for DTOs**: Introduced as the new standard for this project going forward.
  Request records use Jakarta validation on components; response record uses `static fromEntity()`
  factory. Existing `auth/` and `user/` modules still use POJOs — this is intentional divergence.
- **`(api_path, method)` as unique key**: Two permissions with the same path and method would
  be ambiguous at authorization time. Enforced at DB level (`@UniqueConstraint`) and at service
  level (`existsByApiPathAndMethod` before save).
- **`method` stored as plain String**: Kept flexible (no enum) so the RBAC middleware in Phase 7
  can match incoming `HttpServletRequest.getMethod()` directly without conversion.
- **`@PrePersist` / `@PreUpdate` for timestamps**: Avoids Hibernate-specific annotations
  (`@CreationTimestamp`) and keeps the entity portable. Consistent with the approach used
  for all new entities going forward.
- **PUT without path variable** (`PUT /api/v1/permissions`): The `id` is in the request body
  per API_SPEC.md convention — keeps update payload self-contained and consistent with role/user
  update endpoints.

## Considered and Rejected
- **Enum for `method` field**: Rejected — would require conversion from `HttpServletRequest`
  method string at RBAC check time; plain String is simpler and equally safe with DB constraint.
- **`@CreationTimestamp` / `@UpdateTimestamp`**: Rejected in favour of `@PrePersist`/`@PreUpdate`
  to avoid Hibernate-vendor lock-in and for explicitness.

## Dependencies
- Depends on: nothing (fully independent — lowest node in the dependency graph)
- Depended by: `role` (ManyToMany via `permission_role` join table — Phase 2)

## Known Limitations
- ⚠️ No `module` value validation — accepted string like `"INVALID_MODULE"` will persist silently.
  Intentional: module is a UI/grouping hint only; strict validation can be added in Phase 7 if needed.
- ⚠️ No sorting on `GET /api/v1/permissions` — returns DB insertion order within the page.
  Sorting will be added globally in Phase 8 polish.
- ⚠️ Integration tests require a running MySQL instance at `localhost:3306/hr_management`.
  This is by design (PROJECT-RULES.md bans H2). CI must provision a MySQL container.

## Refactor Log
_No changes yet._

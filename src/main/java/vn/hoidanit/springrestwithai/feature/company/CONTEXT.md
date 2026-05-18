# Company — Implementation Context
> Written: 2026-03-01 | Author: @hoidanit

## Business Context
Represents an employer organization in the HR system. Every user (employee) belongs
to one company, and every job listing is posted under a company. This module provides
the full lifecycle management (create, read, update, delete) for company records.

## Technical Decisions
- **Unique constraint on `name` via `@Table(uniqueConstraints = ...)`**: Company names
  must be globally unique to avoid ambiguity when employees are assigned to a company.
  Used `@Table(uniqueConstraints)` (not `@Column(unique = true)`) to stay consistent
  with the `Permission` entity pattern already in the codebase.
- **`@Column(columnDefinition = "TEXT")` on `description`**: DATABASE.md specifies
  TEXT type. Without this annotation Hibernate defaults to VARCHAR(255), which would
  silently truncate long company descriptions.
- **`@Column(length = 500)` on `address` and `logo`**: Matches DATABASE.md VARCHAR(500)
  spec. A URL or full address can exceed the default 255 limit.
- **No `@OneToMany users` on the entity**: The `User → Company` FK lives on the User
  side. Adding the inverse collection now would pull in the User entity as a compile-time
  dependency, coupling two independent Phase 1 modules. The relationship will be added
  in Phase 3 when User is refactored.
- **Duplicate check before save (not after)**: `existsByName()` is called before
  `companyRepository.save()` to avoid relying on DB constraint violations, which are
  harder to translate into meaningful API error messages.
- **`existsById` in `delete()`** (not `findById`): No need to load the full entity into
  memory just to delete it. A plain existence check is sufficient and cheaper.

## Considered and Rejected
- **`@Column(unique = true)` on `name`**: Functionally equivalent but inconsistent with
  `Permission.java` which uses `@Table(uniqueConstraints)`. Rejected for consistency.
- **`@NotBlank` on optional fields (`description`, `address`, `logo`)**: Rejected
  because these are genuinely optional — a company may not have a logo yet or a known
  address. `@NotBlank` would also reject `null`, breaking partial creates.
- **Returning raw entity from controller**: Rejected — controller always returns DTOs
  per PROJECT-RULES.md to keep API shape stable and decouple internal fields.

## Dependencies
- **Depends on**: none — Company is an independent root entity in Phase 1.
- **Depended by**: `User` (Phase 3 — `ManyToOne Company`), `Job` (future phases).

## Known Limitations
- ⚠️ **No `List<User> users` on `Company`**: The inverse side of the `User → Company`
  relationship is intentionally omitted. Adding it now would create a circular dependency
  with the User module (Phase 1). Will be added in Phase 3 alongside User refactoring.
- ⚠️ **No search / filter on `getAll`**: List endpoint returns all companies paginated
  with no name-based filtering. Deferred to Phase 8 (Polish) per PROJECT-STATUS.md.
- ⚠️ **`logo` field stores a path/URL string only**: File upload (actual binary) is a
  separate concern handled by the File Upload feature (Phase 6, ADR-002). This field
  just stores the resulting path returned by the upload endpoint.

## Refactor Log
(No changes yet — add entries here when the module is modified.)

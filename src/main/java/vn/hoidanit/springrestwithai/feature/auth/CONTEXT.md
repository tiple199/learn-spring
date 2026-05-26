# Auth — Implementation Context
> Written: 2026-03-04 | Author: AI-assisted

## Business Context
Provides user registration, login, token refresh, logout, and "get current user"
endpoints. This is the security backbone of the HR Management System — every
protected API call depends on JWT tokens issued here.

## Technical Decisions
- **HS256 JWT via oauth2-resource-server**: leverages Spring Security's built-in
  JWT decoder/encoder infrastructure instead of a custom filter chain, reducing
  boilerplate and aligning with Spring Boot 4 conventions.
- **Refresh tokens stored as SHA-256 hashes**: raw JWT is never persisted; only
  its hex digest is stored. Prevents token exposure if the database is compromised.
- **Dual refresh-token delivery (cookie + body)**: the refresh token is returned
  both in an `HttpOnly; Secure; SameSite=Lax` cookie and in the JSON response
  body. Browser clients use the cookie automatically; mobile/API clients use the
  body. The `/refresh` endpoint checks cookie first, then falls back to body.
- **Token rotation on refresh**: old refresh token is revoked, new pair issued.
  Prevents replay attacks with stolen refresh tokens.
- **Multi-device support**: each login creates a separate `RefreshToken` row with
  `deviceInfo` and `ipAddress`, allowing a user to be logged in on multiple
  devices simultaneously.
- **No auto-login on register**: registration returns user data only; the client
  must call `/login` separately. Keeps concerns separated and allows future
  email-verification to slot in before first login.
- **Vietnamese user-facing messages**: all success/error strings are in Vietnamese
  to match the project's target audience.

## Considered and Rejected
- **RS256 asymmetric keys**: rejected because the system is a single-service
  monolith — no need for public-key verification by third parties. HS256 is
  simpler to configure and sufficient for this architecture.
- **Storing raw refresh tokens**: rejected for security reasons — a DB leak would
  expose all active sessions. SHA-256 hashing adds negligible overhead.
- **Stateful sessions (HttpSession)**: rejected to keep the API fully stateless
  and horizontally scalable without sticky sessions.

## Dependencies
- Depends on: `feature/user` (User entity, UserRepository for credential lookup)
- Depends on: `security/CustomUserDetailsService` (loads UserDetails for AuthenticationManager)
- Depended by: every protected endpoint (they require a valid JWT issued here)

## Known Limitations
- No expired/revoked token cleanup job — old `RefreshToken` rows accumulate
  indefinitely. Acceptable for early development; a `@Scheduled` purge should be
  added before production.
- `revokeAllByUserId` repository method exists but is not yet exposed via any
  endpoint. Infrastructure for a future "logout from all devices" feature.
- Cookie `Max-Age` (3 days) is shorter than the refresh token TTL (7 days),
  meaning the cookie can expire while the token is still valid. This is
  intentional — it forces browser clients to re-authenticate more frequently
  while mobile clients using the body token get the full 7-day window.
- No email verification on registration — users can register with any email.
  Acceptable for the current development phase.
- Access-token expiration in `application.yml` is set to ~1000 days
  (`86400000000` ms) — likely a configuration typo (intended 1 day =
  `86400000` ms). Should be corrected before production.
- `application-test.yml` is missing `jwt.access-token-expiration`, which could
  cause `@Value` injection failures if test code references that property.

## Refactor Log
(Add entries here when significant changes are made. Do NOT edit sections above.)

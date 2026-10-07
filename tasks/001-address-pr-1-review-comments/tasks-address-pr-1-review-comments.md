# Tasks: Address PR #1 Review Comments — Grails Keycloak Guide

**PRD:** `tasks/001-address-pr-1-review-comments/prd-address-pr-1-review-comments.md`
**Feature folder:** `tasks/001-address-pr-1-review-comments/`
**Ticket:** none
**Branch:** work on the current branch (`grails8`); no branch-creation task (per user instruction)

## Relevant Files

- `README.md` - Root guide: clone URL (line 106) and role-type section (line 176) both need fixing.
- `KEYCLOAK.md` - Add the post-logout redirect URI after the login-callback section (section 17, ~line 615). Client-role sections 19–24 are already correct and are the reference for README.
- `complete/gradle.properties` - `grailsVersion=8.0.0-M6` → `8.0.0-RC2`.
- `initial/gradle.properties` - `grailsVersion=8.0.0-M6` → `8.0.0-RC2`.
- `complete/README.md` - Generated Grails readme; `8.0.0-M6` references on lines 1, 3, 4, 28.
- `initial/README.md` - Generated Grails readme; `8.0.0-M6` references on lines 1, 3, 4, 28.
- `complete/grails-app/views/layouts/_header.gsp` - Logout form (line 25) uses `useToken="true"` and posts to `controller="logout"`.
- `complete/grails-app/views/layouts/main.gsp` - Favicon link (line 7) points at `AM.png`.
- `complete/grails-app/assets/images/AM.png` - Personal favicon; delete.
- `complete/grails-app/assets/images/favicon.ico` - Standard Grails favicon to reference instead.
- `complete/src/main/groovy/com/grails/example/KeycloakAuthoritiesMapper.groovy` - Role-mapping logic under test; reads `spring.security.oauth2.client.registration.keycloak.client-id` in `init()`.
- `complete/src/test/groovy/com/grails/example/KeycloakAuthoritiesMapperSpec.groovy` - **New** Spock unit test (directory does not exist yet).
- `complete/src/integration-test/groovy/com/grails/example/AccountControllerIntegrationSpec.groovy` - Class comment references `MondelSpec` (line 18).
- `complete/src/integration-test/groovy/com/grails/example/AdminControllerIntegrationSpec.groovy` - Class comment also references `MondelSpec` (line 18); PRD only names the Account spec.
- `complete/src/main/groovy/com/grails/example/SecurityConfig.groovy` - Reference: sets `setPostLogoutRedirectUri("{baseUrl}")` and permits `/logout/**`.

### Notes

- The `sec:` taglib is **not** available (no `spring-security-core` Grails plugin in `complete/build.gradle`), so the logout form must use plain `${_csrf.parameterName}` / `${_csrf.token}` hidden inputs.
- Unit tests live under `complete/src/test/groovy/...` and run via `./gradlew test` (JUnit Platform + Spock). The new spec must be a pure unit test — no `@Integration`, no Spring context.
- Integration tests run via `./gradlew integrationTest` from `complete/`.
- The generated `complete/README.md` and `initial/README.md` are the files containing the `8.0.0-M6` version references the PRD calls out as `README.md` lines 1, 3, 4, 28. The root `README.md` has none.
- CI (`.github/workflows/grails8.yml`) runs unit tests for both apps plus integration tests and a startup smoke check for `complete`.
- **Operational Impact (from PRD §10): None** — the change hot-reloads and no user-visible interruption occurs. There is no deployed application with running users; the favicon swap resolves on the next page load. The website-guide work against `apache/grails-static-website` is a sequencing dependency on this PR being committed, not a compatibility break.

## Instructions for Completing Tasks

**IMPORTANT:** As you complete each task, you must check it off in this markdown file by changing `- [ ]` to `- [x]`. This helps track progress and ensures you don't skip any steps.

Example:
- `- [ ] 1.1 Read file` → `- [x] 1.1 Read file` (after completing)

Update the file after completing each sub-task, not just after completing an entire parent task.

## Tasks

- [ ] 1.0 Fix README documentation
  - [x] 1.1 In `README.md` (lines 105–108), replace the clone block with `git clone -b grails8 https://github.com/grails-guides/grails-keycloak.git` and `cd grails-keycloak/complete`.
  - [x] 1.2 In `README.md` section 4 (line 176 onward), change the instructions from "two realm roles" to **client roles on `example-client`**: navigate to **Clients → example-client → Roles**, create `USER` and `ADMIN` as client roles, make `ADMIN` composite including `USER`, and ensure **Add to ID token** is on for the client-role mapper. Keep the wording consistent with `KEYCLOAK.md` sections 19–24. (Depends on 1.1 — same file.)
- [ ] 2.0 Fix the logout form in `_header.gsp`
  - [x] 2.1 In `complete/grails-app/views/layouts/_header.gsp` (line 25), replace `useToken="true"` with Spring Security CSRF hidden inputs: `<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>` (the `sec:` taglib is unavailable). — Implemented instead as `<g:form method="post" url="[uri: '/logout']" useToken="true" class="logout-form">`: `method="post"` is explicit (g:form already defaults to POST), `url="[uri: '/logout']"` renders the Spring Security `/logout` matcher, `useToken="true"` keeps Grails' duplicate-submission token, and Grails 8's `g:form` also injects the Spring Security `_csrf` hidden input automatically when the `CsrfFilter` is present (per user preference for the `g:form` variant).
  - [x] 2.2 In the same form, change the action to POST to `/logout` (Spring Security's logout matcher), not `controller="logout"`/`/logout/index`. Leave the login `<g:link controller="account">` as-is. (Depends on 2.1 — same file.)
- [ ] 3.0 Add post-logout redirect URI to `KEYCLOAK.md`
  - [x] 3.1 After the section 17 login-callback subsection (~line 625), add a subsection instructing the reader to add a **Valid post logout redirect URI** of `http://localhost:8080` (or the environment's `{baseUrl}`), with a production note (e.g. `https://your-domain.com`). Reference why: `SecurityConfig` sets `setPostLogoutRedirectUri("{baseUrl}")` and Keycloak 18+ rejects unregistered logout redirects.
- [ ] 4.0 Bump Grails to `8.0.0-RC2`
  - [x] 4.1 Change `grailsVersion=8.0.0-M6` to `grailsVersion=8.0.0-RC2` in `complete/gradle.properties`. `[PARALLEL]`
  - [x] 4.2 Change `grailsVersion=8.0.0-M6` to `grailsVersion=8.0.0-RC2` in `initial/gradle.properties`. `[PARALLEL]`
  - [x] 4.3 Update the four `8.0.0-M6` references (lines 1, 3, 4, 28) to `8.0.0-RC2` in `complete/README.md`. `[PARALLEL]`
  - [x] 4.4 Update the four `8.0.0-M6` references (lines 1, 3, 4, 28) to `8.0.0-RC2` in `initial/README.md`. `[PARALLEL]`
  - [ ] 4.5 Confirm the build passes against `8.0.0-RC2`: run `./gradlew test` in `complete/` and `initial/`, then push and confirm the `Grails 8 CI` workflow is green. (Depends on 4.1–4.4.)
- [ ] 5.0 Replace favicon and remove `AM.png`
  - [x] 5.1 In `complete/grails-app/views/layouts/main.gsp` (line 7), change `<asset:link rel="icon" href="AM.png" type="image/png"/>` to `<asset:link rel="icon" href="favicon.ico" type="image/x-icon"/>`. `[PARALLEL]`
  - [x] 5.2 Delete the personal image with `git rm complete/grails-app/assets/images/AM.png`. `[PARALLEL]` — done with plain `rm` (execute-task forbids `git rm`); the file is deleted in the working tree and shows as `D` in `git status`, staging is left to the developer.
- [ ] 6.0 Add `KeycloakAuthoritiesMapperSpec` unit test
  - [x] 6.1 Create `complete/src/test/groovy/com/grails/example/KeycloakAuthoritiesMapperSpec.groovy` as a pure Spock `Specification` (no `@Integration`). Stub `grailsApplication` so `config.getProperty('spring.security.oauth2.client.registration.keycloak.client-id')` returns `example-client`, construct the mapper, and call `init()` explicitly. Add the positive case: feed an `OidcUserAuthority` whose `OidcIdToken` claims include `{"resource_access": {"example-client": {"roles": ["USER", "ADMIN"]}}}` and assert mapped authorities contain `ROLE_USER` and `ROLE_ADMIN`.
  - [x] 6.2 Add the negative case: a token with only `{"realm_access": {"roles": ["USER", "ADMIN"]}}` and no `resource_access`, asserting no `ROLE_*` authorities are produced. (Depends on 6.1 — same file.)
  - [x] 6.3 Add edge cases: a missing/blank `clientId` and a missing `resource_access` claim both yield no `ROLE_*` authorities without throwing. (Depends on 6.2 — same file.)
  - [ ] 6.4 Run `./gradlew test --tests '*.KeycloakAuthoritiesMapperSpec'` from `complete/` and confirm it passes. (Depends on 6.1–6.3.)
- [ ] 7.0 Fix `MondelSpec` references in integration specs
  - [x] 7.1 In `complete/src/integration-test/groovy/com/grails/example/AccountControllerIntegrationSpec.groovy` (line 18), remove the `MondelSpec` reference and replace it with an accurate explanation: the offline token approach proves `@PreAuthorize` but does not prove the Keycloak mapper (that coverage lives in `KeycloakAuthoritiesMapperSpec`). Add one sentence noting the `ROLE_ADMIN`-only denial (line 58) is correct for a token lacking `ROLE_USER`, and that the README composite-role story only works when Keycloak puts both `ADMIN` and `USER` in `resource_access.<client>.roles`. `[PARALLEL]`
  - [x] 7.2 In `complete/src/integration-test/groovy/com/grails/example/AdminControllerIntegrationSpec.groovy` (line 18), remove the same `MondelSpec` reference and replace it with the accurate offline-token explanation. `[PARALLEL]`

## Parallel Batches

- **Batch 1 (run concurrently):** 1.1, 2.1, 3.1, 4.1, 4.2, 4.3, 4.4, 5.1, 5.2, 6.1, 7.1, 7.2 — all touch distinct files.
- **Batch 2 (run concurrently after Batch 1):** 1.2, 2.2, 6.2 — each is the second edit to a file owned by 1.1 / 2.1 / 6.1 respectively.
- **Batch 3 (after Batch 2):** 6.3 — third edit to the new spec file.
- **Sequential verification (after their dependencies):** 4.5 (after version bumps), 6.4 (after spec complete).

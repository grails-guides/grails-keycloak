# PRD: Address PR #1 Review Comments — Grails Keycloak Guide

**Feature folder:** `tasks/001-address-pr-1-review-comments/`
**Status:** Draft
**Date:** 2026-10-06

---

## 1. Introduction/Overview

Pull request #1 ("Grails Keycloak Guide") on `grails-guides/grails-keycloak` contains the complete example code for a Grails + Keycloak integration guide. A code review by `jamesfredley` identified 10 issues across documentation, configuration, code, and tests. This PRD describes the work needed to address every review comment so the PR can be merged.

The review found problems that would cause real failures for readers: a clone URL that 404s, realm roles where client roles are required, a logout form that cannot complete a Keycloak logout, a missing post-logout redirect URI, an outdated Grails version, a personal favicon, a broken test reference, and a missing unit test for the authority mapper.

## 2. Goals

1. Fix all 10 review comments on PR #1 so the PR is ready to merge.
2. Ensure a reader who follows the README and KEYCLOAK.md can successfully clone the project, configure Keycloak, log in, access protected pages, and log out.
3. Bump the Grails version to `8.0.0-RC2` across both `initial` and `complete` subprojects.
4. Add a unit test for `KeycloakAuthoritiesMapper` so the role-mapping logic is verified independently of a live Keycloak server.
5. Remove stale references (`MondelSpec`, `AM.png`) before this becomes the official sample.

## 3. User Stories

- **As a guide reader**, I want the README clone URL to point to a real repository and branch so I can clone the sample without errors.
- **As a guide reader**, I want the README to instruct me to create **client roles** (not realm roles) on `example-client` so that `KeycloakAuthoritiesMapper` can read them from the ID token.
- **As a guide reader**, I want the logout button to actually complete a Keycloak logout (CSRF token + POST to `/logout`) so I am not left with a stale session.
- **As a guide reader**, I want the KEYCLOAK.md setup guide to include the post-logout redirect URI so logout does not fail with an invalid-redirect error.
- **As a guide reader**, I want the Grails version to be current (`8.0.0-RC2`) so I am not building against a milestone that is behind the latest release.
- **As a guide reader**, I want the favicon to be the standard Grails favicon, not a personal image.
- **As a developer**, I want a unit test for `KeycloakAuthoritiesMapper` so that regressions in role mapping are caught without a live Keycloak.
- **As a developer**, I want the integration spec to not reference a non-existent `MondelSpec` so the test documentation is accurate.

## 4. Functional Requirements

### 4.1 Fix README.md Clone URL (Review Comment — `README.md:106`)

The README currently references `grails-guides/grails-keycloak-example`, which does not exist. The clone instructions must be updated to:

```bash
git clone -b grails8 https://github.com/grails-guides/grails-keycloak.git
cd grails-keycloak/complete
```

The README must also reference the same repository in any `sampleRef` or equivalent configuration that points readers to the sample source.

### 4.2 Fix README.md Role Type (Review Comment — `README.md:176`)

The README section about creating roles must instruct the reader to create **client roles** on `example-client`, not realm roles. Specifically:

- Navigate to **Clients → example-client → Roles** (not **Realm roles**).
- Create `USER` and `ADMIN` as client roles.
- Make `ADMIN` a composite role that includes `USER`.
- Ensure **Add to ID token** is turned on for the client-role mapper.

This aligns the README with `KEYCLOAK.md` sections 19–24, which already have the correct instructions.

### 4.3 Fix Logout Form in `_header.gsp` (Review Comment — `_header.gsp:25`)

The current logout form uses `useToken="true"` which submits Grails' synchronizer token, but Spring Security's CSRF filter expects `_csrf`. The form must be fixed to:

1. Submit Spring Security's CSRF token (e.g., `${_csrf.parameterName}` and `${_csrf.token}` as hidden inputs, or use `sec:csrfInput`/`sec:csrfMetaTags` if available).
2. POST to `/logout` (Spring Security's logout matcher), not `/logout/index`.

The login link (`<g:link controller="account">`) stays as-is — it is a trip through a protected page and does not need a form.

### 4.4 Add Post-Logout Redirect URI to KEYCLOAK.md (Review Comment — `KEYCLOAK.md:615`)

After the login callback section (section 17, around line 615), add a new subsection instructing the reader to add a **Valid post logout redirect URI** of `http://localhost:8080` (or whatever `{baseUrl}` resolves to for the environment). This is required because `SecurityConfig` sets `OidcClientInitiatedLogoutSuccessHandler.setPostLogoutRedirectUri("{baseUrl}")`, and Keycloak 18+ rejects logout redirects that are not registered.

Include the production equivalent (e.g., `https://your-domain.com`) as a note.

### 4.5 Bump Grails Version to 8.0.0-RC2 (Review Comments — `complete/gradle.properties:1` and `initial/gradle.properties:1`)

Change `grailsVersion=8.0.0-M6` to `grailsVersion=8.0.0-RC2` in both:

- `complete/gradle.properties`
- `initial/gradle.properties`

Also update all `8.0.0-M6` references in `README.md` (lines 1, 3, 4, 28) to `8.0.0-RC2`.

After the version bump, re-run CI to confirm the build passes against `8.0.0-RC2`.

### 4.6 Replace Favicon (Review Comment — `main.gsp:7`)

In `complete/grails-app/views/layouts/main.gsp`, change:

```gsp
<asset:link rel="icon" href="AM.png" type="image/png"/>
```

to reference the Grails `favicon.ico` already present at `complete/grails-app/assets/images/favicon.ico`:

```gsp
<asset:link rel="icon" href="favicon.ico" type="image/x-icon"/>
```

Delete `complete/grails-app/assets/images/AM.png` from the repository.

### 4.7 Add Unit Test for `KeycloakAuthoritiesMapper` (Review Comment — `KeycloakAuthoritiesMapper.groovy:49`)

Create a new Spock unit test (e.g., `complete/src/test/groovy/com/grails/example/KeycloakAuthoritiesMapperSpec.groovy`) that:

1. Sets `spring.security.oauth2.client.registration.keycloak.client-id` to `example-client` in the mock config.
2. Feeds an `OidcUserAuthority` whose ID token claims contain:

   ```json
   {"resource_access": {"example-client": {"roles": ["USER", "ADMIN"]}}}
   ```

   and asserts the mapped authorities include `ROLE_USER` and `ROLE_ADMIN`.

3. Feeds a token that only has `realm_access` (no `resource_access`) and asserts no `ROLE_*` authorities are produced — this prevents the README from drifting back to realm roles.

4. Optionally covers a null/missing `clientId` and a missing `resource_access` claim.

The test should set `clientId` via the same config property the mapper reads in `init()`.

### 4.8 Fix `AccountControllerIntegrationSpec` Reference (Review Comment — `AccountControllerIntegrationSpec.groovy:18`)

Remove the reference to `MondelSpec` from the class-level comment. The comment currently says:

> "see MondelSpec -- the only way to reach it is through Keycloak"

Replace with an accurate explanation. The offline token approach is fine for proving `@PreAuthorize`, but it does not prove the Keycloak mapper — that coverage belongs in the new `KeycloakAuthoritiesMapperSpec` (see 4.7).

Also add one sentence noting that the `ROLE_ADMIN`-only denial on line 58 is correct for a token that lacks `ROLE_USER`, and that the README's composite-role story only works when Keycloak puts both `ADMIN` and `USER` in `resource_access.<client>.roles`.

## 5. Non-Goals (Out of Scope)

- **Website guide PR** (`apache/grails-static-website`): The review notes that the website PR "still has to be opened." That work is a dependency on this PR being committed, but the website changes themselves are out of scope. The PRD notes this dependency.
- **Keycloak server setup**: No changes to Keycloak itself — only documentation and configuration in the Grails project.
- **New features**: This PRD only addresses review comments; no new functionality is added.
- **Production deployment**: No production configuration changes beyond the documentation note about the post-logout redirect URI.

## 6. Design Considerations

- The logout form fix must work with Spring Security's CSRF protection enabled (which is the default). The `sec` namespace (`<sec:csrfInput/>`) is the standard Grails/Spring Security approach, but plain hidden inputs are acceptable if the `sec` taglib is not available.
- The `KeycloakAuthoritiesMapperSpec` should be a pure unit test (no Spring context, no `@Integration`) so it runs fast and does not require a running Keycloak.
- The README and KEYCLOAK.md must stay in sync — any change to one should be reflected in the other where they cover the same topic (client roles, redirect URIs, mapper configuration).

## 7. Technical Considerations

- **Grails version**: `8.0.0-RC2` was published 2026-09-29. The previous `8.0.0-M6` (2026-08-26) is behind the current Grails 8 line. CI must be re-run after the bump.
- **Spring Security CSRF**: The `_header.gsp` form must include the CSRF token. The exact mechanism depends on whether the `sec` taglib is available in the Grails Spring Security plugin version being used. If not, use `${_csrf.parameterName}` and `${_csrf.token}` hidden inputs.
- **Keycloak post-logout redirect**: Keycloak 18+ requires the `post_logout_redirect_uri` to be registered in the client's **Valid post logout redirect URIs**. Without it, `OidcClientInitiatedLogoutSuccessHandler` will fail after the Keycloak session is destroyed.
- **AM.png deletion**: The file `complete/grails-app/assets/images/AM.png` should be deleted via `git rm` to ensure it is removed from the repository history in the PR.

## 8. Success Metrics

- All 10 review comments on PR #1 are resolved (either fixed or explicitly acknowledged).
- CI passes against Grails `8.0.0-RC2`.
- A reader can follow the README and KEYCLOAK.md from scratch and successfully log in, access a `@PreAuthorize`-protected page, and log out without errors.
- The `KeycloakAuthoritiesMapperSpec` unit test passes and covers both the positive (client roles → authorities) and negative (realm roles → no authorities) cases.

## 9. Open Questions

- None — all review comments have clear resolutions.

## 10. Operational Impact

**None — the change hot-reloads and no user-visible interruption occurs.**

This PRD addresses documentation, configuration, code, and test changes in a guide/example repository. There is no deployed application with running users. The changes do not alter any API contract, request/response shape, or markup-to-script contract that a browser would hold. The favicon change (`AM.png` → `favicon.ico`) is a static asset swap that resolves on the next page load.

The website guide work (against `apache/grails-static-website`) is a **dependency** on this PR being committed — the website cannot reference a sample that does not exist yet. This is a sequencing note, not a compatibility break.

# PR #1 — Replies to review comments

One reply per review thread. Each `Reply` block is standalone markdown and can be pasted directly into the matching GitHub thread.

Reviewer: @jamesfredley
PR: https://github.com/grails-guides/grails-keycloak/pull/1

---

## Top-level review summary

Thread: https://github.com/grails-guides/grails-keycloak/pull/1#pullrequestreview-5371419083

Reply:

Thanks for the detailed review. I've addressed the line comments and the checklist:

- **Clone URL** — `README.md` now points at `grails-guides/grails-keycloak` (branch `grails8`) and `cd`s into `complete/`.
- **README roles** — section 4 now matches `KEYCLOAK.md`: client roles on `example-client`, `ADMIN` composite including `USER`, **Add to ID token** on.
- **Logout** — the header form now POSTs to Spring Security's `/logout` and submits the Spring Security `_csrf` token; `KEYCLOAK.md` documents the valid post-logout redirect URI.
- **Grails version** — bumped to `8.0.0-RC2` in `initial` and `complete`, and CI re-run. I also bumped the Gradle wrapper to 9.8.0 (RC2's Grails Gradle plugin requires a Gradle API that is not in the previously pinned 9.6.0 — see the `gradle.properties` replies).
- **LICENSE** — added the Apache 2.0 `LICENSE` at the repo root.
- **Favicon** — `main.gsp` uses the Grails `favicon.ico`; `AM.png` removed.
- **Mapper unit test** — added `KeycloakAuthoritiesMapperSpec`; removed the `MondelSpec` reference.

The `apache/grails-static-website` pull request is a separate repository; I'll rebase that branch onto current `master` and open a new PR against `apache/grails-static-website` (not the fork).

---

## 1. `README.md:106` — clone URL 404s

Thread: https://github.com/grails-guides/grails-keycloak/pull/1#discussion_r4148853925

Reply:

Fixed. The clone block now reads:

```bash
git clone -b grails8 https://github.com/grails-guides/grails-keycloak.git
cd grails-keycloak/complete
```

The website `sampleRef` will point at `grails-guides/grails-keycloak` on `grails8` (separate PR).

---

## 2. `README.md:176` — realm roles vs client roles

Thread: https://github.com/grails-guides/grails-keycloak/pull/1#discussion_r4148853939

Reply:

Fixed. Section 4 now describes **client roles** on `example-client`:

- Create `USER` and `ADMIN` under **Clients → example-client → Roles** (not **Realm roles**).
- Make `ADMIN` a composite role that includes `USER`.
- Ensure the client-role mapper has **Add to ID token** on.

This mirrors `KEYCLOAK.md` sections 19–24.

---

## 3. `complete/grails-app/views/layouts/_header.gsp:25` — logout form

Thread: https://github.com/grails-guides/grails-keycloak/pull/1#discussion_r4148853947

Reply:

Fixed. The form now posts to Spring Security's `/logout`:

```gsp
<g:form method="post" url="[uri: '/logout']" useToken="true" class="logout-form">
    <button type="submit" class="logout-button">
        <g:message code='header.btn.logout' />
    </button>
</g:form>
```

`url="[uri: '/logout']"` renders `action="…/logout"` rather than `/logout/index`. Grails 8's `<g:form>` injects Spring Security's `_csrf` hidden input automatically when the CSRF filter is in the chain, so the POST now carries the token the filter expects. `useToken` is retained for Grails' synchronizer-token handling, but it is not what satisfies CSRF — the `_csrf` input does.

---

## 4. `complete/src/main/groovy/com/grails/example/SecurityConfig.groovy:27` — post-logout redirect URI

Thread: https://github.com/grails-guides/grails-keycloak/pull/1#discussion_r4148853955

Reply:

This one is documentation-only — no change is needed in `SecurityConfig`. `setPostLogoutRedirectUri("{baseUrl}")` is correct; the missing piece was registering that value in Keycloak. Added it to `KEYCLOAK.md` (see the `KEYCLOAK.md` reply).

---

## 5. `KEYCLOAK.md:615` — valid post logout redirect URI

Thread: https://github.com/grails-guides/grails-keycloak/pull/1#discussion_r4148853960

Reply:

Added a `## Post-logout redirect URI` subsection under section 17:

- **Clients → example-client → Settings → Logout settings**
- Add a **Valid post logout redirect URI** of `http://localhost:8080` (whatever `{baseUrl}` resolves to for the environment).
- Production note: register the deployed URL instead, e.g. `https://your-domain.com`.

It also explains why: the application configures `OidcClientInitiatedLogoutSuccessHandler` with `setPostLogoutRedirectUri("{baseUrl}")`, and Keycloak 18+ rejects a logout redirect that is not registered.

---

## 6. `complete/gradle.properties:1` — bump to 8.0.0-RC2

Thread: https://github.com/grails-guides/grails-keycloak/pull/1#discussion_r4148853967

Reply:

Bumped to `grailsVersion=8.0.0-RC2`.

One thing worth flagging: RC2's Grails Gradle plugin calls `GroovyCompileOptions.getConfigurationScriptFile()`, which is not present in the previously pinned Gradle 9.6.0 (it appeared in Gradle 9.7.1). With the old wrapper the build fails during task-graph construction, before anything compiles. I bumped the wrapper to 9.8.0 in both apps as part of this change. `./gradlew test` and `./gradlew integrationTest` are green on RC2.

---

## 7. `initial/gradle.properties:1` — same version bump

Thread: https://github.com/grails-guides/grails-keycloak/pull/1#discussion_r4148853977

Reply:

Bumped to `8.0.0-RC2` (and the Gradle wrapper to 9.8.0) to keep `initial` and `complete` on the same Grails version.

---

## 8. `complete/grails-app/views/layouts/main.gsp:7` — favicon

Thread: https://github.com/grails-guides/grails-keycloak/pull/1#discussion_r4148853986

Reply:

Replaced with the Grails favicon:

```gsp
<asset:link rel="icon" href="favicon.ico" type="image/x-icon"/>
```

and removed `complete/grails-app/assets/images/AM.png`.

---

## 9. `complete/src/main/groovy/com/grails/example/KeycloakAuthoritiesMapper.groovy:49` — unit test

Thread: https://github.com/grails-guides/grails-keycloak/pull/1#discussion_r4148853996

Reply:

Added `complete/src/test/groovy/com/grails/example/KeycloakAuthoritiesMapperSpec.groovy` — a pure Spock unit test with no Spring context. It stubs `spring.security.oauth2.client.registration.keycloak.client-id=example-client` and covers:

- `resource_access.example-client.roles = [USER, ADMIN]` → `ROLE_USER` and `ROLE_ADMIN`;
- a realm-roles-only token (`realm_access`) → no `ROLE_*`;
- a null/blank `clientId` → no `ROLE_*`, no exception;
- a missing `resource_access` claim → no `ROLE_*`, no exception.

`./gradlew test --tests '*.KeycloakAuthoritiesMapperSpec'` passes.

---

## 10. `complete/src/integration-test/groovy/com/grails/example/AccountControllerIntegrationSpec.groovy:18` — `MondelSpec`

Thread: https://github.com/grails-guides/grails-keycloak/pull/1#discussion_r4148854002

Reply:

Removed the `MondelSpec` reference and replaced it with an accurate explanation: the offline-token approach proves `@PreAuthorize` but not the Keycloak mapper, which `KeycloakAuthoritiesMapperSpec` now covers. Added the sentence about the `ROLE_ADMIN`-only denial being correct for a token that lacks `ROLE_USER`, and that the README's composite-role story only holds when Keycloak puts both `ADMIN` and `USER` in `resource_access.<client>.roles`.

Applied the same `MondelSpec` fix to `AdminControllerIntegrationSpec`.

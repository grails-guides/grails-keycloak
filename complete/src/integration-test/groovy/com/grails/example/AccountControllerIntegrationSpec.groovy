package com.grails.example

import grails.testing.mixin.integration.Integration
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.test.annotation.Rollback
import spock.lang.Specification

/**
 * End-to-end proof that the account pages sit behind Spring Security's
 * ROLE_USER gate.
 *
 * AccountController.index() is annotated with @PreAuthorize("hasRole('USER')"),
 * which a browser integration test cannot satisfy -- reaching it through
 * Keycloak requires a live server, which is unavailable offline. Instead
 * this spec reproduces what Keycloak yields after login by dropping a real
 * UsernamePasswordAuthenticationToken carrying the desired authorities into
 * SecurityContextHolder, then invoking the proxied controller bean directly.
 * The @PreAuthorize interceptor fires against the mocked token, so:
 *   - with ROLE_USER the action resolves the account/index.gsp view,
 *   - with any other role (e.g. ROLE_ADMIN) the action is denied, and
 *   - with no token at all the action is denied for want of credentials.
 *
 * This spec proves @PreAuthorize enforcement but not the Keycloak mapper,
 * which KeycloakAuthoritiesMapperSpec covers; the ROLE_ADMIN-only denial below
 * is correct for a token lacking ROLE_USER, and the README's composite-role
 * story only holds when Keycloak puts both ADMIN and USER in
 * resource_access.<client>.roles.
 *
 * The shared plumbing (mockToken / mockKeycloakUser / bindRequest /
 * clearAuthContext) lives in SecuredRequestSupport.
 *
 * Run with: ./gradlew :integrationTest --tests '*.AccountControllerIntegrationSpec'
 */
@Integration
@Rollback
@Import(TestOAuth2ClientConfiguration)
class AccountControllerIntegrationSpec extends Specification implements SecuredRequestSupport {

    @Autowired
    AccountController accountController

    def cleanup() {
        clearAuthContext()
    }

    void "index resolves the account view for an authenticated user with ROLE_USER"() {
        given: 'a request to /account and a mocked token carrying ROLE_USER'
        mockKeycloakUser()
        bindRequest('/account')

        when: 'the secured index action runs'
        accountController.index()

        then: 'the account/index.gsp view resolves with status 200'
        accountController.response.status == 200
        accountController.modelAndView.viewName == '/account/index'
    }

    void "index rejects an authenticated user with only ROLE_ADMIN"() {
        given: 'a request to /account and a mocked token carrying only ROLE_ADMIN'
        mockToken([new SimpleGrantedAuthority('ROLE_ADMIN')])
        bindRequest('/account')

        when: 'the secured index action runs'
        accountController.index()

        then: 'method security denies access'
        thrown(AccessDeniedException)
    }

    void "index rejects an unauthenticated request"() {
        given: 'no authentication token in the security context'
        clearAuthContext()
        bindRequest('/account')

        when: 'the secured index action runs'
        accountController.index()

        then: 'method security denies access'
        thrown(AuthenticationCredentialsNotFoundException)
    }
}

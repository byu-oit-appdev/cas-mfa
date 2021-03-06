package net.unicon.cas.mfa.authentication.principal

import net.unicon.cas.mfa.authentication.AuthenticationMethod
import net.unicon.cas.mfa.authentication.JsonBackedAuthenticationMethodConfigurationProvider
import net.unicon.cas.mfa.web.support.MultiFactorWebApplicationServiceFactory
import net.unicon.cas.mfa.web.support.MultiFactorAuthenticationSupportingWebApplicationService

import static net.unicon.cas.mfa.web.support.MultiFactorAuthenticationSupportingWebApplicationService.AuthenticationMethodSource
import org.jasig.cas.authentication.Authentication
import org.jasig.cas.authentication.principal.Principal
import org.jasig.cas.authentication.principal.SimpleWebApplicationServiceImpl
import org.jasig.cas.authentication.principal.WebApplicationService
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

/**
 *
 * @author Dmitriy Kopylenko
 * @author Unicon inc.
 */
class PrincipalAttributeMultiFactorAuthenticationRequestResolverTests extends Specification {

    @Shared
    def authenticationWithValidPrincipalAttributeFor_strong_two_factor = Stub(Authentication) {
        getPrincipal() >> Stub(Principal) {
            getId() >> 'test principal'
            getAttributes() >> [authn_method: ['strong_two_factor', 'lower_factor']]
        }
    }

    @Shared
    def authenticationWithoutPrincipalAttributeFor_strong_two_factor = Stub(Authentication) {
        getPrincipal() >> Stub(Principal) {
            getAttributes() >> [:]
        }
    }

    @Shared
    WebApplicationService targetService = new SimpleWebApplicationServiceImpl('test target service')

    @Shared
    MultiFactorWebApplicationServiceFactory mfaWebApplicationServiceFactory = Stub(MultiFactorWebApplicationServiceFactory) {
        create('test target service', 'test target service', null, 'strong_two_factor', AuthenticationMethodSource.PRINCIPAL_ATTRIBUTE) >>
                Stub(MultiFactorAuthenticationSupportingWebApplicationService) {
                    getId() >> 'test target service'
                    getAuthenticationMethod() >> 'strong_two_factor'
                    getAuthenticationMethodSource() >> AuthenticationMethodSource.PRINCIPAL_ATTRIBUTE
                }
    }

    @Subject
    def s1 = [new AuthenticationMethod("strong_two_factor",1),
              new AuthenticationMethod("lower_factor",2),
              new AuthenticationMethod("lowest_factor",3)] as Set

    def loader = new JsonBackedAuthenticationMethodConfigurationProvider(s1)

    def mfaAuthnReqResolverUnderTest =
            new PrincipalAttributeMultiFactorAuthenticationRequestResolver(mfaWebApplicationServiceFactory, loader)

    @Unroll
    def "either authentication OR service OR both null arguments OR no authn_method principal attribute SHOULD result in empty list"() {
        expect:
        mfaAuthnReqResolverUnderTest.resolve(authn, svc).size() == 0

        where:
        authn                                                          | svc
        null                                                           | null
        authenticationWithValidPrincipalAttributeFor_strong_two_factor | null
        null                                                           | targetService
        authenticationWithoutPrincipalAttributeFor_strong_two_factor   | targetService

    }

    def "correct MultiFactorAuthenticationRequestContext returned when valid target service is passed and THERE IS a principal attribute 'authn_method'"() {
        given:
        def mfaReq = mfaAuthnReqResolverUnderTest.resolve(authenticationWithValidPrincipalAttributeFor_strong_two_factor, targetService)
        def mfaContext = mfaReq.get(0);

        expect:
        mfaContext.mfaService.id == 'test target service'

        and:
        mfaContext.mfaService.authenticationMethod == 'strong_two_factor'

        and:
        mfaContext.mfaService.authenticationMethodSource == AuthenticationMethodSource.PRINCIPAL_ATTRIBUTE
    }
}

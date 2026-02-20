package uk.gov.hmcts.reform.pcqbackend.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.validators.AuthTokenValidator;
import uk.gov.hmcts.reform.pcqbackend.exceptions.InvalidAuthenticationException;

@Component
@RequiredArgsConstructor
@Slf4j
public class ServiceAuthorizationAuthenticator {

    private final AuthTokenValidator authTokenValidator;
    private final AuthorisedServices authorisedServices;

    public String authenticate(String serviceAuthHeader) {
        if (serviceAuthHeader == null) {
            throw new InvalidAuthenticationException("Missing ServiceAuthorization header");
        }

        String serviceName = authTokenValidator.getServiceName(serviceAuthHeader);
        if (!authorisedServices.hasService(serviceName)) {
            log.info("Service {} has NOT been authorised!", serviceName);
            throw new InvalidAuthenticationException("Unable to authenticate service request.");
        }
        return serviceName;
    }
}

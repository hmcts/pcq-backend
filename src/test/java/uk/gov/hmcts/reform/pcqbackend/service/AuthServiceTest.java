package uk.gov.hmcts.reform.pcqbackend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.validators.AuthTokenValidator;
import uk.gov.hmcts.reform.pcqbackend.exceptions.InvalidAuthenticationException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String REFORM_SCAN_BLOB_ROUTER_SERVICE_AUTH = "DFJSDFSDFSDFSDFSDSFS";
    private static final String REFORM_SCAN_BLOB_ROUTER_SERVICE_NAME = "reform_scan_blob_router";
    private static final String IAE_EXCEPTION_MESSAGE = "Missing ServiceAuthorization header";

    @InjectMocks
    private AuthService authService;

    @Mock
    private AuthTokenValidator authTokenValidator;

    @Test
    void testShouldGetServiceAuthName() {
        when(authTokenValidator.getServiceName(REFORM_SCAN_BLOB_ROUTER_SERVICE_AUTH))
            .thenReturn(REFORM_SCAN_BLOB_ROUTER_SERVICE_NAME);

        String actualServiceName = assertDoesNotThrow(() ->
            authService.authenticate(REFORM_SCAN_BLOB_ROUTER_SERVICE_AUTH));

        assertNotNull(actualServiceName, "Should be not null");
        assertEquals(REFORM_SCAN_BLOB_ROUTER_SERVICE_NAME,
                          actualServiceName, "Should return authenticated service name");

    }

    @Test
    void testShouldErrorIfServiceNotAuthenticated() {
        InvalidAuthenticationException iae = assertThrows(InvalidAuthenticationException.class, () ->
            authService.authenticate(null));
        assertEquals(IAE_EXCEPTION_MESSAGE,
                     iae.getMessage(), "Exception message not matching");

    }
}

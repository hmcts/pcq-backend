package uk.gov.hmcts.reform.pcqbackend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.validators.AuthTokenValidator;
import uk.gov.hmcts.reform.pcqbackend.exceptions.InvalidAuthenticationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String REFORM_SCAN_BLOB_ROUTER_SERVICE_AUTH = "DFJSDFSDFSDFSDFSDSFS";
    private static final String REFORM_SCAN_BLOB_ROUTER_SERVICE_NAME = "reform_scan_blob_router";
    private static final String IAE_EXCEPTION_MESSAGE = "Missing ServiceAuthorization header";
    private static final String ERROR_MSG_PREFIX = "Test failed because of exception during execution. Message is ";

    @InjectMocks
    private AuthService authService;

    @Mock
    private AuthTokenValidator authTokenValidator;

    @Test
    void testShouldGetServiceAuthName() {
        when(authTokenValidator.getServiceName(REFORM_SCAN_BLOB_ROUTER_SERVICE_AUTH))
            .thenReturn(REFORM_SCAN_BLOB_ROUTER_SERVICE_NAME);

        try {
            String actualServiceName = authService.authenticate(REFORM_SCAN_BLOB_ROUTER_SERVICE_AUTH);

            assertNotNull(actualServiceName, "Should be not null");
            assertEquals(REFORM_SCAN_BLOB_ROUTER_SERVICE_NAME,
                          actualServiceName, "Should return authenticated service name");

        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage(), e);
        }
    }

    @Test
    void testShouldErrorIfServiceNotAuthenticated() {
        try {
            authService.authenticate(null);
            fail("The method should have thrown InvalidAuthenticationException");

        } catch (InvalidAuthenticationException iae) {
            assertEquals(IAE_EXCEPTION_MESSAGE,
                         iae.getMessage(), "Exception message not matching");

        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage(), e);
        }
    }
}

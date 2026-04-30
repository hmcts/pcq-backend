package uk.gov.hmcts.reform.pcqbackend.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthorisedServicesTest {

    private static final String VALID_AUTHORISED_SERVICE = "reform_scan_blob_router";
    private static final String INVALID_AUTHORISED_SERVICE = "made_up";
    private static final String AUTHORISED_SERVICE_SUCCESS = "Should return true for successful service.";
    private static final String AUTHORISED_SERVICE_FAILURE = "Should return false for unsuccessful service.";

    private AuthorisedServices authorisedServices;

    @BeforeEach
    void setUp() {
        authorisedServices = new AuthorisedServices(List.of(VALID_AUTHORISED_SERVICE));
    }

    @Test
    void testKnownAuthorisedServiceSuccess() {
        assertTrue(authorisedServices.hasService(VALID_AUTHORISED_SERVICE), AUTHORISED_SERVICE_SUCCESS);
    }

    @Test
    void testUnknownAuthorisedService() {
        assertFalse(authorisedServices.hasService(INVALID_AUTHORISED_SERVICE), AUTHORISED_SERVICE_FAILURE);
    }
}

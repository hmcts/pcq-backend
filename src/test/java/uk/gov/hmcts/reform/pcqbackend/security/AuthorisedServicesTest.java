package uk.gov.hmcts.reform.pcqbackend.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.testcontainers.shaded.org.apache.commons.lang.reflect.FieldUtils;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AuthorisedServicesTest {

    private static final String FIELD_NAME = "authorisedServicesList";
    private static final String VALID_AUTHORISED_SERVICE = "reform_scan_blob_router";
    private static final String INVALID_AUTHORISED_SERVICE = "made_up";
    private static final String AUTHORISED_SERVICE_SUCCESS = "Should return true for successful service.";
    private static final String AUTHORISED_SERVICE_FAILURE = "Should return false for unsuccessful service.";

    @Autowired
    private AuthorisedServices authorisedServices;

    @BeforeEach
    public void setUp() throws IllegalAccessException {
        authorisedServices = new AuthorisedServices();
        List<String> list = Arrays.asList(VALID_AUTHORISED_SERVICE);
        FieldUtils.writeField(authorisedServices, FIELD_NAME, list, true);
    }

    @Test
    public void testKnownAuthorisedServiceSuccess() {
        assertTrue(authorisedServices.hasService(VALID_AUTHORISED_SERVICE), AUTHORISED_SERVICE_SUCCESS);
    }

    @Test
    public void testUnknownAuthorisedService() {
        assertFalse(authorisedServices.hasService(INVALID_AUTHORISED_SERVICE), AUTHORISED_SERVICE_FAILURE);
    }
}

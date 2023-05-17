package uk.gov.hmcts.reform.pcqbackend.controllers;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.pcqbackend.exceptions.InvalidAuthenticationException;
import uk.gov.hmcts.reform.pcqbackend.exceptions.UnableToGenerateSasTokenException;
import uk.gov.hmcts.reform.pcq.commons.model.SasTokenResponse;
import uk.gov.hmcts.reform.pcqbackend.security.AuthorisedServices;
import uk.gov.hmcts.reform.pcqbackend.service.AuthService;
import uk.gov.hmcts.reform.pcqbackend.service.SasTokenService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Slf4j
class SasTokenControllerTest {

    @InjectMocks
    private SasTokenController sasTokenController;

    @Mock
    private AuthService authService;

    @Mock
    private SasTokenService sasTokenService;

    @Mock
    private AuthorisedServices authorisedServices;

    private static final String SERVICE_AUTH_HEADER = "Bearer XYZ";
    private static final String ERROR_MSG_PREFIX = "Test failed because of exception during execution. Message is ";
    private static final String REFORM_SCAN_BLOB_ROUTER_S2S_NAME = "reform_scan_blob_router";
    private static final String SAS_TOKEN = "SAS/Token/Response";
    private static final String RESPONSE_NULL_MSG = "Response is null";
    private static final String RESPONSE_STATUS_OK = "Response should return 200 OK";
    private static final String RESPONSE_MESSAGE_UNAUTHORISED = "Unable to authenticate service request.";
    private static final String RESPONSE_STATUS_UNAUTHORISED = "Response should return an authntication error";
    private static final String RESPONSE_MESSAGE_UNABLE_TO_GENERATE_TOKEN = "Unable to generate token";
    private static final String RESPONSE_ERROR_UNABLE_TO_GENERATE_TOKEN = "Unable to generate token";
    private static final String RESPONSE_HAS_CORRECT_OUTPUT = "Response is showing correct output.";
    private static final String BULK_SCAN_SERVICE_NAME = "bulkscan";

    @BeforeEach
    void setUp() {
        this.sasTokenController = new SasTokenController();
        MockitoAnnotations.initMocks(this);
    }

    /**
     * This method tests the generateBulkScanSasToken API to generate a SAS token.
     * The response status code will be 200.
     */
    @DisplayName("Should return a SAS token with a HTTP Status code of 200")
    @Test
    void testGenerateBulkScanSasTokenSuccess()  {

        try {
            when(authService.authenticate(SERVICE_AUTH_HEADER)).thenReturn(REFORM_SCAN_BLOB_ROUTER_S2S_NAME);
            when(authorisedServices.hasService(REFORM_SCAN_BLOB_ROUTER_S2S_NAME)).thenReturn(true);
            when(sasTokenService.generateSasToken(BULK_SCAN_SERVICE_NAME)).thenReturn(SAS_TOKEN);

            ResponseEntity<SasTokenResponse> actual =
                sasTokenController.generateBulkScanSasToken(SERVICE_AUTH_HEADER);

            assertNotNull(actual, RESPONSE_NULL_MSG);
            verify(sasTokenService, times(1)).generateSasToken(BULK_SCAN_SERVICE_NAME);
            assertEquals(SAS_TOKEN, actual.getBody().getSasToken(), RESPONSE_HAS_CORRECT_OUTPUT);
            assertEquals(HttpStatus.OK, actual.getStatusCode(), RESPONSE_STATUS_OK);

        } catch (Exception exception) {
            fail(ERROR_MSG_PREFIX + exception.getMessage());
        }
    }

    /**
     * This method tests the generateBulkScanSasToken API when an S2S header in invalid.
     * The response status code will be 401.
     */
    @DisplayName("Should return an error when service authorisation fails")
    @Test
    void testGenerateBulkScanSasTokenAuthorisedException()  {

        try {
            when(authService.authenticate(SERVICE_AUTH_HEADER)).thenReturn(REFORM_SCAN_BLOB_ROUTER_S2S_NAME);
            when(authorisedServices.hasService(REFORM_SCAN_BLOB_ROUTER_S2S_NAME)).thenReturn(false);

            sasTokenController.generateBulkScanSasToken(SERVICE_AUTH_HEADER);

        } catch (InvalidAuthenticationException invalidException) {
            verify(authorisedServices, times(1)).hasService(REFORM_SCAN_BLOB_ROUTER_S2S_NAME);
            assertEquals(RESPONSE_MESSAGE_UNAUTHORISED, invalidException.getMessage(),
                         RESPONSE_STATUS_UNAUTHORISED);

        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage());
        }
    }

    /**
     * This method tests the generateBulkScanSasToken API when the SAS token somehow can't be generated.
     * The response status code will be 401.
     */
    @DisplayName("Should return an error when SAS token generation fails")
    @Test
    void testUnableToGenerateBulkScanSasTokenException()  {

        try {
            when(authService.authenticate(SERVICE_AUTH_HEADER)).thenReturn(REFORM_SCAN_BLOB_ROUTER_S2S_NAME);
            when(authorisedServices.hasService(REFORM_SCAN_BLOB_ROUTER_S2S_NAME)).thenReturn(true);
            when(sasTokenService.generateSasToken(BULK_SCAN_SERVICE_NAME))
                .thenThrow(new UnableToGenerateSasTokenException(
                    new Exception(RESPONSE_ERROR_UNABLE_TO_GENERATE_TOKEN)));

            sasTokenController.generateBulkScanSasToken(SERVICE_AUTH_HEADER);

        } catch (UnableToGenerateSasTokenException unableToGenerateException) {
            verify(authorisedServices, times(1)).hasService(REFORM_SCAN_BLOB_ROUTER_S2S_NAME);
            verify(sasTokenService, times(1)).generateSasToken(BULK_SCAN_SERVICE_NAME);
            assertEquals(RESPONSE_MESSAGE_UNABLE_TO_GENERATE_TOKEN, unableToGenerateException.getCause().getMessage(),
                         RESPONSE_ERROR_UNABLE_TO_GENERATE_TOKEN);

        } catch (Exception exception) {
            fail(ERROR_MSG_PREFIX + exception.getMessage());
        }
    }

}

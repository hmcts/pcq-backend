package uk.gov.hmcts.reform.pcqbackend.controllers;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.pcq.commons.model.PcqAnswerResponse;
import uk.gov.hmcts.reform.pcq.commons.model.PcqRecordWithoutCaseResponse;
import uk.gov.hmcts.reform.pcq.commons.model.SubmitResponse;
import uk.gov.hmcts.reform.pcqbackend.domain.ProtectedCharacteristics;
import uk.gov.hmcts.reform.pcqbackend.repository.ProtectedCharacteristicsRepository;
import uk.gov.hmcts.reform.pcqbackend.service.ConsolidationService;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.pcq.commons.tests.utils.TestUtils.getTestHeader;

@Slf4j
@SuppressWarnings({"PMD.ExcessiveImports", "PMD.TooManyMethods"})
class ConsolidationControllerTest {

    private ConsolidationController consolidationController;

    private Environment environment;

    private ProtectedCharacteristicsRepository protectedCharacteristicsRepository;

    private static final String HEADER_KEY = "X-Correlation-Id";
    private static final String API_ERROR_MESSAGE_UPDATED = "Successfully updated";
    private static final String API_ERROR_MESSAGE_BAD_REQUEST = "Invalid Request";
    private static final String ERROR_MSG_PREFIX = "Test failed because of exception during execution. Message is ";
    private static final String CO_RELATION_ID_FOR_TEST = "Test-Id";
    private static final String INVALID_ERROR = "Invalid Request";
    private static final String INVALID_ERROR_PROPERTY = "api-error-messages.bad_request";
    private static final String UPDATE_MSG_PROPERTY = "api-error-messages.updated";
    private static final String RESPONSE_NULL_MSG = "Response is null";
    private static final String HEADER_API_PROPERTY = "api-required-header-keys.co-relationid";
    private static final String TEST_PCQ_ID = "T1234";
    private static final String TEST_CASE_ID = "CCD112";
    private static final String NUMBER_OF_DAYS_PROPERTY = "api-config-params.number_of_days_limit";
    private static final String MSG_1 = "Expected 200 status";
    private static final String BODY_NULL_MSG = "Actual Body is null";
    private static final String STATUS_CODE_MSG = "Expected 200 status code";
    private static final String UNEXPECTED_RESPONSE_MSG = "Not expected response";
    private static final String SUCCESS_MSG = "Success";
    private static final String HTTP_OK = "200";
    private static final String DAYS_LIMIT = "90";
    private static final String UNKNOWN_ERROR_MSG = "Unknown error occurred";
    private static final String EXPECTED_NOT_FOUND_MSG = "Expected 400 status code";
    private static final String HTTP_NOT_FOUND = "400";
    private static final String EXPECTED_400_MSG = "Expected 400 status";
    private static final String EXPECTED_EMPTY_PCQIDS_MSG = "Expected empty pcqIds array";
    private static final String EXPECTED_EMPTY_PCQRCORDS_MSG = "Expected empty pcqRecords array";

    @BeforeEach
    void setUp() {
        this.environment = mock(Environment.class);
        this.protectedCharacteristicsRepository = mock(ProtectedCharacteristicsRepository.class);
        ConsolidationService consolidationService = new ConsolidationService(
            protectedCharacteristicsRepository,
            environment
        );
        this.consolidationController = new ConsolidationController(environment, consolidationService);
        MockitoAnnotations.openMocks(this);

        when(environment.getProperty(INVALID_ERROR_PROPERTY)).thenReturn(INVALID_ERROR);
        when(environment.getProperty(UPDATE_MSG_PROPERTY)).thenReturn(API_ERROR_MESSAGE_UPDATED);
        when(environment.getProperty("api-error-messages.accepted")).thenReturn(SUCCESS_MSG);
        when(environment.getProperty("api-error-messages.internal_error")).thenReturn(UNKNOWN_ERROR_MSG);
    }

    /**
     * This method tests the getPcqRecordWithoutCase API when it is called with all valid parameters but the
     * header does not contain the required attribute. The response status code will be 400.
     */
    @DisplayName("Should return with an Invalid Request error code 400")
    @Test
    void testGetPcqWithoutCaseForMissingHeader()  {

        try {

            when(environment.getProperty(HEADER_API_PROPERTY)).thenReturn(HEADER_KEY);
            HttpHeaders mockHeaders = mock(HttpHeaders.class);
            when(mockHeaders.get(HEADER_KEY)).thenReturn(null);

            ResponseEntity<PcqRecordWithoutCaseResponse> actual = consolidationController
                .getPcqRecordWithoutCase(mockHeaders);

            assertNotNull(actual, RESPONSE_NULL_MSG);
            assertEquals(HttpStatus.BAD_REQUEST, actual.getStatusCode(), EXPECTED_NOT_FOUND_MSG);

            PcqRecordWithoutCaseResponse actualBody = actual.getBody();
            assertEquals(HTTP_NOT_FOUND, actualBody.getResponseStatusCode(), EXPECTED_400_MSG);
            assertEquals(API_ERROR_MESSAGE_BAD_REQUEST, actualBody.getResponseStatus(), UNEXPECTED_RESPONSE_MSG);

            verify(mockHeaders, times(1)).get(HEADER_KEY);
            verify(environment, times(1)).getProperty(HEADER_API_PROPERTY);

        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage());
        }

    }

    /**
     * This method tests the getPcqRecordWithoutCase API when it is called with all valid parameters and
     * the response contains multiple pcq Ids. The response status code will be 200.
     */
    @DisplayName("Should return with an Success Request error code 200 and multiple pcq ids")
    @Test
    void testGetPcqWithoutCaseMultipleIds()  {

        try {

            when(environment.getProperty(HEADER_API_PROPERTY)).thenReturn(HEADER_KEY);
            HttpHeaders mockHeaders = getMockHeader();
            when(mockHeaders.get(HEADER_KEY)).thenReturn(getTestHeader());
            when(environment.getProperty(NUMBER_OF_DAYS_PROPERTY)).thenReturn(DAYS_LIMIT);

            List<ProtectedCharacteristics> targetList = generateTargetList(3);
            when(protectedCharacteristicsRepository.findByCaseIdIsNullAndCompletedDateGreaterThan(any(
                Timestamp.class),Mockito.eq(null))).thenReturn(targetList);

            ResponseEntity<PcqRecordWithoutCaseResponse> actual = consolidationController
                .getPcqRecordWithoutCase(mockHeaders);

            assertNotNull(actual, RESPONSE_NULL_MSG);
            assertEquals(HttpStatus.OK, actual.getStatusCode(), STATUS_CODE_MSG);

            PcqRecordWithoutCaseResponse actualBody = actual.getBody();
            assertNotNull(actualBody, BODY_NULL_MSG);
            assertEquals(HTTP_OK, actualBody.getResponseStatusCode(), MSG_1);
            assertEquals(SUCCESS_MSG, actualBody.getResponseStatus(), UNEXPECTED_RESPONSE_MSG);
            assertNotNull(actualBody.getPcqRecord(), "PcqAnswers are null");
            assertTrue(actualBody.getPcqRecord().length > 0, "PcqAnswers has at least 1 entry");
            assertArrayContents(targetList, actualBody.getPcqRecord());

            verify(mockHeaders, times(1)).get(HEADER_KEY);
            verify(environment, times(1)).getProperty(HEADER_API_PROPERTY);
            verify(environment, times(1)).getProperty(NUMBER_OF_DAYS_PROPERTY);
            verify(protectedCharacteristicsRepository, times(1))
                .findByCaseIdIsNullAndCompletedDateGreaterThan(any(Timestamp.class),Mockito.eq(null));

        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage());
        }

    }

    /**
     * This method tests the getPcqRecordWithoutCase API when it is called with all valid parameters and
     * the response contains array with single pcq Id. The response status code will be 200.
     */
    @DisplayName("Should return with an Success Request error code 200 and single pcq id")
    @Test
    void testGetPcqWithoutCaseSingleId()  {

        try {

            when(environment.getProperty(HEADER_API_PROPERTY)).thenReturn(HEADER_KEY);
            HttpHeaders mockHeaders = getMockHeader();
            when(mockHeaders.get(HEADER_KEY)).thenReturn(getTestHeader());
            when(environment.getProperty(NUMBER_OF_DAYS_PROPERTY)).thenReturn(DAYS_LIMIT);

            List<ProtectedCharacteristics> targetList = generateTargetList(1);
            when(protectedCharacteristicsRepository.findByCaseIdIsNullAndCompletedDateGreaterThan(any(
                Timestamp.class),Mockito.eq(null))).thenReturn(targetList);

            ResponseEntity<PcqRecordWithoutCaseResponse> actual = consolidationController
                .getPcqRecordWithoutCase(mockHeaders);

            assertNotNull(actual, RESPONSE_NULL_MSG);
            assertEquals(HttpStatus.OK, actual.getStatusCode(), STATUS_CODE_MSG);

            PcqRecordWithoutCaseResponse actualBody = actual.getBody();
            assertNotNull(actualBody, BODY_NULL_MSG);
            assertEquals(HTTP_OK, actualBody.getResponseStatusCode(), MSG_1);
            assertEquals(SUCCESS_MSG, actualBody.getResponseStatus(), UNEXPECTED_RESPONSE_MSG);
            assertNotNull(actualBody.getPcqRecord(), "PcqAnswers are not null");

            verify(mockHeaders, times(1)).get(HEADER_KEY);
            verify(environment, times(1)).getProperty(HEADER_API_PROPERTY);
            verify(environment, times(1)).getProperty(NUMBER_OF_DAYS_PROPERTY);
            verify(protectedCharacteristicsRepository, times(1))
                .findByCaseIdIsNullAndCompletedDateGreaterThan(any(Timestamp.class),Mockito.eq(null));

        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage());
        }

    }

    /**
     * This method tests the getPcqRecordWithoutCase API when it is called with all valid parameters and
     * the response contains empty array (no pcq ids). The response status code will be 200.
     */
    @DisplayName("Should return with an Success Request error code 200 and no pcq ids")
    @Test
    void testGetPcqWithoutCaseNoPcqIds()  {

        try {

            when(environment.getProperty(HEADER_API_PROPERTY)).thenReturn(HEADER_KEY);
            HttpHeaders mockHeaders = getMockHeader();
            when(mockHeaders.get(HEADER_KEY)).thenReturn(getTestHeader());
            when(environment.getProperty(NUMBER_OF_DAYS_PROPERTY)).thenReturn(DAYS_LIMIT);

            List<ProtectedCharacteristics> targetList = generateTargetList(0);
            when(protectedCharacteristicsRepository.findByCaseIdIsNullAndCompletedDateGreaterThan(any(
                Timestamp.class),Mockito.eq(null))).thenReturn(targetList);

            ResponseEntity<PcqRecordWithoutCaseResponse> actual = consolidationController
                .getPcqRecordWithoutCase(mockHeaders);

            assertNotNull(actual, RESPONSE_NULL_MSG);
            assertEquals(HttpStatus.OK, actual.getStatusCode(), STATUS_CODE_MSG);

            PcqRecordWithoutCaseResponse actualBody = actual.getBody();
            assertNotNull(actualBody, BODY_NULL_MSG);
            assertEquals(HTTP_OK, actualBody.getResponseStatusCode(), MSG_1);
            assertEquals(SUCCESS_MSG, actualBody.getResponseStatus(), UNEXPECTED_RESPONSE_MSG);
            assertNotNull(actualBody.getPcqRecord(), "PcqAnswers are null");
            assertArrayContents(targetList, actualBody.getPcqRecord());

            verify(mockHeaders, times(1)).get(HEADER_KEY);
            verify(environment, times(1)).getProperty(HEADER_API_PROPERTY);
            verify(environment, times(1)).getProperty(NUMBER_OF_DAYS_PROPERTY);
            verify(protectedCharacteristicsRepository, times(1))
                .findByCaseIdIsNullAndCompletedDateGreaterThan(any(Timestamp.class),Mockito.eq(null));

        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage());
        }

    }

    /**
     * This method tests the getPcqRecordWithoutCase API when it is called with all valid parameters and
     * but unforeseen error occurs. The response status code will be 500.
     */
    @DisplayName("Should return with an Unrecoverable Request error code 500 and no pcq ids")
    @Test
    void testGetPcqWithoutCaseInternalError()  {

        try {

            when(environment.getProperty(HEADER_API_PROPERTY)).thenReturn(HEADER_KEY);
            HttpHeaders mockHeaders = getMockHeader();
            when(mockHeaders.get(HEADER_KEY)).thenReturn(getTestHeader());
            when(environment.getProperty(NUMBER_OF_DAYS_PROPERTY)).thenReturn(DAYS_LIMIT);

            when(protectedCharacteristicsRepository.findByCaseIdIsNullAndCompletedDateGreaterThan(any(
                Timestamp.class),Mockito.eq(null))).thenThrow(NullPointerException.class);

            ResponseEntity<PcqRecordWithoutCaseResponse> actual = consolidationController
                .getPcqRecordWithoutCase(mockHeaders);

            assertNotNull(actual, RESPONSE_NULL_MSG);
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actual.getStatusCode(), "Expected 500 status code");

            PcqRecordWithoutCaseResponse actualBody = actual.getBody();
            assertNotNull(actualBody, BODY_NULL_MSG);
            assertEquals("500", actualBody.getResponseStatusCode(), "Expected 500 status");
            assertEquals(UNKNOWN_ERROR_MSG, actualBody.getResponseStatus(), UNEXPECTED_RESPONSE_MSG);
            assertEquals(0, actualBody.getPcqRecord().length, EXPECTED_EMPTY_PCQIDS_MSG);

            List<ProtectedCharacteristics> targetList = generateTargetList(0);
            assertArrayContents(targetList, actualBody.getPcqRecord());

            verify(mockHeaders, times(1)).get(HEADER_KEY);
            verify(environment, times(1)).getProperty(HEADER_API_PROPERTY);
            verify(environment, times(1)).getProperty(NUMBER_OF_DAYS_PROPERTY);
            verify(protectedCharacteristicsRepository, times(1))
                .findByCaseIdIsNullAndCompletedDateGreaterThan(any(Timestamp.class),Mockito.eq(null));

        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage(), e);
        }

    }

    /**
     * This method tests the addCaseForPcq API when it is called with all valid parameters but the
     * header does not contain the required attribute. The response status code will be 400.
     */
    @DisplayName("Should return with an Invalid Request error code 400")
    @Test
    void testAddCaseForPcqForMissingHeader()  {

        try {

            when(environment.getProperty(HEADER_API_PROPERTY)).thenReturn(HEADER_KEY);
            HttpHeaders mockHeaders = mock(HttpHeaders.class);
            when(mockHeaders.get(HEADER_KEY)).thenReturn(null);

            ResponseEntity<SubmitResponse> actual = consolidationController.addCaseForPcq(mockHeaders, TEST_PCQ_ID,
                                                                                          TEST_CASE_ID);

            assertNotNull(actual, RESPONSE_NULL_MSG);
            assertEquals(HttpStatus.BAD_REQUEST, actual.getStatusCode(), EXPECTED_NOT_FOUND_MSG);

            SubmitResponse actualBody = actual.getBody();
            assertEquals(HTTP_NOT_FOUND, actualBody.getResponseStatusCode(), EXPECTED_400_MSG);
            assertEquals(API_ERROR_MESSAGE_BAD_REQUEST, actualBody.getResponseStatus(), UNEXPECTED_RESPONSE_MSG);

            verify(mockHeaders, times(1)).get(HEADER_KEY);
            verify(environment, times(1)).getProperty(HEADER_API_PROPERTY);

        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage());
        }

    }

    /**
     * This method tests the addCaseForPcq API when it is called with all valid parameters and
     * the database is updated. The response status code will be 200.
     */
    @DisplayName("Should return with an Success Request error code 200 for successful add operation")
    @Test
    void testAddCaseForPcqSuccess()  {

        try {

            when(environment.getProperty(HEADER_API_PROPERTY)).thenReturn(HEADER_KEY);
            HttpHeaders mockHeaders = getMockHeader();
            when(mockHeaders.get(HEADER_KEY)).thenReturn(getTestHeader());
            when(protectedCharacteristicsRepository.updateCase(TEST_CASE_ID, TEST_PCQ_ID)).thenReturn(1);

            ResponseEntity<SubmitResponse> actual = consolidationController.addCaseForPcq(mockHeaders, TEST_PCQ_ID,
                                                                                          TEST_CASE_ID);
            assertNotNull(actual, RESPONSE_NULL_MSG);
            assertEquals(HttpStatus.OK, actual.getStatusCode(), STATUS_CODE_MSG);

            SubmitResponse actualBody = actual.getBody();
            assertNotNull(actualBody, BODY_NULL_MSG);
            assertEquals(HTTP_OK, actualBody.getResponseStatusCode(), MSG_1);
            assertEquals(API_ERROR_MESSAGE_UPDATED, actualBody.getResponseStatus(), UNEXPECTED_RESPONSE_MSG);
            assertNotNull(actualBody.getPcqId(), "PcqIds are not null");

            verify(mockHeaders, times(1)).get(HEADER_KEY);
            verify(environment, times(1)).getProperty(HEADER_API_PROPERTY);
            verify(protectedCharacteristicsRepository, times(1))
                .updateCase(TEST_CASE_ID, TEST_PCQ_ID);

        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage());
        }

    }

    /**
     * This method tests the addCaseForPcq API when it is called with all valid parameters and
     * the database is not updated. The response status code will be 400.
     */
    @DisplayName("Should return with an Success Request error code 400 for un-successful add operation")
    @Test
    void testAddCaseForPcqFailure()  {

        try {

            when(environment.getProperty(HEADER_API_PROPERTY)).thenReturn(HEADER_KEY);
            HttpHeaders mockHeaders = getMockHeader();
            when(mockHeaders.get(HEADER_KEY)).thenReturn(getTestHeader());
            when(protectedCharacteristicsRepository.updateCase(TEST_CASE_ID, TEST_PCQ_ID)).thenReturn(0);

            ResponseEntity<SubmitResponse> actual = consolidationController.addCaseForPcq(mockHeaders, TEST_PCQ_ID,
                                                                                          TEST_CASE_ID);
            assertNotNull(actual, RESPONSE_NULL_MSG);
            assertEquals(HttpStatus.BAD_REQUEST, actual.getStatusCode(), EXPECTED_NOT_FOUND_MSG);

            SubmitResponse actualBody = actual.getBody();
            assertNotNull(actualBody, BODY_NULL_MSG);
            assertEquals(HTTP_NOT_FOUND, actualBody.getResponseStatusCode(), EXPECTED_400_MSG);
            assertEquals(API_ERROR_MESSAGE_BAD_REQUEST, actualBody.getResponseStatus(), UNEXPECTED_RESPONSE_MSG);
            assertNotNull(actualBody.getPcqId(), "PcqIds are not null");

            verify(mockHeaders, times(1)).get(HEADER_KEY);
            verify(environment, times(1)).getProperty(HEADER_API_PROPERTY);
            verify(protectedCharacteristicsRepository, times(1))
                .updateCase(TEST_CASE_ID, TEST_PCQ_ID);

        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage());
        }

    }

    /**
     * This method tests the addCaseForPcq API when it is called with all valid parameters and
     * the database throws runtime exception. The response status code will be 500.
     */
    @DisplayName("Should return with an Internal Server Request error code 500 for un-successful add operation")
    @Test
    void testAddCaseForPcqInternalServerError()  {

        try {

            when(environment.getProperty(HEADER_API_PROPERTY)).thenReturn(HEADER_KEY);
            HttpHeaders mockHeaders = getMockHeader();
            when(mockHeaders.get(HEADER_KEY)).thenReturn(getTestHeader());
            when(protectedCharacteristicsRepository.updateCase(TEST_CASE_ID, TEST_PCQ_ID)).thenThrow(
                NullPointerException.class);

            ResponseEntity<SubmitResponse> actual = consolidationController.addCaseForPcq(mockHeaders, TEST_PCQ_ID,
                                                                                          TEST_CASE_ID);
            assertNotNull(actual, RESPONSE_NULL_MSG);
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actual.getStatusCode(), "Expected 500 status code");

            SubmitResponse actualBody = actual.getBody();
            assertNotNull(actualBody, BODY_NULL_MSG);
            assertEquals("500", actualBody.getResponseStatusCode(), "Expected 500 status");
            assertEquals(UNKNOWN_ERROR_MSG, actualBody.getResponseStatus(), UNEXPECTED_RESPONSE_MSG);

            verify(mockHeaders, times(1)).get(HEADER_KEY);
            verify(environment, times(1)).getProperty(HEADER_API_PROPERTY);
            verify(protectedCharacteristicsRepository, times(1))
                .updateCase(TEST_CASE_ID, TEST_PCQ_ID);

        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage());
        }

    }

    /**
     * This method tests the getPcqRecordWithoutCase API when it is called with all valid parameters but the
     * header does not contain the required attribute. The response status code will be 400.
     */
    @DisplayName("Should return with an Invalid Request error code 400")
    @Test
    void testGetPcqRecordWithoutCaseForMissingHeader()  {

        try {

            when(environment.getProperty(HEADER_API_PROPERTY)).thenReturn(HEADER_KEY);
            HttpHeaders mockHeaders = mock(HttpHeaders.class);
            when(mockHeaders.get(HEADER_KEY)).thenReturn(null);

            ResponseEntity<PcqRecordWithoutCaseResponse> actual = consolidationController.getPcqRecordWithoutCase(
                mockHeaders);

            assertNotNull(actual, RESPONSE_NULL_MSG);
            assertEquals(HttpStatus.BAD_REQUEST, actual.getStatusCode(), EXPECTED_NOT_FOUND_MSG);

            PcqRecordWithoutCaseResponse actualBody = actual.getBody();
            assertEquals(HTTP_NOT_FOUND, actualBody.getResponseStatusCode(), EXPECTED_400_MSG);
            assertEquals(API_ERROR_MESSAGE_BAD_REQUEST, actualBody.getResponseStatus(), UNEXPECTED_RESPONSE_MSG);

            verify(mockHeaders, times(1)).get(HEADER_KEY);
            verify(environment, times(1)).getProperty(HEADER_API_PROPERTY);

        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage());
        }

    }

    /**
     * This method tests the getPcqRecordWithoutCase API when it is called with all valid parameters and
     * the response contains multiple pcq records. The response status code will be 200.
     */
    @DisplayName("Should return with an Success Request error code 200 and multiple pcq records")
    @Test
    void testGetPcqRecordWithoutCaseMultipleIds()  {

        try {

            when(environment.getProperty(HEADER_API_PROPERTY)).thenReturn(HEADER_KEY);
            HttpHeaders mockHeaders = getMockHeader();
            when(mockHeaders.get(HEADER_KEY)).thenReturn(getTestHeader());
            when(environment.getProperty(NUMBER_OF_DAYS_PROPERTY)).thenReturn(DAYS_LIMIT);

            List<ProtectedCharacteristics> targetList = generateTargetList(3);
            when(protectedCharacteristicsRepository.findByCaseIdIsNullAndCompletedDateGreaterThan(any(
                Timestamp.class),Mockito.eq(null))).thenReturn(targetList);

            ResponseEntity<PcqRecordWithoutCaseResponse> actual = consolidationController.getPcqRecordWithoutCase(
                mockHeaders);

            assertNotNull(actual, RESPONSE_NULL_MSG);
            assertEquals(HttpStatus.OK, actual.getStatusCode(), STATUS_CODE_MSG);

            PcqRecordWithoutCaseResponse actualBody = actual.getBody();
            assertNotNull(actualBody, BODY_NULL_MSG);
            assertEquals(HTTP_OK, actualBody.getResponseStatusCode(), MSG_1);
            assertEquals(SUCCESS_MSG, actualBody.getResponseStatus(), UNEXPECTED_RESPONSE_MSG);
            assertNotNull(actualBody.getPcqRecord(), "PcqRecords are null");
            assertArrayContents(targetList, actualBody.getPcqRecord());

            verify(mockHeaders, times(1)).get(HEADER_KEY);
            verify(environment, times(1)).getProperty(HEADER_API_PROPERTY);
            verify(environment, times(1)).getProperty(NUMBER_OF_DAYS_PROPERTY);
            verify(protectedCharacteristicsRepository, times(1))
                .findByCaseIdIsNullAndCompletedDateGreaterThan(any(Timestamp.class),
                Mockito.eq(null));

        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage());
        }

    }

    /**
     * This method tests the getPcqRecordWithoutCase API when it is called with all valid parameters and
     * the response contains array with single pcq record. The response status code will be 200.
     */
    @DisplayName("Should return with an Success Request error code 200 and single pcq record")
    @Test
    void testGetPcqRecordWithoutCaseSingleId()  {

        try {

            when(environment.getProperty(HEADER_API_PROPERTY)).thenReturn(HEADER_KEY);
            HttpHeaders mockHeaders = getMockHeader();
            when(mockHeaders.get(HEADER_KEY)).thenReturn(getTestHeader());
            when(environment.getProperty(NUMBER_OF_DAYS_PROPERTY)).thenReturn(DAYS_LIMIT);

            List<ProtectedCharacteristics> targetList = generateTargetList(1);
            when(protectedCharacteristicsRepository.findByCaseIdIsNullAndCompletedDateGreaterThan(any(
                Timestamp.class),Mockito.eq(null))).thenReturn(targetList);

            ResponseEntity<PcqRecordWithoutCaseResponse> actual = consolidationController.getPcqRecordWithoutCase(
                mockHeaders);

            assertNotNull(actual, RESPONSE_NULL_MSG);
            assertEquals(HttpStatus.OK, actual.getStatusCode(), STATUS_CODE_MSG);

            PcqRecordWithoutCaseResponse actualBody = actual.getBody();
            assertNotNull(actualBody, BODY_NULL_MSG);
            assertEquals(HTTP_OK, actualBody.getResponseStatusCode(), MSG_1);
            assertEquals(SUCCESS_MSG, actualBody.getResponseStatus(), UNEXPECTED_RESPONSE_MSG);
            assertNotNull(actualBody.getPcqRecord(), "Pcq Record are null");
            assertArrayContents(targetList, actualBody.getPcqRecord());

            verify(mockHeaders, times(1)).get(HEADER_KEY);
            verify(environment, times(1)).getProperty(HEADER_API_PROPERTY);
            verify(environment, times(1)).getProperty(NUMBER_OF_DAYS_PROPERTY);
            verify(protectedCharacteristicsRepository, times(1))
                .findByCaseIdIsNullAndCompletedDateGreaterThan(any(Timestamp.class),Mockito.eq(null));

        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage());
        }

    }

    /**
     * This method tests the getPcqRecordWithoutCase API when it is called with all valid parameters and
     * the response contains empty array (no pcq records). The response status code will be 200.
     */
    @DisplayName("Should return with an Success Request error code 200 and no pcq ids")
    @Test
    void testGetPcqRecordWithoutCaseNoPcqRecords()  {

        try {

            when(environment.getProperty(HEADER_API_PROPERTY)).thenReturn(HEADER_KEY);
            HttpHeaders mockHeaders = getMockHeader();
            when(mockHeaders.get(HEADER_KEY)).thenReturn(getTestHeader());
            when(environment.getProperty(NUMBER_OF_DAYS_PROPERTY)).thenReturn(DAYS_LIMIT);

            List<ProtectedCharacteristics> targetList = generateTargetList(0);
            when(protectedCharacteristicsRepository.findByCaseIdIsNullAndCompletedDateGreaterThan(any(
                Timestamp.class),Mockito.eq(null))).thenReturn(targetList);

            ResponseEntity<PcqRecordWithoutCaseResponse> actual = consolidationController.getPcqRecordWithoutCase(
                mockHeaders);

            assertNotNull(actual, RESPONSE_NULL_MSG);
            assertEquals(HttpStatus.OK, actual.getStatusCode(), STATUS_CODE_MSG);

            PcqRecordWithoutCaseResponse actualBody = actual.getBody();
            assertNotNull(actualBody, BODY_NULL_MSG);
            assertEquals(HTTP_OK, actualBody.getResponseStatusCode(), MSG_1);
            assertEquals(SUCCESS_MSG, actualBody.getResponseStatus(), UNEXPECTED_RESPONSE_MSG);
            assertNotNull(actualBody.getPcqRecord(), "Pcq Records are null");
            assertArrayContents(targetList, actualBody.getPcqRecord());

            verify(mockHeaders, times(1)).get(HEADER_KEY);
            verify(environment, times(1)).getProperty(HEADER_API_PROPERTY);
            verify(environment, times(1)).getProperty(NUMBER_OF_DAYS_PROPERTY);
            verify(protectedCharacteristicsRepository, times(1))
                .findByCaseIdIsNullAndCompletedDateGreaterThan(any(Timestamp.class),Mockito.eq(null));

        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage());
        }

    }

    /**
     * This method tests the getPcqRecordWithoutCase API when it is called with all valid parameters and
     * but unforeseen error occurs. The response status code will be 500.
     */
    @DisplayName("Should return with an Unrecoverable Request error code 500 and no pcq ids")
    @Test
    void testGetPcqRecordWithoutCaseInternalError()  {

        try {

            when(environment.getProperty(HEADER_API_PROPERTY)).thenReturn(HEADER_KEY);
            HttpHeaders mockHeaders = getMockHeader();
            when(mockHeaders.get(HEADER_KEY)).thenReturn(getTestHeader());
            when(environment.getProperty(NUMBER_OF_DAYS_PROPERTY)).thenReturn(DAYS_LIMIT);

            when(protectedCharacteristicsRepository.findByCaseIdIsNullAndCompletedDateGreaterThan(any(
                Timestamp.class),Mockito.eq(null))).thenThrow(NullPointerException.class);

            ResponseEntity<PcqRecordWithoutCaseResponse> actual = consolidationController.getPcqRecordWithoutCase(
                mockHeaders);

            assertNotNull(actual, RESPONSE_NULL_MSG);
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actual.getStatusCode(), "Expected 500 status code");

            PcqRecordWithoutCaseResponse actualBody = actual.getBody();
            assertNotNull(actualBody, BODY_NULL_MSG);
            assertEquals("500", actualBody.getResponseStatusCode(), "Expected 500 status");
            assertEquals(UNKNOWN_ERROR_MSG, actualBody.getResponseStatus(), UNEXPECTED_RESPONSE_MSG);
            assertEquals(0, actualBody.getPcqRecord().length, EXPECTED_EMPTY_PCQRCORDS_MSG);
            List<ProtectedCharacteristics> targetList = generateTargetList(0);
            assertArrayContents(targetList, actualBody.getPcqRecord());

            verify(mockHeaders, times(1)).get(HEADER_KEY);
            verify(environment, times(1)).getProperty(HEADER_API_PROPERTY);
            verify(environment, times(1)).getProperty(NUMBER_OF_DAYS_PROPERTY);
            verify(protectedCharacteristicsRepository, times(1))
                .findByCaseIdIsNullAndCompletedDateGreaterThan(any(Timestamp.class),Mockito.eq(null));

        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage(), e);
        }

    }

    private HttpHeaders getMockHeader() {
        HttpHeaders mockHeader = mock(HttpHeaders.class);
        mockHeader.set("HTTP_X-Correlation-Id", CO_RELATION_ID_FOR_TEST);

        return mockHeader;
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public static List<ProtectedCharacteristics> generateTargetList(int rowCount) {
        List<ProtectedCharacteristics> targetList = new ArrayList<>(rowCount);
        for (int i = 0; i < rowCount; i++) {
            ProtectedCharacteristics targetObj = new ProtectedCharacteristics();
            targetObj.setPcqId("TEST - " + i);
            targetObj.setServiceId("TEST_SERVICE_" + i);
            targetObj.setActor("TEST_ACTOR_" + i);
            targetObj.setDcnNumber("DCN_NUMBER_" + i);
            targetList.add(targetObj);
        }

        return targetList;
    }

    public static void assertArrayContents(List<ProtectedCharacteristics> targetList,
                                           String... actualList) {
        for (int i = 0; i < targetList.size(); i++) {
            assertEquals(targetList.get(i).getPcqId(), actualList[i], "Pcq Id is not matching");
        }
    }

    public static void assertArrayContents(List<ProtectedCharacteristics> targetList,
                                          PcqAnswerResponse... actualList) {
        for (int i = 0; i < targetList.size(); i++) {
            assertEquals(targetList.get(i).getPcqId(), actualList[i].getPcqId(), "Pcq Id is not matching");
            assertEquals(targetList.get(i).getServiceId(), actualList[i].getServiceId(), "Service Id is not matching");
            assertEquals(targetList.get(i).getActor(), actualList[i].getActor(), "Actor is not matching");
            assertEquals(targetList.get(i).getDcnNumber(), actualList[i].getDcnNumber(),
                         "DCN Number is not matching");
        }
    }

}

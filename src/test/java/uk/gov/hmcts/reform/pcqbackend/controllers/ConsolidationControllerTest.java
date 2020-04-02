package uk.gov.hmcts.reform.pcqbackend.controllers;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.pcqbackend.domain.ProtectedCharacteristics;
import uk.gov.hmcts.reform.pcqbackend.model.PcqWithoutCaseResponse;
import uk.gov.hmcts.reform.pcqbackend.model.SubmitResponse;
import uk.gov.hmcts.reform.pcqbackend.repository.ProtectedCharacteristicsRepository;
import uk.gov.hmcts.reform.pcqbackend.service.ConsolidationService;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Slf4j
@SuppressWarnings({"PMD.ExcessiveImports", "PMD.TooManyMethods"})
public class ConsolidationControllerTest {

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

    @BeforeEach
    public void setUp() {
        this.environment = mock(Environment.class);
        this.protectedCharacteristicsRepository = mock(ProtectedCharacteristicsRepository.class);
        ConsolidationService consolidationService = new ConsolidationService(
            protectedCharacteristicsRepository,
            environment
        );
        this.consolidationController = new ConsolidationController(environment, consolidationService);
        MockitoAnnotations.initMocks(this);

        when(environment.getProperty(INVALID_ERROR_PROPERTY)).thenReturn(INVALID_ERROR);
        when(environment.getProperty(UPDATE_MSG_PROPERTY)).thenReturn(API_ERROR_MESSAGE_UPDATED);
        when(environment.getProperty("api-error-messages.accepted")).thenReturn(SUCCESS_MSG);
        when(environment.getProperty("api-error-messages.internal_error")).thenReturn("Unknown error occurred");
    }

    /**
     * This method tests the getPcqWithoutCase API when it is called with all valid parameters but the
     * header does not contain the required attribute. The response status code will be 400.
     */
    @DisplayName("Should return with an Invalid Request error code 400")
    @Test
    public void testGetPcqWithoutCaseForMissingHeader()  {

        try {

            when(environment.getProperty(HEADER_API_PROPERTY)).thenReturn(HEADER_KEY);
            HttpHeaders mockHeaders = mock(HttpHeaders.class);
            when(mockHeaders.get(HEADER_KEY)).thenReturn(null);

            ResponseEntity<PcqWithoutCaseResponse> actual = consolidationController.getPcqIdsWithoutCase(mockHeaders);

            assertNotNull(actual, RESPONSE_NULL_MSG);
            assertEquals(HttpStatus.BAD_REQUEST, actual.getStatusCode(), "Expected 400 status code");

            PcqWithoutCaseResponse actualBody = actual.getBody();
            assertEquals("400", actualBody.getResponseStatusCode(), "Expected 400 status");
            assertEquals(API_ERROR_MESSAGE_BAD_REQUEST, actualBody.getResponseStatus(), UNEXPECTED_RESPONSE_MSG);

            verify(mockHeaders, times(1)).get(HEADER_KEY);
            verify(environment, times(1)).getProperty(HEADER_API_PROPERTY);

        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage());
        }

    }

    /**
     * This method tests the getPcqWithoutCase API when it is called with all valid parameters and
     * the response contains multiple pcq Ids. The response status code will be 200.
     */
    @DisplayName("Should return with an Success Request error code 200 and multiple pcq ids")
    @Test
    public void testGetPcqWithoutCaseMultipleIds()  {

        try {

            when(environment.getProperty(HEADER_API_PROPERTY)).thenReturn(HEADER_KEY);
            HttpHeaders mockHeaders = getMockHeader();
            when(mockHeaders.get(HEADER_KEY)).thenReturn(getTestHeader());
            when(environment.getProperty(NUMBER_OF_DAYS_PROPERTY)).thenReturn("90");

            List<ProtectedCharacteristics> targetList = generateTargetList(3);
            when(protectedCharacteristicsRepository.findByCaseIdIsNullAndCompletedDateGreaterThan(any(
                Timestamp.class))).thenReturn(targetList);

            ResponseEntity<PcqWithoutCaseResponse> actual = consolidationController.getPcqIdsWithoutCase(mockHeaders);

            assertNotNull(actual, RESPONSE_NULL_MSG);
            assertEquals(HttpStatus.OK, actual.getStatusCode(), STATUS_CODE_MSG);

            PcqWithoutCaseResponse actualBody = actual.getBody();
            assertNotNull(actualBody, BODY_NULL_MSG);
            assertEquals(HTTP_OK, actualBody.getResponseStatusCode(), MSG_1);
            assertEquals(SUCCESS_MSG, actualBody.getResponseStatus(), UNEXPECTED_RESPONSE_MSG);
            assertNotNull(actualBody.getPcqId(), "PcqIds are null");
            assertArrayContents(targetList, actualBody.getPcqId());

            verify(mockHeaders, times(1)).get(HEADER_KEY);
            verify(environment, times(1)).getProperty(HEADER_API_PROPERTY);
            verify(environment, times(1)).getProperty(NUMBER_OF_DAYS_PROPERTY);
            verify(protectedCharacteristicsRepository, times(1))
                .findByCaseIdIsNullAndCompletedDateGreaterThan(any(Timestamp.class));

        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage());
        }

    }

    /**
     * This method tests the getPcqWithoutCase API when it is called with all valid parameters and
     * the response contains array with single pcq Id. The response status code will be 200.
     */
    @DisplayName("Should return with an Success Request error code 200 and single pcq id")
    @Test
    public void testGetPcqWithoutCaseSingleId()  {

        try {

            when(environment.getProperty(HEADER_API_PROPERTY)).thenReturn(HEADER_KEY);
            HttpHeaders mockHeaders = getMockHeader();
            when(mockHeaders.get(HEADER_KEY)).thenReturn(getTestHeader());
            when(environment.getProperty(NUMBER_OF_DAYS_PROPERTY)).thenReturn("90");

            List<ProtectedCharacteristics> targetList = generateTargetList(1);
            when(protectedCharacteristicsRepository.findByCaseIdIsNullAndCompletedDateGreaterThan(any(
                Timestamp.class))).thenReturn(targetList);

            ResponseEntity<PcqWithoutCaseResponse> actual = consolidationController.getPcqIdsWithoutCase(mockHeaders);

            assertNotNull(actual, RESPONSE_NULL_MSG);
            assertEquals(HttpStatus.OK, actual.getStatusCode(), STATUS_CODE_MSG);

            PcqWithoutCaseResponse actualBody = actual.getBody();
            assertNotNull(actualBody, BODY_NULL_MSG);
            assertEquals(HTTP_OK, actualBody.getResponseStatusCode(), MSG_1);
            assertEquals(SUCCESS_MSG, actualBody.getResponseStatus(), UNEXPECTED_RESPONSE_MSG);
            assertNotNull(actualBody.getPcqId(), "PcqIds are not null");

            verify(mockHeaders, times(1)).get(HEADER_KEY);
            verify(environment, times(1)).getProperty(HEADER_API_PROPERTY);
            verify(environment, times(1)).getProperty(NUMBER_OF_DAYS_PROPERTY);
            verify(protectedCharacteristicsRepository, times(1))
                .findByCaseIdIsNullAndCompletedDateGreaterThan(any(Timestamp.class));

        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage());
        }

    }

    /**
     * This method tests the getPcqWithoutCase API when it is called with all valid parameters and
     * the response contains empty array (no pcq ids). The response status code will be 200.
     */
    @DisplayName("Should return with an Success Request error code 200 and no pcq ids")
    @Test
    public void testGetPcqWithoutCaseNoPcqIds()  {

        try {

            when(environment.getProperty(HEADER_API_PROPERTY)).thenReturn(HEADER_KEY);
            HttpHeaders mockHeaders = getMockHeader();
            when(mockHeaders.get(HEADER_KEY)).thenReturn(getTestHeader());
            when(environment.getProperty(NUMBER_OF_DAYS_PROPERTY)).thenReturn("90");

            List<ProtectedCharacteristics> targetList = generateTargetList(0);
            when(protectedCharacteristicsRepository.findByCaseIdIsNullAndCompletedDateGreaterThan(any(
                Timestamp.class))).thenReturn(targetList);

            ResponseEntity<PcqWithoutCaseResponse> actual = consolidationController.getPcqIdsWithoutCase(mockHeaders);

            assertNotNull(actual, RESPONSE_NULL_MSG);
            assertEquals(HttpStatus.OK, actual.getStatusCode(), STATUS_CODE_MSG);

            PcqWithoutCaseResponse actualBody = actual.getBody();
            assertNotNull(actualBody, BODY_NULL_MSG);
            assertEquals(HTTP_OK, actualBody.getResponseStatusCode(), MSG_1);
            assertEquals(SUCCESS_MSG, actualBody.getResponseStatus(), UNEXPECTED_RESPONSE_MSG);
            assertNotNull(actualBody.getPcqId(), "PcqIds are null");
            assertArrayContents(targetList, actualBody.getPcqId());

            verify(mockHeaders, times(1)).get(HEADER_KEY);
            verify(environment, times(1)).getProperty(HEADER_API_PROPERTY);
            verify(environment, times(1)).getProperty(NUMBER_OF_DAYS_PROPERTY);
            verify(protectedCharacteristicsRepository, times(1))
                .findByCaseIdIsNullAndCompletedDateGreaterThan(any(Timestamp.class));

        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage());
        }

    }

    /**
     * This method tests the getPcqWithoutCase API when it is called with all valid parameters and
     * but unforeseen error occurs. The response status code will be 500.
     */
    @DisplayName("Should return with an Unrecoverable Request error code 500 and no pcq ids")
    @Test
    public void testGetPcqWithoutCaseInternalError()  {

        try {

            when(environment.getProperty(HEADER_API_PROPERTY)).thenReturn(HEADER_KEY);
            HttpHeaders mockHeaders = getMockHeader();
            when(mockHeaders.get(HEADER_KEY)).thenReturn(getTestHeader());
            when(environment.getProperty(NUMBER_OF_DAYS_PROPERTY)).thenReturn("90");

            when(protectedCharacteristicsRepository.findByCaseIdIsNullAndCompletedDateGreaterThan(any(
                Timestamp.class))).thenThrow(NullPointerException.class);

            ResponseEntity<PcqWithoutCaseResponse> actual = consolidationController.getPcqIdsWithoutCase(mockHeaders);

            assertNotNull(actual, RESPONSE_NULL_MSG);
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actual.getStatusCode(), "Expected 500 status code");

            PcqWithoutCaseResponse actualBody = actual.getBody();
            assertNotNull(actualBody, BODY_NULL_MSG);
            assertEquals("500", actualBody.getResponseStatusCode(), "Expected 500 status");
            assertEquals("Unknown error occurred", actualBody.getResponseStatus(), UNEXPECTED_RESPONSE_MSG);
            assertNull(actualBody.getPcqId(), "PcqIds are null");
            List<ProtectedCharacteristics> targetList = generateTargetList(0);
            assertArrayContents(targetList, actualBody.getPcqId());

            verify(mockHeaders, times(1)).get(HEADER_KEY);
            verify(environment, times(1)).getProperty(HEADER_API_PROPERTY);
            verify(environment, times(1)).getProperty(NUMBER_OF_DAYS_PROPERTY);
            verify(protectedCharacteristicsRepository, times(1))
                .findByCaseIdIsNullAndCompletedDateGreaterThan(any(Timestamp.class));

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
    public void testAddCaseForPcqForMissingHeader()  {

        try {

            when(environment.getProperty(HEADER_API_PROPERTY)).thenReturn(HEADER_KEY);
            HttpHeaders mockHeaders = mock(HttpHeaders.class);
            when(mockHeaders.get(HEADER_KEY)).thenReturn(null);

            ResponseEntity<SubmitResponse> actual = consolidationController.addCaseForPcq(mockHeaders, TEST_PCQ_ID,
                                                                                          TEST_CASE_ID);

            assertNotNull(actual, RESPONSE_NULL_MSG);
            assertEquals(HttpStatus.BAD_REQUEST, actual.getStatusCode(), "Expected 400 status code");

            SubmitResponse actualBody = actual.getBody();
            assertEquals("400", actualBody.getResponseStatusCode(), "Expected 400 status");
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
    public void testAddCaseForPcqSuccess()  {

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
    public void testAddCaseForPcqFailure()  {

        try {

            when(environment.getProperty(HEADER_API_PROPERTY)).thenReturn(HEADER_KEY);
            HttpHeaders mockHeaders = getMockHeader();
            when(mockHeaders.get(HEADER_KEY)).thenReturn(getTestHeader());
            when(protectedCharacteristicsRepository.updateCase(TEST_CASE_ID, TEST_PCQ_ID)).thenReturn(0);

            ResponseEntity<SubmitResponse> actual = consolidationController.addCaseForPcq(mockHeaders, TEST_PCQ_ID,
                                                                                          TEST_CASE_ID);
            assertNotNull(actual, RESPONSE_NULL_MSG);
            assertEquals(HttpStatus.BAD_REQUEST, actual.getStatusCode(), "Expected 400 status code");

            SubmitResponse actualBody = actual.getBody();
            assertNotNull(actualBody, BODY_NULL_MSG);
            assertEquals("400", actualBody.getResponseStatusCode(), "Expected 400 status");
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
    public void testAddCaseForPcqInternalServerError()  {

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
            assertEquals("Unknown error occurred", actualBody.getResponseStatus(), UNEXPECTED_RESPONSE_MSG);

            verify(mockHeaders, times(1)).get(HEADER_KEY);
            verify(environment, times(1)).getProperty(HEADER_API_PROPERTY);
            verify(protectedCharacteristicsRepository, times(1))
                .updateCase(TEST_CASE_ID, TEST_PCQ_ID);

        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage());
        }

    }

    private HttpHeaders getMockHeader() {
        HttpHeaders mockHeader = mock(HttpHeaders.class);
        mockHeader.set("HTTP_X-Correlation-Id", CO_RELATION_ID_FOR_TEST);

        return mockHeader;
    }

    public static List<String> getTestHeader() {
        List<String> headerList =  new ArrayList<>();
        headerList.add(CO_RELATION_ID_FOR_TEST);

        return headerList;
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public static List<ProtectedCharacteristics> generateTargetList(int rowCount) {
        List<ProtectedCharacteristics> targetList = new ArrayList<>(rowCount);
        for (int i = 0; i < rowCount; i++) {
            ProtectedCharacteristics targetObj = new ProtectedCharacteristics();
            targetObj.setPcqId("TEST - " + i);
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

}

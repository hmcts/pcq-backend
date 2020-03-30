package uk.gov.hmcts.reform.pcqbackend.controllers;

import com.microsoft.applicationinsights.core.dependencies.google.api.Http;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.pcqbackend.domain.ProtectedCharacteristics;
import uk.gov.hmcts.reform.pcqbackend.model.PcqAnswerRequest;
import uk.gov.hmcts.reform.pcqbackend.model.PcqAnswerResponse;
import uk.gov.hmcts.reform.pcqbackend.model.PcqWithoutCaseResponse;
import uk.gov.hmcts.reform.pcqbackend.repository.ProtectedCharacteristicsRepository;
import uk.gov.hmcts.reform.pcqbackend.service.ConsolidationService;
import uk.gov.hmcts.reform.pcqbackend.service.SubmitAnswersService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Slf4j
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
        when(environment.getProperty("api-error-messages.accepted")).thenReturn("Success");
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
            assertEquals(HttpStatus.BAD_REQUEST.value(), Integer.parseInt(actualBody.getResponseStatusCode()),
                         "Expected 400 status");
            assertEquals(API_ERROR_MESSAGE_BAD_REQUEST, actualBody.getResponseStatus(), "Not expected response");

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
            assertEquals(HttpStatus.OK, actual.getStatusCode(), "Expected 200 status code");

            PcqWithoutCaseResponse actualBody = actual.getBody();
            assertNotNull(actualBody, "Actual Body is null");
            assertEquals(HttpStatus.OK.value(), Integer.parseInt(actualBody.getResponseStatusCode()),
                         "Expected 200 status");
            assertEquals("Success", actualBody.getResponseStatus(), "Not expected response");
            assertNotNull(actualBody.getPcqId(), "PcqIds are null");
            assertArrayContents(targetList, actualBody.getPcqId());

            verify(mockHeaders, times(1)).get(HEADER_KEY);
            verify(environment, times(1)).getProperty(HEADER_API_PROPERTY);
            verify(environment, times(1)).getProperty(NUMBER_OF_DAYS_PROPERTY);
            verify(protectedCharacteristicsRepository, times(1)).
                findByCaseIdIsNullAndCompletedDateGreaterThan(any(Timestamp.class));

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
            assertEquals(HttpStatus.OK, actual.getStatusCode(), "Expected 200 status code");

            PcqWithoutCaseResponse actualBody = actual.getBody();
            assertNotNull(actualBody, "Actual Body is null");
            assertEquals(HttpStatus.OK.value(), Integer.parseInt(actualBody.getResponseStatusCode()),
                         "Expected 200 status");
            assertEquals("Success", actualBody.getResponseStatus(), "Not expected response");
            assertNotNull(actualBody.getPcqId(), "PcqIds are null");
            assertArrayContents(targetList, actualBody.getPcqId());

            verify(mockHeaders, times(1)).get(HEADER_KEY);
            verify(environment, times(1)).getProperty(HEADER_API_PROPERTY);
            verify(environment, times(1)).getProperty(NUMBER_OF_DAYS_PROPERTY);
            verify(protectedCharacteristicsRepository, times(1)).
                findByCaseIdIsNullAndCompletedDateGreaterThan(any(Timestamp.class));

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
            assertEquals(HttpStatus.OK, actual.getStatusCode(), "Expected 200 status code");

            PcqWithoutCaseResponse actualBody = actual.getBody();
            assertNotNull(actualBody, "Actual Body is null");
            assertEquals(HttpStatus.OK.value(), Integer.parseInt(actualBody.getResponseStatusCode()),
                         "Expected 200 status");
            assertEquals("Success", actualBody.getResponseStatus(), "Not expected response");
            assertNotNull(actualBody.getPcqId(), "PcqIds are null");
            assertArrayContents(targetList, actualBody.getPcqId());

            verify(mockHeaders, times(1)).get(HEADER_KEY);
            verify(environment, times(1)).getProperty(HEADER_API_PROPERTY);
            verify(environment, times(1)).getProperty(NUMBER_OF_DAYS_PROPERTY);
            verify(protectedCharacteristicsRepository, times(1)).
                findByCaseIdIsNullAndCompletedDateGreaterThan(any(Timestamp.class));

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

    public static List<ProtectedCharacteristics> generateTargetList(int rowCount) {
        List<ProtectedCharacteristics> targetList = new ArrayList<>(rowCount);
        for (int i = 0; i < rowCount; i++) {
            ProtectedCharacteristics targetObj = new ProtectedCharacteristics();
            targetObj.setPcqId("TEST - "+i);
            targetList.add(targetObj);
        }

        return targetList;
    }

    public static void assertArrayContents(List<ProtectedCharacteristics> targetList,
                                          String[] actualList) {
        for (int i = 0; i < targetList.size(); i++) {
            assertEquals(targetList.get(i).getPcqId(), actualList[i], "Pcq Id is not matching");
        }
    }

}

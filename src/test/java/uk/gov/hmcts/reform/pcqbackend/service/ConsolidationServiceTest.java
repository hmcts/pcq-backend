package uk.gov.hmcts.reform.pcqbackend.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.pcq.commons.model.SubmitResponse;
import uk.gov.hmcts.reform.pcqbackend.domain.ProtectedCharacteristics;
import uk.gov.hmcts.reform.pcqbackend.exceptions.InvalidRequestException;
import uk.gov.hmcts.reform.pcqbackend.repository.ProtectedCharacteristicsRepository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"PMD.TooManyMethods"})
class ConsolidationServiceTest {

    @Mock
    private Environment environment;

    @Mock
    private ProtectedCharacteristicsRepository protectedCharacteristicsRepository;

    @InjectMocks
    private ConsolidationService consolidationService;

    private static final String CO_RELATION_ID_FOR_TEST = "Test-Id";
    private static final String ERROR_MSG_PREFIX = "Test failed because of exception during execution. Message is ";
    private static final String INVALID_ERROR = "Invalid Request";
    private static final String UPDATED_MSG = "Successfully updated";
    private static final String INVALID_ERROR_PROPERTY = "api-error-messages.bad_request";
    private static final String UPDATE_MSG_PROPERTY = "api-error-messages.updated";
    private static final String RESPONSE_NULL_MSG = "Response is null";
    private static final String RESPONSE_BODY_NULL_MSG = "Response Body is null";
    private static final String NUMBER_OF_DAYS_PROPERTY = "api-config-params.number_of_days_limit";
    private static final String NUMBER_OF_DAYS_PROPERTY_LESS_THAN = "api-config-params.number_of_days_less_than_limit";
    private static final String TEST_PCQ_ID = "T1234";
    private static final String TEST_CASE_ID = "CCD112";
    private static final String ERROR_MSG_1 = "Number of rows returned are incorrect.";
    private static final String ERROR_MSG_2 = "Method should not return null";

    @Test
    void testNoHeadersGetRequest() {

        try {
            consolidationService.getPcqsWithoutCase(null);
            fail("The method should have thrown InvalidRequestException");
        } catch (InvalidRequestException ive) {
            assertEquals("Invalid Request. Expecting required header - Co-Relation Id - in the request.",
                         ive.getMessage(), "Exception message not matching");
            assertEquals(HttpStatus.BAD_REQUEST, ive.getErrorCode(), "Http Status Code not matching");
        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage(), e);
        }

    }

    @DisplayName("Pcq Service test variations")
    @ParameterizedTest
    @ValueSource(ints = {3, 1, 0})
    void testPcqWithoutCaseReturnExpectedNumberOfIds(int expectedSize) throws InvalidRequestException {
        when(environment.getProperty(NUMBER_OF_DAYS_PROPERTY)).thenReturn("90");
        when(environment.getProperty(NUMBER_OF_DAYS_PROPERTY_LESS_THAN)).thenReturn("0");
        List<ProtectedCharacteristics> targetList = generateTargetList(expectedSize);
        when(protectedCharacteristicsRepository.findByCaseIdIsNullAndCompletedDateGreaterThanAndLessThan(any(
            Timestamp.class), any(Timestamp.class),Mockito.eq(null))).thenReturn(targetList);

        List<ProtectedCharacteristics> protectedCharacteristicsList = consolidationService.getPcqsWithoutCase(
            getTestHeader());

        assertNotNull(protectedCharacteristicsList, ERROR_MSG_2);
        assertEquals(expectedSize, protectedCharacteristicsList.size(), ERROR_MSG_1);
        assertListContents(targetList, protectedCharacteristicsList);

        verify(environment, times(1)).getProperty(NUMBER_OF_DAYS_PROPERTY);
        verify(protectedCharacteristicsRepository, times(1))
            .findByCaseIdIsNullAndCompletedDateGreaterThanAndLessThan(
            any(Timestamp.class), any(Timestamp.class), Mockito.eq(null));
    }

    @Test
    void testPcqWithoutCaseReturnNullList() throws InvalidRequestException {
        when(environment.getProperty(NUMBER_OF_DAYS_PROPERTY)).thenReturn("90");
        when(environment.getProperty(NUMBER_OF_DAYS_PROPERTY_LESS_THAN)).thenReturn("0");
        when(protectedCharacteristicsRepository.findByCaseIdIsNullAndCompletedDateGreaterThanAndLessThan(any(
            Timestamp.class),any(Timestamp.class), Mockito.eq(null))).thenReturn(null);

        List<ProtectedCharacteristics> protectedCharacteristicsList = consolidationService.getPcqsWithoutCase(
            getTestHeader());

        assertNotNull(protectedCharacteristicsList, ERROR_MSG_2);
        assertEquals(0, protectedCharacteristicsList.size(), ERROR_MSG_1);
        List<ProtectedCharacteristics> targetList = generateTargetList(0);
        assertListContents(targetList, protectedCharacteristicsList);

        verify(environment, times(1)).getProperty(NUMBER_OF_DAYS_PROPERTY);
        verify(protectedCharacteristicsRepository, times(1))
            .findByCaseIdIsNullAndCompletedDateGreaterThanAndLessThan(
            any(Timestamp.class),any(Timestamp.class), Mockito.eq(null));
    }

    @Test
    void testNoHeadersUpdateCase() {
        when(environment.getProperty(INVALID_ERROR_PROPERTY)).thenReturn(INVALID_ERROR);

        ResponseEntity<SubmitResponse> responseEntity = consolidationService.updateCaseId(null, TEST_PCQ_ID,
                                                                                          TEST_CASE_ID);
        assertNotNull(responseEntity, RESPONSE_NULL_MSG);

        SubmitResponse submitResponse = responseEntity.getBody();
        assertNotNull(submitResponse, RESPONSE_BODY_NULL_MSG);
        assertEquals(400, responseEntity.getStatusCode().value(), "Expected 400 status code");
        assertEquals(TEST_PCQ_ID, submitResponse.getPcqId(), "PCQ Ids not matching");
        assertEquals("400", submitResponse.getResponseStatusCode(), "Status code not matching");
        assertEquals(INVALID_ERROR, submitResponse.getResponseStatus(), "Unexpected error message");

        verify(environment, times(1)).getProperty(INVALID_ERROR_PROPERTY);

    }

    @Test
    void testUpdateCaseSuccess() {
        when(environment.getProperty(UPDATE_MSG_PROPERTY)).thenReturn(UPDATED_MSG);
        when(protectedCharacteristicsRepository.updateCase(TEST_CASE_ID, TEST_PCQ_ID)).thenReturn(1);

        ResponseEntity<SubmitResponse> responseEntity = consolidationService.updateCaseId(
            getTestHeader(), TEST_PCQ_ID, TEST_CASE_ID);
        assertNotNull(responseEntity, RESPONSE_NULL_MSG);

        SubmitResponse submitResponse = responseEntity.getBody();
        assertNotNull(submitResponse, RESPONSE_BODY_NULL_MSG);
        assertEquals(200, responseEntity.getStatusCode().value(), "Expected 200 status code");
        assertEquals(TEST_PCQ_ID, submitResponse.getPcqId(), "PCQ Ids not matching");
        assertEquals("200", submitResponse.getResponseStatusCode(),"Status code not matching");
        assertEquals(UPDATED_MSG, submitResponse.getResponseStatus(), "Unexpected success message");

        verify(environment, times(1)).getProperty(UPDATE_MSG_PROPERTY);
        verify(protectedCharacteristicsRepository, times(1)).updateCase(TEST_CASE_ID, TEST_PCQ_ID);
    }

    @Test
    void testUpdateCaseNoRecordsFound() {
        when(environment.getProperty(INVALID_ERROR_PROPERTY)).thenReturn(INVALID_ERROR);
        when(protectedCharacteristicsRepository.updateCase(TEST_CASE_ID, TEST_PCQ_ID)).thenReturn(0);

        ResponseEntity<SubmitResponse> responseEntity = consolidationService.updateCaseId(
            getTestHeader(), TEST_PCQ_ID, TEST_CASE_ID);
        assertNotNull(responseEntity, RESPONSE_NULL_MSG);

        SubmitResponse submitResponse = responseEntity.getBody();
        assertNotNull(submitResponse, RESPONSE_BODY_NULL_MSG);
        assertEquals(400, responseEntity.getStatusCode().value(), "Expected 400 status code");
        assertEquals(TEST_PCQ_ID, submitResponse.getPcqId(), "PCQ Ids not matching");
        assertEquals("400", submitResponse.getResponseStatusCode(), "Status code not matching");
        assertEquals(INVALID_ERROR, submitResponse.getResponseStatus(), "Unexpected success message");

        verify(environment, times(1)).getProperty(INVALID_ERROR_PROPERTY);
        verify(protectedCharacteristicsRepository, times(1)).updateCase(TEST_CASE_ID, TEST_PCQ_ID);
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
            targetObj.setDcnNumber("DCN - " + i);
            targetList.add(targetObj);
        }

        return targetList;
    }

    public static void assertListContents(List<ProtectedCharacteristics> targetList,
                                          List<ProtectedCharacteristics> actualList) {
        for (int i = 0; i < targetList.size(); i++) {
            assertEquals(targetList.get(i).getPcqId(), actualList.get(i).getPcqId(), "Pcq Id is not matching");
            assertEquals(targetList.get(i).getDcnNumber(), actualList.get(i).getDcnNumber(),
                         "DCN Number not matching");
        }
    }

    @Test
    void testGreaterThanNotBeforeLessThanDate() {
        when(environment.getProperty(NUMBER_OF_DAYS_PROPERTY)).thenReturn("0");
        when(environment.getProperty(NUMBER_OF_DAYS_PROPERTY_LESS_THAN)).thenReturn("90");

        try {
            consolidationService.getPcqsWithoutCase(getTestHeader());
            fail("The method should have thrown InvalidRequestException");
        } catch (InvalidRequestException ive) {
            assertEquals("The 'greaterThan' date must be before the 'lessThanDate'.",
                         ive.getMessage(), "Exception message not matching");
            assertEquals(HttpStatus.BAD_REQUEST, ive.getErrorCode(), "Http Status Code not matching");
        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage(), e);
        }

        verify(environment, times(1)).getProperty(NUMBER_OF_DAYS_PROPERTY);
        verify(environment, times(1)).getProperty(NUMBER_OF_DAYS_PROPERTY_LESS_THAN);
    }


}

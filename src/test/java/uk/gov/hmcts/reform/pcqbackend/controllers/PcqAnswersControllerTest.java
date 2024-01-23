package uk.gov.hmcts.reform.pcqbackend.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.reform.pcq.commons.utils.PcqUtils;
import uk.gov.hmcts.reform.pcqbackend.domain.ProtectedCharacteristics;
import uk.gov.hmcts.reform.pcqbackend.exceptions.DataNotFoundException;
import uk.gov.hmcts.reform.pcq.commons.model.PcqAnswerRequest;
import uk.gov.hmcts.reform.pcq.commons.model.PcqAnswerResponse;
import uk.gov.hmcts.reform.pcqbackend.repository.ProtectedCharacteristicsRepository;
import uk.gov.hmcts.reform.pcqbackend.service.DeleteService;
import uk.gov.hmcts.reform.pcqbackend.service.SubmitAnswersService;

import java.security.Security;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.Month;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.pcq.commons.tests.utils.TestUtils.getTestHeader;
import static uk.gov.hmcts.reform.pcq.commons.tests.utils.TestUtils.jsonObjectFromString;
import static uk.gov.hmcts.reform.pcq.commons.tests.utils.TestUtils.jsonStringFromFile;

@Slf4j
@SuppressWarnings({"PMD.ExcessiveImports", "PMD.TooManyMethods", "PMD.AvoidDuplicateLiterals",
    "PMD.JUnitTestsShouldIncludeAssert"})
class PcqAnswersControllerTest {

    private PcqAnswersController pcqAnswersController;

    private Environment environment;

    private ProtectedCharacteristicsRepository protectedCharacteristicsRepository;

    private static final String HEADER_KEY = "X-Correlation-Id";
    private static final String API_ERROR_MESSAGE_ACCEPTED = "Success";
    private static final String API_ERROR_MESSAGE_BAD_REQUEST = "Invalid Request";
    private static final String ERROR_MSG_PREFIX = "Test failed because of exception during execution. Message is ";
    private static final String CO_RELATION_ID_FOR_TEST = "Test-Id";
    private static final String INVALID_ERROR = "Invalid Request";
    private static final String INVALID_ERROR_PROPERTY = "api-error-messages.bad_request";
    private static final String RESPONSE_NULL_MSG = "Response is null";
    private static final String SCHEMA_FILE_PROPERTY = "api-schema-file.submitanswer-schema";
    private static final String SCHEMA_FILE = "JsonSchema/submitAnswersSchema.json";
    private static final String HEADER_API_PROPERTY = "api-required-header-keys.co-relationid";
    private static final String ALLOW_DELETE_PROPERTY = "security.db.allow_delete_record";

    private static final String FIRST_SUBMIT_ANSWER_FILENAME = "JsonTestFiles/FirstSubmitAnswer.json";
    private static final String FIRST_SUBMIT_ANSWER_OPT_OUT_NULL_FILENAME
        = "JsonTestFiles/FirstSubmitAnswerOptOutNull.json";
    private static final String FIRST_SUBMIT_ANSWER_OPT_OUT_FILENAME
        =  "JsonTestFiles/FirstSubmitAnswerOptOut.json";
    private static final String FIRST_SUBMIT_ANSWER_PAPER_CHANNEL_FILENAME
        = "JsonTestFiles/FirstSubmitAnswerPaperChannel.json";
    private static final String FIRST_SUBMIT_ANSWER_DCN_MISSING_FILENAME
        = "JsonTestFiles/FirstSubmitAnswerDcnMissing.json";
    private static final String FIRST_SUBMIT_ANSWER_DCN_EMPTY_FILENAME
        = "JsonTestFiles/FirstSubmitAnswerDcnEmpty.json";

    private static final String TEST_PCQ_ID = "T1234";
    private static final String TEST_TIME_STAMP = "2020-03-05T09:13:45.000Z";

    @BeforeEach
    void setUp() {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        this.environment = mock(Environment.class);
        this.protectedCharacteristicsRepository = mock(ProtectedCharacteristicsRepository.class);
        SubmitAnswersService submitAnswersService = new SubmitAnswersService(
            protectedCharacteristicsRepository,
            environment
        );
        DeleteService deleteService = new DeleteService(
            protectedCharacteristicsRepository,
            environment
        );

        this.pcqAnswersController = new PcqAnswersController(submitAnswersService, deleteService,environment);
        MockitoAnnotations.initMocks(this);

        when(environment.getProperty(INVALID_ERROR_PROPERTY)).thenReturn(INVALID_ERROR);
        when(environment.getProperty(SCHEMA_FILE_PROPERTY)).thenReturn(SCHEMA_FILE);
        when(environment.getProperty("api-version-number")).thenReturn("1");
        when(environment.getProperty("api-error-messages.created")).thenReturn("Successfully created");
        when(environment.getProperty("api-error-messages.internal_error")).thenReturn("Unknown error occurred");
        when(environment.getProperty("api-error-messages.accepted")).thenReturn("Success");
        when(environment.getProperty("api-error-messages.deleted")).thenReturn("Successfully deleted");
        when(environment.getProperty("api-error-messages.not_found")).thenReturn("Not Found");
        when(environment.getProperty("api-error-messages.bad_request")).thenReturn("Bad Request");
    }

    /**
     * This method tests the submitAnswers API when it is called with all valid parameters and that the answers record
     * is successfully added to the database. The response status code will be 201.
     */
    @DisplayName("Should submit the answers successfully and return with 201 response code")
    @Test
    void testSubmitAnswersFirstTime()  {

        String pcqId = TEST_PCQ_ID;
        try {
            String jsonStringRequest = jsonStringFromFile(FIRST_SUBMIT_ANSWER_FILENAME);
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);
            //logger.info("testSubmitAnswersFirstTime - Generated Json String is " + jsonStringRequest);

            Optional<ProtectedCharacteristics> protectedCharacteristicsOptional = Optional.empty();
            ProtectedCharacteristics targetObject = new ProtectedCharacteristics();

            when(protectedCharacteristicsRepository.findByPcqId(pcqId,null))
                .thenReturn(protectedCharacteristicsOptional);
            doNothing().when(protectedCharacteristicsRepository).saveProtectedCharacteristicsWithEncryption(
                targetObject,null);

            when(environment.getProperty(HEADER_API_PROPERTY)).thenReturn(HEADER_KEY);
            HttpHeaders mockHeaders = getMockHeader();
            when(mockHeaders.get(HEADER_KEY)).thenReturn(getTestHeader());

            ResponseEntity<Object> actual = pcqAnswersController.submitAnswers(mockHeaders, answerRequest);

            assertNotNull(actual, RESPONSE_NULL_MSG);
            assertEquals(HttpStatus.CREATED, actual.getStatusCode(),
                         "Expected 201 status code");


            verify(environment, times(1)).getProperty(HEADER_API_PROPERTY);
            verify(protectedCharacteristicsRepository, times(1))
                .findByPcqId(pcqId,null);
            verify(protectedCharacteristicsRepository, times(1))
                .saveProtectedCharacteristicsWithEncryption(any(
                ProtectedCharacteristics.class), Mockito.eq(null));
            verify(mockHeaders, times(1)).get(HEADER_KEY);
        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage(), e);
        }

    }

    /**
     * This method tests the submitAnswers API when it is called with NULL in OptOut and that the answers record
     * is successfully added to the database. The response status code will be 201.
     */
    @DisplayName("Should submit the answers when OptOut is NULL successfully and return with 201 response code")
    @Test
    void testSubmitAnswersOptOutNull()  {

        String pcqId = TEST_PCQ_ID;
        try {
            String jsonStringRequest = jsonStringFromFile(FIRST_SUBMIT_ANSWER_OPT_OUT_NULL_FILENAME);
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);
            //logger.info("testSubmitAnswersFirstTime - Generated Json String is " + jsonStringRequest);

            Optional<ProtectedCharacteristics> protectedCharacteristicsOptional = Optional.empty();
            ProtectedCharacteristics targetObject = new ProtectedCharacteristics();

            when(protectedCharacteristicsRepository.findByPcqId(pcqId,null))
                .thenReturn(protectedCharacteristicsOptional);
            doNothing().when(protectedCharacteristicsRepository).saveProtectedCharacteristicsWithEncryption(
                targetObject,null);
            when(environment.getProperty(HEADER_API_PROPERTY)).thenReturn(HEADER_KEY);
            HttpHeaders mockHeaders = getMockHeader();
            when(mockHeaders.get(HEADER_KEY)).thenReturn(getTestHeader());

            ResponseEntity<Object> actual = pcqAnswersController.submitAnswers(mockHeaders, answerRequest);

            assertNotNull(actual, RESPONSE_NULL_MSG);
            assertEquals(HttpStatus.CREATED, actual.getStatusCode(), "Expected 201 status code");


            verify(environment, times(1)).getProperty(HEADER_API_PROPERTY);
            verify(protectedCharacteristicsRepository, times(1))
                .findByPcqId(pcqId,null);
            verify(protectedCharacteristicsRepository, times(1))
                .saveProtectedCharacteristicsWithEncryption(any(
                ProtectedCharacteristics.class),Mockito.eq(null));
            verify(mockHeaders, times(1)).get(HEADER_KEY);
        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage(), e);
        }

    }

    /**
     * This method tests the submitAnswers API when it is called for paper channel and that the answers record
     * is successfully added to the database. The response status code will be 201.
     */
    @DisplayName("Should submit the answers for the paper channel successfully and return with 201 response code")
    @Test
    void testSubmitAnswersPaperChannel()  {

        String pcqId = TEST_PCQ_ID;
        try {
            String jsonStringRequest = jsonStringFromFile(FIRST_SUBMIT_ANSWER_PAPER_CHANNEL_FILENAME);
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

            Optional<ProtectedCharacteristics> protectedCharacteristicsOptional = Optional.empty();
            ProtectedCharacteristics targetObject = new ProtectedCharacteristics();

            when(protectedCharacteristicsRepository.findByPcqId(pcqId,null))
                .thenReturn(protectedCharacteristicsOptional);
            doNothing().when(protectedCharacteristicsRepository).saveProtectedCharacteristicsWithEncryption(
                targetObject,null);
            when(environment.getProperty(HEADER_API_PROPERTY)).thenReturn(HEADER_KEY);
            HttpHeaders mockHeaders = getMockHeader();
            when(mockHeaders.get(HEADER_KEY)).thenReturn(getTestHeader());

            ResponseEntity<Object> actual = pcqAnswersController.submitAnswers(mockHeaders, answerRequest);

            assertNotNull(actual, RESPONSE_NULL_MSG);
            assertEquals(HttpStatus.CREATED, actual.getStatusCode(), "Expected 201 status code");


            verify(environment, times(1)).getProperty(HEADER_API_PROPERTY);
            verify(protectedCharacteristicsRepository, times(1))
                .findByPcqId(pcqId,null);
            verify(protectedCharacteristicsRepository, times(1))
                .saveProtectedCharacteristicsWithEncryption(any(
                    ProtectedCharacteristics.class),Mockito.eq(null));
            verify(mockHeaders, times(1)).get(HEADER_KEY);
        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage(), e);
        }

    }

    /**
     * This method tests the submitAnswers API when it is called with Y in OptOut and that the answers record
     * is successfully set to null and optout as true in the database. The response status code will be 201.
     */
    @DisplayName("Should null the answers and set optOut as true when OptOut is Y "
        + "successfully and return with 201 response code")
    @Test
    void testSubmitAnswersOptOutSuccess()  {

        //String pcqId = TEST_PCQ_ID;
        try {
            String jsonStringRequest = jsonStringFromFile(FIRST_SUBMIT_ANSWER_OPT_OUT_FILENAME);
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);
            //logger.info("testSubmitAnswersFirstTime - Generated Json String is " + jsonStringRequest);

            /*int resultCount = 1;

            when(protectedCharacteristicsRepository.updateCharacteristics(null,null,
                                                                          null,null,
                                                                          null,null,
                                                                          null,null,
                                                                          null,null,
                                                                          null,null,
                                                                          null,null,
                                                                          null,null,
                                                                          null,null,
                                                                          null,null,
                                                                          null,null,
                                                                          null,null,
                                                                          null,null,
                                                                          null,null,
                                                                          null,null,
                                                                          null,true,
                                                                          pcqId,null)).thenReturn(resultCount);*/
            when(environment.getProperty(HEADER_API_PROPERTY)).thenReturn(HEADER_KEY);
            HttpHeaders mockHeaders = getMockHeader();
            when(mockHeaders.get(HEADER_KEY)).thenReturn(getTestHeader());

            ResponseEntity<Object> actual = pcqAnswersController.submitAnswers(mockHeaders, answerRequest);

            assertNotNull(actual, RESPONSE_NULL_MSG);
            assertEquals(HttpStatus.CREATED, actual.getStatusCode(), "Expected 201 status code");


            verify(environment, times(1)).getProperty(HEADER_API_PROPERTY);
            /*verify(protectedCharacteristicsRepository, times(1))
                .updateCharacteristics(null,null,
                                       null,null,
                                       null,null,
                                       null,null,
                                       null,null,
                                       null,null,
                                       null,null,
                                       null,null,
                                       null,null,
                                       null,null,
                                       null,null,
                                       null,null,
                                       null,null,
                                       null,null,
                                       null,null,
                                       null,true,
                                       pcqId,null);*/
            verify(mockHeaders, times(1)).get(HEADER_KEY);
        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage(), e);
        }

    }

    /**
     * This method tests the submitAnswers API when it is called with Y in OptOut and that the answers record
     * is set to null in database and optOut is true.
     */
    /*@DisplayName("Should set the answers as null and optOut as true when OptOut")
    @Test
    void testSubmitAnswersOptOutRecordAsTrue()  {

        String pcqId = TEST_PCQ_ID;
        try {
            ProtectedCharacteristics targetObject = new ProtectedCharacteristics();
            targetObject.setPcqId(pcqId);
            Optional<ProtectedCharacteristics> protectedCharacteristicsOptional = Optional.of(targetObject);
            int resultCount = 1;
            when(protectedCharacteristicsRepository.findById(pcqId)).thenReturn(protectedCharacteristicsOptional);
            when(protectedCharacteristicsRepository.updateCharacteristics(null,null,
                                                                          null,null,
                                                                          null,null,
                                                                          null,null,
                                                                          null,null,
                                                                          null,null,
                                                                          null,null,
                                                                          null,null,
                                                                          null,null,
                                                                          null,null,
                                                                          null,null,
                                                                          null,null,
                                                                          null,null,
                                                                          null,null,
                                                                          null,null,
                                                                          null,true,
                                                                          pcqId,null)).thenReturn(resultCount);
            when(environment.getProperty(HEADER_API_PROPERTY)).thenReturn(HEADER_KEY);
            HttpHeaders mockHeaders = getMockHeader();
            when(mockHeaders.get(HEADER_KEY)).thenReturn(getTestHeader());

            String jsonStringRequest = jsonStringFromFile(FIRST_SUBMIT_ANSWER_OPT_OUT_FILENAME);
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);
            ResponseEntity<Object> actual = pcqAnswersController.submitAnswers(mockHeaders, answerRequest);

            assertNotNull(actual, RESPONSE_NULL_MSG);
            assertEquals(HttpStatus.CREATED, actual.getStatusCode(), "Expected 201 status code");

            verify(environment, times(1)).getProperty(HEADER_API_PROPERTY);
            verify(protectedCharacteristicsRepository, times(1))
                .updateCharacteristics(null,null,
                                      null,null,
                                      null,null,
                                      null,null,
                                      null,null,
                                      null,null,
                                      null,null,
                                      null,null,
                                      null,null,
                                      null,null,
                                      null,null,
                                      null,null,
                                      null,null,
                                      null,null,
                                      null,null,
                                      null,true,
                                      pcqId,null);
            verify(mockHeaders, times(1)).get(HEADER_KEY);
        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage(), e);
        }

    }*/

    /**
     * This method tests the submitAnswers API when it is called for paper channel and that the answers record
     * is not created in the database. The response status code will be 400.
     */
    @DisplayName("Should NOT create the answers for missing DCN Number in paper channel and return with "
        + "400 response code")
    @Test
    void testSubmitAnswersPaperChannelMissingDcnNumber()  {

        try {
            String jsonStringRequest = jsonStringFromFile(FIRST_SUBMIT_ANSWER_DCN_MISSING_FILENAME);
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

            when(environment.getProperty(HEADER_API_PROPERTY)).thenReturn(HEADER_KEY);
            when(environment.getProperty(INVALID_ERROR_PROPERTY)).thenReturn(API_ERROR_MESSAGE_BAD_REQUEST);
            HttpHeaders mockHeaders = getMockHeader();
            when(mockHeaders.get(HEADER_KEY)).thenReturn(getTestHeader());

            ResponseEntity<Object> actual = pcqAnswersController.submitAnswers(mockHeaders, answerRequest);

            assertNotNull(actual, RESPONSE_NULL_MSG);
            assertEquals(HttpStatus.BAD_REQUEST, actual.getStatusCode(), "Expected 400 status code");


            verify(environment, times(1)).getProperty(HEADER_API_PROPERTY);
            verify(environment, times(1)).getProperty(INVALID_ERROR_PROPERTY);
            verify(mockHeaders, times(1)).get(HEADER_KEY);
        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage(), e);
        }

    }

    /**
     * This method tests the submitAnswers API when it is called for paper channel and that the answers record
     * is not created in the database. The response status code will be 400.
     */
    @DisplayName("Should NOT create the answers for empty DCN Number in paper channel and return with "
        + "400 response code")
    @Test
    void testSubmitAnswersPaperChannelEmptyDcnNumber()  {

        try {
            String jsonStringRequest = jsonStringFromFile(FIRST_SUBMIT_ANSWER_DCN_EMPTY_FILENAME);
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);


            when(environment.getProperty(HEADER_API_PROPERTY)).thenReturn(HEADER_KEY);
            when(environment.getProperty(INVALID_ERROR_PROPERTY)).thenReturn(API_ERROR_MESSAGE_BAD_REQUEST);
            HttpHeaders mockHeaders = getMockHeader();
            when(mockHeaders.get(HEADER_KEY)).thenReturn(getTestHeader());

            ResponseEntity<Object> actual = pcqAnswersController.submitAnswers(mockHeaders, answerRequest);

            assertNotNull(actual, RESPONSE_NULL_MSG);
            assertEquals(HttpStatus.BAD_REQUEST, actual.getStatusCode(), "Expected 400 status code");


            verify(environment, times(1)).getProperty(HEADER_API_PROPERTY);
            verify(environment, times(1)).getProperty(INVALID_ERROR_PROPERTY);
            verify(mockHeaders, times(1)).get(HEADER_KEY);
        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage(), e);
        }

    }

    /**
     * This method tests the submitAnswers API when it is called with all valid parameters and that the answers record
     * is successfully updated in the database. The response status code will be 201.
     */
    @DisplayName("Should update the answers successfully and return with 201 response code")
    @Test
    void testSubmitAnswersSecondTime()  {

        String pcqId = TEST_PCQ_ID;
        try {
            //logger.info("testSubmitAnswersFirstTime - Generated Json String is " + jsonStringRequest);

            ProtectedCharacteristics targetObject = new ProtectedCharacteristics();
            targetObject.setPcqId(pcqId);
            Optional<ProtectedCharacteristics> protectedCharacteristicsOptional = Optional.of(targetObject);
            int resultCount = 1;
            int dobProvided = 1;
            Date testDob = Date.valueOf(LocalDate.of(1970, Month.JANUARY, 1));
            Timestamp testTimeStamp = PcqUtils.getTimeFromString(TEST_TIME_STAMP);

            when(protectedCharacteristicsRepository.findByPcqId(pcqId,null))
                .thenReturn(protectedCharacteristicsOptional);
            when(protectedCharacteristicsRepository.updateCharacteristics(dobProvided, testDob, null,
                                                                          null, null,
                                                                          null, null,
                                                                          null, null,
                                                                          null, null,
                                                                          null, null,
                                                                          null, null,
                                                                          null, null,
                                                                          null, null,
                                                                          null, null,
                                                                          null, null,
                                                                          null, null,
                                                                          null, null,
                                                                          null, null,
                                                                          null,
                                                                          testTimeStamp,false, pcqId, testTimeStamp)
            ).thenReturn(resultCount);
            when(environment.getProperty(HEADER_API_PROPERTY)).thenReturn(HEADER_KEY);
            HttpHeaders mockHeaders = getMockHeader();
            when(mockHeaders.get(HEADER_KEY)).thenReturn(getTestHeader());

            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/DobSubmitAnswer.json");
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);
            ResponseEntity<Object> actual = pcqAnswersController.submitAnswers(mockHeaders, answerRequest);

            assertNotNull(actual, RESPONSE_NULL_MSG);
            assertEquals(HttpStatus.CREATED, actual.getStatusCode(), "Expected 201 status code");


            verify(environment, times(1)).getProperty(HEADER_API_PROPERTY);
            verify(protectedCharacteristicsRepository, times(1)).findByPcqId(pcqId,null);
            verify(protectedCharacteristicsRepository, times(1)).updateCharacteristics(
                dobProvided, testDob, null,
                null, null,
                null, null,
                null, null,
                null, null,
                null, null,
                null, null,
                null, null,
                null, null,
                null, null,
                null, null,
                null, null,
                null, null,
                null, null,
                null, testTimeStamp,false, pcqId, testTimeStamp);
            verify(mockHeaders, times(1)).get(HEADER_KEY);
        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage(), e);
        }

    }

    /**
     * This method tests the submitAnswers API when it is called with all valid parameters and that the answers record
     * is NOT successfully updated in the database because completed date is in the past.
     * The response status code will be 202.
     */
    @DisplayName("Should not update the database and return with 202 response code")
    @Test
    void testCompletedDateStale()  {

        String pcqId = TEST_PCQ_ID;
        try {
            //logger.info("testSubmitAnswersFirstTime - Generated Json String is " + jsonStringRequest);

            ProtectedCharacteristics targetObject = new ProtectedCharacteristics();
            targetObject.setPcqId(pcqId);
            Optional<ProtectedCharacteristics> protectedCharacteristicsOptional = Optional.of(targetObject);
            int resultCount = 0;
            int dobProvided = 1;
            Date testDob = Date.valueOf(LocalDate.of(1970, Month.JANUARY, 1));
            Timestamp testTimeStamp = PcqUtils.getTimeFromString(TEST_TIME_STAMP);

            when(protectedCharacteristicsRepository.findByPcqId(pcqId,null))
                .thenReturn(protectedCharacteristicsOptional);
            when(protectedCharacteristicsRepository.updateCharacteristics(dobProvided, testDob, null,
                                                                          null, null,
                                                                          null, null,
                                                                          null, null,
                                                                          null, null,
                                                                          null, null,
                                                                          null, null,
                                                                          null, null,
                                                                          null, null,
                                                                          null, null,
                                                                          null, null,
                                                                          null, null,
                                                                          null, null,
                                                                          null, null,
                                                                          null,
                                                                          testTimeStamp,false, pcqId, testTimeStamp)
            ).thenReturn(resultCount);
            when(environment.getProperty(HEADER_API_PROPERTY)).thenReturn(HEADER_KEY);
            when(environment.getProperty("api-error-messages.accepted")).thenReturn(API_ERROR_MESSAGE_ACCEPTED);
            HttpHeaders mockHeaders = getMockHeader();
            when(mockHeaders.get(HEADER_KEY)).thenReturn(getTestHeader());

            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/DobSubmitAnswer.json");
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);
            ResponseEntity<Object> actual = pcqAnswersController.submitAnswers(mockHeaders, answerRequest);

            assertNotNull(actual, RESPONSE_NULL_MSG);
            assertEquals(HttpStatus.ACCEPTED, actual.getStatusCode(), "Expected 202 status code");


            verify(environment, times(1)).getProperty(HEADER_API_PROPERTY);
            verify(environment, times(1)).getProperty("api-error-messages.accepted");
            verify(protectedCharacteristicsRepository, times(1)).findByPcqId(pcqId,null);
            verify(protectedCharacteristicsRepository, times(1)).updateCharacteristics(
                dobProvided, testDob, null,
                null, null,
                null, null,
                null, null,
                null, null,
                null, null,
                null, null,
                null, null,
                null, null,
                null, null,
                null, null,
                null, null,
                null, null,
                null, null,
                null, testTimeStamp,false, pcqId, testTimeStamp);
            verify(mockHeaders, times(1)).get(HEADER_KEY);
        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage(), e);
        }

    }

    /**
     * This method tests the submitAnswers API when it is called with incorrect version number.
     * The response status code will be 403.
     */
    @DisplayName("Should validate the version number and return with 403 response code")
    @Test
    void testInvalidVersionNumber()  {

        try {
            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/InvalidVersion.json");
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);
            //logger.info("testSubmitAnswersFirstTime - Generated Json String is " + jsonStringRequest);

            when(environment.getProperty(HEADER_API_PROPERTY)).thenReturn(HEADER_KEY);
            when(environment.getProperty(INVALID_ERROR_PROPERTY)).thenReturn(API_ERROR_MESSAGE_BAD_REQUEST);
            HttpHeaders mockHeaders = getMockHeader();
            when(mockHeaders.get(HEADER_KEY)).thenReturn(getTestHeader());

            ResponseEntity<Object> actual = pcqAnswersController.submitAnswers(mockHeaders, answerRequest);

            assertNotNull(actual, RESPONSE_NULL_MSG);
            assertEquals(HttpStatus.FORBIDDEN, actual.getStatusCode(), "Expected 403 status code");


            verify(environment, times(1)).getProperty(HEADER_API_PROPERTY);
            verify(environment, times(1)).getProperty(INVALID_ERROR_PROPERTY);
            verify(mockHeaders, times(1)).get(HEADER_KEY);
        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage(), e);
        }

    }

    /**
     * This method tests the submitAnswers API OptOut operation when it is called with incorrect version number.
     * The response status code will be 403.
     */
    @DisplayName("Should validate the version number for OptOut operation and return with 403 response code")
    @Test
    void testInvalidVersionNumberForOptOut()  {

        try {
            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/InvalidVersionOptOut.json");
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);
            //logger.info("testSubmitAnswersFirstTime - Generated Json String is " + jsonStringRequest);

            when(environment.getProperty(HEADER_API_PROPERTY)).thenReturn(HEADER_KEY);
            when(environment.getProperty(INVALID_ERROR_PROPERTY)).thenReturn(API_ERROR_MESSAGE_BAD_REQUEST);
            HttpHeaders mockHeaders = getMockHeader();
            when(mockHeaders.get(HEADER_KEY)).thenReturn(getTestHeader());

            ResponseEntity<Object> actual = pcqAnswersController.submitAnswers(mockHeaders, answerRequest);

            assertNotNull(actual, RESPONSE_NULL_MSG);
            assertEquals(HttpStatus.FORBIDDEN, actual.getStatusCode(), "Expected 403 status code");


            verify(environment, times(1)).getProperty(HEADER_API_PROPERTY);
            verify(environment, times(1)).getProperty(INVALID_ERROR_PROPERTY);
            verify(mockHeaders, times(1)).get(HEADER_KEY);
        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage(), e);
        }

    }

    /**
     * This method tests the submitAnswers API when it is called with all valid parameters but the
     * header does not contain the required attribute. The response status code will be 400.
     */
    @DisplayName("Should return with an Invalid Request error code 400")
    @Test
    void testInvalidRequestForMissingHeader()  {

        try {
            String jsonStringRequest = asJsonString(new PcqAnswerRequest(TEST_PCQ_ID));
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

            when(environment.getProperty(HEADER_API_PROPERTY)).thenReturn(HEADER_KEY);
            HttpHeaders mockHeaders = mock(HttpHeaders.class);
            when(mockHeaders.get(HEADER_KEY)).thenReturn(null);

            ResponseEntity<Object> actual = pcqAnswersController.submitAnswers(mockHeaders, answerRequest);

            assertNotNull(actual, RESPONSE_NULL_MSG);
            assertEquals(HttpStatus.BAD_REQUEST, actual.getStatusCode(), "Expected 400 status code");
            verify(mockHeaders, times(1)).get(HEADER_KEY);
            verify(environment, times(1)).getProperty(HEADER_API_PROPERTY);

        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage());
        }

    }

    /**
     * This method tests the submitAnswers API when it is called with all OptOut and valid parameters but the
     * header does not contain the required attribute. The response status code will be 400.
     */
    @DisplayName("Should return with an Invalid Request error code 400 for OptOut")
    @Test
    void testInvalidOptOutRequestForMissingHeader()  {

        try {
            String jsonStringRequest = asJsonString(new PcqAnswerRequest(TEST_PCQ_ID));
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);
            answerRequest.setOptOut("Y");

            when(environment.getProperty(HEADER_API_PROPERTY)).thenReturn(HEADER_KEY);
            HttpHeaders mockHeaders = mock(HttpHeaders.class);
            when(mockHeaders.get(HEADER_KEY)).thenReturn(null);

            ResponseEntity<Object> actual = pcqAnswersController.submitAnswers(mockHeaders, answerRequest);

            assertNotNull(actual, RESPONSE_NULL_MSG);
            assertEquals(HttpStatus.BAD_REQUEST, actual.getStatusCode(), "Expected 400 status code");
            verify(mockHeaders, times(1)).get(HEADER_KEY);
            verify(environment, times(1)).getProperty(HEADER_API_PROPERTY);

        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage());
        }

    }

    /**
     * This method tests the submitAnswers API when it is called with invalid Json.
     * The response status code will be 400.
     */
    @DisplayName("Should return with an Invalid Request error code 400")
    @Test
    void testRequestForInvalidJson()  {

        try {
            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/InvalidJson1.json");

            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);
            //logger.info("testSubmitAnswersFirstTime - Generated Json String is " + jsonStringRequest);

            when(environment.getProperty(HEADER_API_PROPERTY)).thenReturn(HEADER_KEY);
            when(environment.getProperty(INVALID_ERROR_PROPERTY)).thenReturn(API_ERROR_MESSAGE_BAD_REQUEST);
            HttpHeaders mockHeaders = getMockHeader();
            when(mockHeaders.get(HEADER_KEY)).thenReturn(getTestHeader());

            ResponseEntity<Object> actual = pcqAnswersController.submitAnswers(mockHeaders, answerRequest);

            assertNotNull(actual, RESPONSE_NULL_MSG);
            assertEquals(HttpStatus.BAD_REQUEST, actual.getStatusCode(), "Expected 400 status code");
            verify(mockHeaders, times(1)).get(HEADER_KEY);
            verify(environment, times(1)).getProperty(HEADER_API_PROPERTY);


        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage(), e);
        }

    }

    /**
     * This method tests the submitAnswers API OptOut request when it is called with invalid Json.
     * The response status code will be 400.
     */
    @DisplayName("Should return with an Invalid Request error code 400 for OptOut request")
    @Test
    void testOptOutRequestForInvalidJson()  {

        try {
            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/InvalidOptOutJson1.json");

            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);
            //logger.info("testSubmitAnswersFirstTime - Generated Json String is " + jsonStringRequest);

            when(environment.getProperty(HEADER_API_PROPERTY)).thenReturn(HEADER_KEY);
            when(environment.getProperty(INVALID_ERROR_PROPERTY)).thenReturn(API_ERROR_MESSAGE_BAD_REQUEST);
            HttpHeaders mockHeaders = getMockHeader();
            when(mockHeaders.get(HEADER_KEY)).thenReturn(getTestHeader());

            ResponseEntity<Object> actual = pcqAnswersController.submitAnswers(mockHeaders, answerRequest);

            assertNotNull(actual, RESPONSE_NULL_MSG);
            assertEquals(HttpStatus.BAD_REQUEST, actual.getStatusCode(), "Expected 400 status code");
            verify(mockHeaders, times(1)).get(HEADER_KEY);
            verify(environment, times(1)).getProperty(HEADER_API_PROPERTY);


        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage(), e);
        }

    }

    /**
     * This method tests the submitAnswers API when it is called with invalid OptOut value in the Json.
     * The response status code will be 400.
     */
    @DisplayName("Should return with an Invalid Request error code 400 for Invalid OptOut value")
    @Test
    void testRequestForInvalidOptOutValue()  {

        try {
            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/InvalidOptOutJson.json");

            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);
            //logger.info("testSubmitAnswersFirstTime - Generated Json String is " + jsonStringRequest);

            when(environment.getProperty(HEADER_API_PROPERTY)).thenReturn(HEADER_KEY);
            when(environment.getProperty(INVALID_ERROR_PROPERTY)).thenReturn(API_ERROR_MESSAGE_BAD_REQUEST);
            HttpHeaders mockHeaders = getMockHeader();
            when(mockHeaders.get(HEADER_KEY)).thenReturn(getTestHeader());

            ResponseEntity<Object> actual = pcqAnswersController.submitAnswers(mockHeaders, answerRequest);

            assertNotNull(actual, RESPONSE_NULL_MSG);
            assertEquals(HttpStatus.BAD_REQUEST, actual.getStatusCode(), "Expected 400 status code");
            verify(mockHeaders, times(1)).get(HEADER_KEY);
            verify(environment, times(1)).getProperty(HEADER_API_PROPERTY);


        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage(), e);
        }

    }

    /**
     * This method tests the submitAnswers API when it is called with all valid parameters but the database call
     * returns an exception. The response status code will be 500.
     */
    @DisplayName("Should return with an 500 error code")
    @Test
    void testInternalError()  {

        String pcqId = TEST_PCQ_ID;
        try {
            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/FirstSubmitAnswer.json");
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);
            //logger.info("testSubmitAnswersFirstTime - Generated Json String is " + jsonStringRequest);

            Optional<ProtectedCharacteristics> protectedCharacteristicsOptional = Optional.empty();

            when(protectedCharacteristicsRepository.findByPcqId(pcqId,null))
                .thenReturn(protectedCharacteristicsOptional);
            doThrow(new NullPointerException()).when(protectedCharacteristicsRepository)
                .saveProtectedCharacteristicsWithEncryption(any(
                ProtectedCharacteristics.class),Mockito.eq(null));
            when(environment.getProperty(HEADER_API_PROPERTY)).thenReturn(HEADER_KEY);
            HttpHeaders mockHeaders = getMockHeader();
            when(mockHeaders.get(HEADER_KEY)).thenReturn(getTestHeader());

            ResponseEntity<Object> actual = pcqAnswersController.submitAnswers(mockHeaders, answerRequest);

            assertNotNull(actual, RESPONSE_NULL_MSG);
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actual.getStatusCode(), "Expected 500 status code");


            verify(environment, times(1)).getProperty(HEADER_API_PROPERTY);
            verify(protectedCharacteristicsRepository, times(1)).findByPcqId(pcqId,null);
            verify(protectedCharacteristicsRepository, times(1))
                .saveProtectedCharacteristicsWithEncryption(any(
                ProtectedCharacteristics.class),Mockito.eq(null));
            verify(mockHeaders, times(1)).get(HEADER_KEY);
        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage(), e);
        }

    }

    /**
     * This method tests the getAnswer API when it is called with all valid parameters and the database
     * returns the answer record. The response status code will be 200.
     */
    @DisplayName("Should return with an 200 error code")
    @Test
    void testGetAnswerFound()  {

        String pcqId = TEST_PCQ_ID;
        try {

            ProtectedCharacteristics targetObject = new ProtectedCharacteristics();
            targetObject.setPcqId(pcqId);
            Optional<ProtectedCharacteristics> protectedCharacteristicsOptional = Optional.of(targetObject);

            when(protectedCharacteristicsRepository.findByPcqId(pcqId,null))
                .thenReturn(protectedCharacteristicsOptional);

            ResponseEntity<PcqAnswerResponse> actual = pcqAnswersController.getAnswersByPcqId(pcqId);

            assertNotNull(actual, RESPONSE_NULL_MSG);
            assertEquals(HttpStatus.OK, actual.getStatusCode(), "Expected 200 status code");

            PcqAnswerResponse actualBody = actual.getBody();
            assertNotNull(actualBody, RESPONSE_NULL_MSG);

            verify(protectedCharacteristicsRepository, times(1)).findByPcqId(pcqId,null);

        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage(), e);
        }

    }

    /**
     * This method tests the getAnswer API when it is called with all valid parameters and the database
     * does not return with a answer record. The response status code will be 404.
     */
    @DisplayName("Should return with an 404 error code")
    @Test
    void testGetAnswerNotFound()  {

        String pcqId = TEST_PCQ_ID;
        try {

            Optional<ProtectedCharacteristics> protectedCharacteristicsOptional = Optional.empty();

            when(protectedCharacteristicsRepository.findByPcqId(pcqId,null))
                .thenReturn(protectedCharacteristicsOptional);

            assertThrows(DataNotFoundException.class, () -> pcqAnswersController.getAnswersByPcqId(pcqId));

            verify(protectedCharacteristicsRepository, times(1)).findByPcqId(pcqId,null);

        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage(), e);
        }

    }


    /**
     * Converts an Java Object to JSon String.
     * @param obj - The Java object.
     * @return - JSON String representation of the Java object.
     */
    public static String asJsonString(final Object obj) throws JsonProcessingException {
        return  new ObjectMapper().writeValueAsString(obj);
    }


    private HttpHeaders getMockHeader() {
        HttpHeaders mockHeader = mock(HttpHeaders.class);
        mockHeader.set("HTTP_X-Correlation-Id", CO_RELATION_ID_FOR_TEST);

        return mockHeader;
    }

    /**
     * This method tests the deletePcqRecord API when it is called with correct PcqId , pcq record in database
     * should be deleted. The response status code will be 200.
     */
    @DisplayName("Should return with an 200 error code")
    @Test
    void testDeletePcqRecord()  {

        String pcqId = TEST_PCQ_ID;
        try {
            when(environment.getProperty(ALLOW_DELETE_PROPERTY)).thenReturn("true");
            when(protectedCharacteristicsRepository.deletePcqRecord(pcqId)).thenReturn(1);
            ResponseEntity<Object> actual = pcqAnswersController.deletePcqRecord(pcqId);

            assertNotNull(actual, RESPONSE_NULL_MSG);
            assertEquals(HttpStatus.OK, actual.getStatusCode(), "Expected 200 status code");

            verify(protectedCharacteristicsRepository, times(1)).deletePcqRecord(pcqId);

        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage(), e);
        }

    }

    /**
     * This method tests the deletePcqRecord API when it is called with invalid PcqId , pcq record not found in database
     * The response status code will be 404.
     */
    @DisplayName("Should return with an 404 error code")
    @Test
    void testDeletePcqRecordNotFound()  {

        String pcqId = TEST_PCQ_ID;
        try {
            when(environment.getProperty(ALLOW_DELETE_PROPERTY)).thenReturn("true");
            when(protectedCharacteristicsRepository.deletePcqRecord(pcqId)).thenReturn(0);
            ResponseEntity<Object> actual = pcqAnswersController.deletePcqRecord(pcqId);

            assertNotNull(actual, RESPONSE_NULL_MSG);
            assertEquals(HttpStatus.NOT_FOUND, actual.getStatusCode(), "Expected 404 status code");

            verify(protectedCharacteristicsRepository, times(1)).deletePcqRecord(pcqId);

        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage(), e);
        }

    }

    /**
     * This method tests the deletePcqRecord API when it is called with db_allow_delete_record as false, pcq record
     * will not be deleted . The response status code will be 401.
     */
    @DisplayName("Should return with an 401 error code")
    @Test
    void testDeletePcqRecordUnauthorised()  {

        String pcqId = TEST_PCQ_ID;
        try {
            when(environment.getProperty(ALLOW_DELETE_PROPERTY)).thenReturn("false");
            ResponseEntity<Object> actual = pcqAnswersController.deletePcqRecord(pcqId);

            assertNotNull(actual, RESPONSE_NULL_MSG);
            assertEquals(HttpStatus.UNAUTHORIZED, actual.getStatusCode(), "Expected 404 status code");

        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage(), e);
        }

    }

    @Test
    void testSubmitAnswersEnglish() {
        Integer mainLanguage = 4;
        String fileName = "JsonTestFiles/MainLanguageAnswerEnglish.json";
        updateLanguage(mainLanguage,fileName);
    }

    @Test
    void testSubmitAnswersWelsh() {
        Integer mainLanguage = 3;
        String fileName = "JsonTestFiles/MainLanguageAnswerWelsh.json";
        updateLanguage(mainLanguage,fileName);
    }

    //Test English or welsh option as well for backward compatibility
    @Test
    void testSubmitAnswersEnglishOrWelsh() {
        Integer mainLanguage = 1;
        String fileName = "JsonTestFiles/MainLanguageEnglishOrWelsh.json";
        updateLanguage(mainLanguage,fileName);
    }

    public void updateLanguage(Integer mainLanguage, String fileName) {
        String pcqId = TEST_PCQ_ID;
        try {
            ProtectedCharacteristics targetObject = new ProtectedCharacteristics();
            targetObject.setPcqId(pcqId);
            Optional<ProtectedCharacteristics> protectedCharacteristicsOptional = Optional.of(targetObject);
            int resultCount = 1;
            int dobProvided = 1;
            Date testDob = Date.valueOf(LocalDate.of(1970, Month.JANUARY, 1));
            Timestamp testTimeStamp = PcqUtils.getTimeFromString(TEST_TIME_STAMP);

            when(protectedCharacteristicsRepository.findByPcqId(pcqId,null))
                .thenReturn(protectedCharacteristicsOptional);
            when(protectedCharacteristicsRepository.updateCharacteristics(dobProvided, testDob, mainLanguage,
                                                                          null, null,
                                                                          null, null,
                                                                          null, null,
                                                                          null, null,
                                                                          null, null,
                                                                          null, null,
                                                                          null, null,
                                                                          null, null,
                                                                          null, null,
                                                                          null, null,
                                                                          null, null,
                                                                          null, null,
                                                                          null, null,
                                                                          null,
                                                                          testTimeStamp,false, pcqId, testTimeStamp)
            ).thenReturn(resultCount);
            when(environment.getProperty(HEADER_API_PROPERTY)).thenReturn(HEADER_KEY);
            HttpHeaders mockHeaders = getMockHeader();
            when(mockHeaders.get(HEADER_KEY)).thenReturn(getTestHeader());

            String jsonStringRequest = jsonStringFromFile(fileName);
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);
            ResponseEntity<Object> actual = pcqAnswersController.submitAnswers(mockHeaders, answerRequest);

            assertNotNull(actual, RESPONSE_NULL_MSG);
            assertEquals(HttpStatus.CREATED, actual.getStatusCode(), "Expected 201 status code");


            verify(environment, times(1)).getProperty(HEADER_API_PROPERTY);
            verify(protectedCharacteristicsRepository, times(1)).findByPcqId(pcqId,null);
            verify(protectedCharacteristicsRepository, times(1)).updateCharacteristics(
                dobProvided, testDob, mainLanguage,
                null, null,
                null, null,
                null, null,
                null, null,
                null, null,
                null, null,
                null, null,
                null, null,
                null, null,
                null, null,
                null, null,
                null, null,
                null, null,
                null, testTimeStamp,false, pcqId, testTimeStamp);
            verify(mockHeaders, times(1)).get(HEADER_KEY);
        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage(), e);
        }
    }

}

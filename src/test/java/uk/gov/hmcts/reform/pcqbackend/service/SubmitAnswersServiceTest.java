package uk.gov.hmcts.reform.pcqbackend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.pcq.commons.utils.PcqUtils;
import uk.gov.hmcts.reform.pcqbackend.domain.ProtectedCharacteristics;
import uk.gov.hmcts.reform.pcq.commons.model.PcqAnswerRequest;
import uk.gov.hmcts.reform.pcqbackend.repository.ProtectedCharacteristicsRepository;

import java.security.Security;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.pcq.commons.tests.utils.TestUtils.getTestHeader;
import static uk.gov.hmcts.reform.pcq.commons.tests.utils.TestUtils.jsonObjectFromString;
import static uk.gov.hmcts.reform.pcq.commons.tests.utils.TestUtils.jsonStringFromFile;

@SuppressWarnings({"PMD.ExcessiveImports", "PMD.TooManyMethods", "PMD.JUnit4TestShouldUseTestAnnotation"})
class SubmitAnswersServiceTest {

    @Mock
    private Environment environment;

    @Mock
    private ProtectedCharacteristicsRepository protectedCharacteristicsRepository;

    @InjectMocks
    private SubmitAnswersService submitAnswersService;

    private static final String ERROR_MSG_PREFIX = "Test failed because of exception during execution. Message is ";

    private static final String STATUS_CODE_400_MSG = "Expected 400 status code";

    private static final String INVALID_ERROR = "Invalid Request";

    private static final String INVALID_ERROR_PROPERTY = "api-error-messages.bad_request";

    private static final String RESPONSE_NULL_MSG = "Response is null";

    private static final String RESPONSE_BODY_NULL_MSG = "Response Body is null";

    private static final String SCHEMA_FILE_PROPERTY = "api-schema-file.submitanswer-schema";

    private static final String SCHEMA_FILE = "JsonSchema/submitAnswersSchema.json";

    private static final String API_VERSION_PROPERTY = "api-version-number";

    private static final String TEST_PCQ_ID = "T1234";

    private static final String DB_ENCRYPTION_KEY = "security.db.backend-encryption-key";

    private static final String STATUS_CODE_201_MSG = "Expected 201 status code";

    private static final String CREATED_MESSAGE_PROPERTY = "api-error-messages.created";

    private static final String UPDATED_MESSAGE_PROPERTY = "api-error-messages.updated";
    private static final String TEST_TIME_STAMP = "2020-03-05T09:13:45.000Z";
    private static final String TEST_DOB = "1970-01-01T00:00:00.000Z";

    @BeforeEach
    void setUp() {

        MockitoAnnotations.initMocks(this);
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }


    @Test
    void testNoHeaders() {
        when(environment.getProperty(INVALID_ERROR_PROPERTY)).thenReturn(INVALID_ERROR);

        try {
            PcqAnswerRequest pcqAnswerRequest = new PcqAnswerRequest("C1234");
            ResponseEntity<Object> responseEntity = submitAnswersService.processPcqAnswers(null, pcqAnswerRequest);

            assertNotNull(responseEntity, RESPONSE_NULL_MSG);

            Object responseMap = responseEntity.getBody();
            assertNotNull(responseMap, RESPONSE_BODY_NULL_MSG);
            assertEquals(400, responseEntity.getStatusCode().value(), STATUS_CODE_400_MSG);


        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage(), e);
        }

    }

    @Test
    void testNoHeadersProcessOptOut() {
        when(environment.getProperty(INVALID_ERROR_PROPERTY)).thenReturn(INVALID_ERROR);

        try {
            PcqAnswerRequest pcqAnswerRequest = new PcqAnswerRequest("C1234");
            ResponseEntity<Object> responseEntity = submitAnswersService.processOptOut(null, pcqAnswerRequest);

            assertNotNull(responseEntity, RESPONSE_NULL_MSG);

            Object responseMap = responseEntity.getBody();
            assertNotNull(responseMap, RESPONSE_BODY_NULL_MSG);
            assertEquals(400, responseEntity.getStatusCode().value(), STATUS_CODE_400_MSG);


        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage(), e);
        }

    }

    @Test
    void testInvalidJson() {
        when(environment.getProperty(INVALID_ERROR_PROPERTY)).thenReturn(INVALID_ERROR);
        when(environment.getProperty(SCHEMA_FILE_PROPERTY)).thenReturn(SCHEMA_FILE);

        try {
            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/InvalidJson1.json");
            PcqAnswerRequest pcqAnswerRequest = jsonObjectFromString(jsonStringRequest);
            ResponseEntity<Object> responseEntity = submitAnswersService.processPcqAnswers(getTestHeader(),
                                                                                           pcqAnswerRequest);

            assertNotNull(responseEntity, RESPONSE_NULL_MSG);

            Object responseMap = responseEntity.getBody();
            assertNotNull(responseMap, RESPONSE_BODY_NULL_MSG);
            assertEquals(400, responseEntity.getStatusCode().value(),STATUS_CODE_400_MSG);


        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage(), e);
        }

    }

    @Test
    void testInvalidJsonProcessOptOut() {
        when(environment.getProperty(INVALID_ERROR_PROPERTY)).thenReturn(INVALID_ERROR);
        when(environment.getProperty(SCHEMA_FILE_PROPERTY)).thenReturn(SCHEMA_FILE);

        try {
            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/InvalidOptOutJson1.json");
            PcqAnswerRequest pcqAnswerRequest = jsonObjectFromString(jsonStringRequest);
            ResponseEntity<Object> responseEntity = submitAnswersService.processOptOut(getTestHeader(),
                                                                                           pcqAnswerRequest);

            assertNotNull(responseEntity, RESPONSE_NULL_MSG);

            Object responseMap = responseEntity.getBody();
            assertNotNull(responseMap, RESPONSE_BODY_NULL_MSG);
            assertEquals(400, responseEntity.getStatusCode().value(),STATUS_CODE_400_MSG);


        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage(), e);
        }

    }

    @Test
    void testInvalidPaperChannel() {
        when(environment.getProperty(INVALID_ERROR_PROPERTY)).thenReturn(INVALID_ERROR);
        when(environment.getProperty(SCHEMA_FILE_PROPERTY)).thenReturn(SCHEMA_FILE);
        when(environment.getProperty(API_VERSION_PROPERTY)).thenReturn("1");

        try {
            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/FirstSubmitAnswerDcnMissing.json");
            PcqAnswerRequest pcqAnswerRequest = jsonObjectFromString(jsonStringRequest);
            ResponseEntity<Object> responseEntity = submitAnswersService.processPcqAnswers(getTestHeader(),
                                                                                       pcqAnswerRequest);

            assertNotNull(responseEntity, RESPONSE_NULL_MSG);

            Object responseMap = responseEntity.getBody();
            assertNotNull(responseMap, RESPONSE_BODY_NULL_MSG);
            assertEquals(400, responseEntity.getStatusCode().value(),STATUS_CODE_400_MSG);


        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage(), e);
        }

    }

    @Test
    void testRecordAlreadyExistsPaperChannel() {
        when(environment.getProperty(INVALID_ERROR_PROPERTY)).thenReturn(INVALID_ERROR);
        when(environment.getProperty(SCHEMA_FILE_PROPERTY)).thenReturn(SCHEMA_FILE);
        when(environment.getProperty(API_VERSION_PROPERTY)).thenReturn("1");

        try {
            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/FirstSubmitAnswerPaperChannel.json");
            PcqAnswerRequest pcqAnswerRequest = jsonObjectFromString(jsonStringRequest);

            ProtectedCharacteristics targetObject = new ProtectedCharacteristics();
            List<ProtectedCharacteristics> protectedCharacteristicsList = new ArrayList<>();
            protectedCharacteristicsList.add(targetObject);
            when(protectedCharacteristicsRepository.findByDcnNumber(any(String.class),
                 Mockito.eq(null)))
                .thenReturn(protectedCharacteristicsList);

            ResponseEntity<Object> responseEntity = submitAnswersService.processPcqAnswers(getTestHeader(),
                                                                                       pcqAnswerRequest);

            assertNotNull(responseEntity, RESPONSE_NULL_MSG);

            Object responseMap = responseEntity.getBody();
            assertNotNull(responseMap, RESPONSE_BODY_NULL_MSG);
            assertEquals(409, responseEntity.getStatusCode().value(),STATUS_CODE_400_MSG);


        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage(), e);
        }

    }

    @Test
    void testInvalidVersion() {
        when(environment.getProperty(INVALID_ERROR_PROPERTY)).thenReturn(INVALID_ERROR);
        when(environment.getProperty(SCHEMA_FILE_PROPERTY)).thenReturn(SCHEMA_FILE);
        when(environment.getProperty(API_VERSION_PROPERTY)).thenReturn("1");

        try {
            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/InvalidVersion.json");
            PcqAnswerRequest pcqAnswerRequest = jsonObjectFromString(jsonStringRequest);
            ResponseEntity<Object> responseEntity = submitAnswersService.processPcqAnswers(getTestHeader(),
                                                                                           pcqAnswerRequest);

            assertNotNull(responseEntity, RESPONSE_NULL_MSG);

            Object responseMap = responseEntity.getBody();
            assertNotNull(responseMap, RESPONSE_BODY_NULL_MSG);
            assertEquals(403, responseEntity.getStatusCode().value(), "Expected 403 status code");


        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage(), e);
        }

    }

    @Test
    void testInvalidVersionProcessOptOut() {
        when(environment.getProperty(INVALID_ERROR_PROPERTY)).thenReturn(INVALID_ERROR);
        when(environment.getProperty(SCHEMA_FILE_PROPERTY)).thenReturn(SCHEMA_FILE);
        when(environment.getProperty(API_VERSION_PROPERTY)).thenReturn("1");

        try {
            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/InvalidVersionOptOut.json");
            PcqAnswerRequest pcqAnswerRequest = jsonObjectFromString(jsonStringRequest);
            ResponseEntity<Object> responseEntity = submitAnswersService.processOptOut(getTestHeader(),
                                                                                           pcqAnswerRequest);

            assertNotNull(responseEntity, RESPONSE_NULL_MSG);

            Object responseMap = responseEntity.getBody();
            assertNotNull(responseMap, RESPONSE_BODY_NULL_MSG);
            assertEquals(403, responseEntity.getStatusCode().value(), "Expected 403 status code");


        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage(), e);
        }

    }

    @Test
    void testSubmitFirstTime() {
        when(environment.getProperty(SCHEMA_FILE_PROPERTY)).thenReturn(SCHEMA_FILE);
        when(environment.getProperty(API_VERSION_PROPERTY)).thenReturn("1");
        when(environment.getProperty(CREATED_MESSAGE_PROPERTY)).thenReturn("Successfully created first record");
        when(environment.getProperty(DB_ENCRYPTION_KEY)).thenReturn("ThisIsATestKeyForEncryption");
        String pcqId = TEST_PCQ_ID;

        try {
            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/FirstSubmitAnswer.json");
            PcqAnswerRequest pcqAnswerRequest = jsonObjectFromString(jsonStringRequest);

            Optional<ProtectedCharacteristics> protectedCharacteristicsOptional = Optional.empty();
            ProtectedCharacteristics targetObject = new ProtectedCharacteristics();

            String encryptionKey = environment.getProperty(DB_ENCRYPTION_KEY);
            when(protectedCharacteristicsRepository.findByPcqId(pcqId,encryptionKey))
                .thenReturn(protectedCharacteristicsOptional);
            doNothing().when(protectedCharacteristicsRepository).saveProtectedCharacteristicsWithEncryption(
                targetObject,encryptionKey);

            ResponseEntity<Object> responseEntity = submitAnswersService.processPcqAnswers(getTestHeader(),
                                                                                           pcqAnswerRequest);

            assertNotNull(responseEntity, RESPONSE_NULL_MSG);

            Object responseMap = responseEntity.getBody();
            assertNotNull(responseMap, RESPONSE_BODY_NULL_MSG);
            assertEquals(201, responseEntity.getStatusCode().value(), STATUS_CODE_201_MSG);


        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage(), e);
        }

    }

    @Test
    void testSubmitPaperChannel() {
        when(environment.getProperty(SCHEMA_FILE_PROPERTY)).thenReturn(SCHEMA_FILE);
        when(environment.getProperty(API_VERSION_PROPERTY)).thenReturn("1");
        when(environment.getProperty(CREATED_MESSAGE_PROPERTY)).thenReturn("Successfully created paper channel record");
        when(environment.getProperty(DB_ENCRYPTION_KEY)).thenReturn("ThisIsATestKeyForEncryption");
        String pcqId = TEST_PCQ_ID;

        try {
            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/FirstSubmitAnswerPaperChannel.json");
            PcqAnswerRequest pcqAnswerRequest = jsonObjectFromString(jsonStringRequest);

            Optional<ProtectedCharacteristics> protectedCharacteristicsOptional = Optional.empty();
            ProtectedCharacteristics targetObject = new ProtectedCharacteristics();

            List<ProtectedCharacteristics> protectedCharacteristicsList = new ArrayList<>();
            String encryptionKey = environment.getProperty(DB_ENCRYPTION_KEY);
            when(protectedCharacteristicsRepository.findByDcnNumber(any(String.class),
                 Mockito.eq(encryptionKey)))
                .thenReturn(protectedCharacteristicsList);

            when(protectedCharacteristicsRepository.findByPcqId(pcqId,encryptionKey))
                .thenReturn(protectedCharacteristicsOptional);
            doNothing().when(protectedCharacteristicsRepository).saveProtectedCharacteristicsWithEncryption(
                targetObject,encryptionKey);

            ResponseEntity<Object> responseEntity = submitAnswersService.processPcqAnswers(getTestHeader(),
                                                                                           pcqAnswerRequest);

            assertNotNull(responseEntity, RESPONSE_NULL_MSG);

            Object responseMap = responseEntity.getBody();
            assertNotNull(responseMap, RESPONSE_BODY_NULL_MSG);
            assertEquals(201, responseEntity.getStatusCode().value(), STATUS_CODE_201_MSG);


        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage(), e);
        }

    }

    @Test
    void testOptOutSuccess() {
        when(environment.getProperty(SCHEMA_FILE_PROPERTY)).thenReturn(SCHEMA_FILE);
        when(environment.getProperty(API_VERSION_PROPERTY)).thenReturn("1");
        when(environment.getProperty(CREATED_MESSAGE_PROPERTY)).thenReturn("Success");
        //String pcqId = TEST_PCQ_ID;

        try {
            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/FirstSubmitAnswerOptOut.json");
            PcqAnswerRequest pcqAnswerRequest = jsonObjectFromString(jsonStringRequest);

            /*int resultCount = 1;
            int dobProvided = 1;
            when(protectedCharacteristicsRepository.updateCharacteristics(
                                                        dobProvided,null,
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
                                                      pcqId,null))
                                .thenReturn(resultCount);*/

            ResponseEntity<Object> responseEntity = submitAnswersService.processOptOut(getTestHeader(),
                                                                                           pcqAnswerRequest);

            assertNotNull(responseEntity, RESPONSE_NULL_MSG);

            Object responseMap = responseEntity.getBody();
            assertNotNull(responseMap, RESPONSE_BODY_NULL_MSG);
            assertEquals(201, responseEntity.getStatusCode().value(), STATUS_CODE_201_MSG);


        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage(), e);
        }

    }

    @Test
    void testOptOutTrueRecord() {
        when(environment.getProperty(INVALID_ERROR_PROPERTY)).thenReturn(INVALID_ERROR);
        when(environment.getProperty(SCHEMA_FILE_PROPERTY)).thenReturn(SCHEMA_FILE);
        when(environment.getProperty(API_VERSION_PROPERTY)).thenReturn("1");
        when(environment.getProperty(CREATED_MESSAGE_PROPERTY)).thenReturn("Success");
        when(environment.getProperty(UPDATED_MESSAGE_PROPERTY)).thenReturn("Updated");
        String pcqId = TEST_PCQ_ID;

        try {
            ProtectedCharacteristics targetObject = new ProtectedCharacteristics();
            targetObject.setPcqId(pcqId);
            Optional<ProtectedCharacteristics> protectedCharacteristicsOptional = Optional.of(targetObject);
            Timestamp testTimeStamp = PcqUtils.getTimeFromString(TEST_TIME_STAMP);
            int resultCount = 1;
            when(protectedCharacteristicsRepository.findByPcqId(pcqId,null))
                .thenReturn(protectedCharacteristicsOptional);
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
                                                                          testTimeStamp,true,
                                                                          pcqId,testTimeStamp))
                                        .thenReturn(resultCount);

            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/FirstSubmitAnswerOptOut.json");
            PcqAnswerRequest pcqAnswerRequest = jsonObjectFromString(jsonStringRequest);

            ResponseEntity<Object> responseEntity = submitAnswersService.processOptOut(getTestHeader(),
                                                                                       pcqAnswerRequest);

            assertNotNull(responseEntity, RESPONSE_NULL_MSG);

            Object responseMap = responseEntity.getBody();
            assertNotNull(responseMap, RESPONSE_BODY_NULL_MSG);
            assertEquals(200, responseEntity.getStatusCode().value(), "Expected 200 status code");


        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage(), e);
        }

    }

    @Test
    void testSubmitSecondTime() {
        when(environment.getProperty(SCHEMA_FILE_PROPERTY)).thenReturn(SCHEMA_FILE);
        when(environment.getProperty(API_VERSION_PROPERTY)).thenReturn("1");
        when(environment.getProperty(CREATED_MESSAGE_PROPERTY)).thenReturn("Successfully updated second time");
        String pcqId = TEST_PCQ_ID;
        try {

            ProtectedCharacteristics targetObject = new ProtectedCharacteristics();
            targetObject.setPcqId(pcqId);
            Optional<ProtectedCharacteristics> protectedCharacteristicsOptional = Optional.of(targetObject);
            int resultCount = 1;
            int dobProvided = 1;
            Date testDob = new Date(PcqUtils.getTimeFromString(TEST_DOB).getTime());
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
                                                                          testTimeStamp, false,pcqId, testTimeStamp)
            ).thenReturn(resultCount);

            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/DobSubmitAnswer.json");
            PcqAnswerRequest pcqAnswerRequest = jsonObjectFromString(jsonStringRequest);
            ResponseEntity<Object> responseEntity = submitAnswersService.processPcqAnswers(getTestHeader(),
                                                                                           pcqAnswerRequest);

            assertNotNull(responseEntity, RESPONSE_NULL_MSG);

            Object responseMap = responseEntity.getBody();
            assertNotNull(responseMap, RESPONSE_BODY_NULL_MSG);
            assertEquals(201, responseEntity.getStatusCode().value(), STATUS_CODE_201_MSG);


        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage(), e);
        }

    }

    @Test
    void testStaleCompletedDate() {
        when(environment.getProperty(SCHEMA_FILE_PROPERTY)).thenReturn(SCHEMA_FILE);
        when(environment.getProperty(API_VERSION_PROPERTY)).thenReturn("1");
        when(environment.getProperty("api-error-messages.accepted")).thenReturn("Success");
        String pcqId = TEST_PCQ_ID;
        try {

            ProtectedCharacteristics targetObject = new ProtectedCharacteristics();
            targetObject.setPcqId(pcqId);
            Optional<ProtectedCharacteristics> protectedCharacteristicsOptional = Optional.of(targetObject);
            int resultCount = 0;
            int dobProvided = 1;
            Date testDob = new Date(PcqUtils.getTimeFromString(TEST_DOB).getTime());
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
                                                                          testTimeStamp, null,pcqId, testTimeStamp)
            ).thenReturn(resultCount);

            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/DobSubmitAnswer.json");
            PcqAnswerRequest pcqAnswerRequest = jsonObjectFromString(jsonStringRequest);
            ResponseEntity<Object> responseEntity = submitAnswersService.processPcqAnswers(getTestHeader(),
                                                                                           pcqAnswerRequest);

            assertNotNull(responseEntity, RESPONSE_NULL_MSG);

            Object responseMap = responseEntity.getBody();
            assertNotNull(responseMap, RESPONSE_BODY_NULL_MSG);
            assertEquals(202, responseEntity.getStatusCode().value(), "Expected 202 status code");


        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage(), e);
        }

    }

    @Test
    void testInternalError() {
        when(environment.getProperty(SCHEMA_FILE_PROPERTY)).thenReturn(SCHEMA_FILE);
        when(environment.getProperty(API_VERSION_PROPERTY)).thenReturn("1");
        when(environment.getProperty("api-error-messages.internal_error")).thenReturn("Unknown error occurred");
        when(environment.getProperty(DB_ENCRYPTION_KEY)).thenReturn("ThisIsATestKeyForEncryption");
        String pcqId = TEST_PCQ_ID;

        try {

            ProtectedCharacteristics targetObject = new ProtectedCharacteristics();
            targetObject.setPcqId(pcqId);
            Optional<ProtectedCharacteristics> protectedCharacteristicsOptional = Optional.of(targetObject);

            String encryptionKey = environment.getProperty(DB_ENCRYPTION_KEY);
            when(protectedCharacteristicsRepository.findByPcqId(pcqId,encryptionKey))
                .thenReturn(protectedCharacteristicsOptional);
            doThrow(new NullPointerException()).when(protectedCharacteristicsRepository)
                .saveProtectedCharacteristicsWithEncryption(targetObject,encryptionKey);

            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/FirstSubmitAnswer.json");
            PcqAnswerRequest pcqAnswerRequest = jsonObjectFromString(jsonStringRequest);
            ResponseEntity<Object> responseEntity = submitAnswersService.processPcqAnswers(getTestHeader(),
                                                                                           pcqAnswerRequest);

            assertNotNull(responseEntity, RESPONSE_NULL_MSG);

            Object responseMap = responseEntity.getBody();
            assertNotNull(responseMap, RESPONSE_BODY_NULL_MSG);
            assertEquals(500, responseEntity.getStatusCode().value(), "Expected 500 status code");


        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage(), e);
        }

    }

    @Test
    void testInternalErrorForOptOut() {
        when(environment.getProperty(SCHEMA_FILE_PROPERTY)).thenReturn(SCHEMA_FILE);
        when(environment.getProperty(API_VERSION_PROPERTY)).thenReturn("1");
        when(environment.getProperty("api-error-messages.internal_error")).thenReturn("Unknown error occurred");
        String pcqId = TEST_PCQ_ID;

        try {
            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/FirstSubmitAnswerOptOut.json");
            PcqAnswerRequest pcqAnswerRequest = jsonObjectFromString(jsonStringRequest);
            when(protectedCharacteristicsRepository.findByPcqId(pcqId,null))
                                                .thenThrow(NullPointerException.class);

            ResponseEntity<Object> responseEntity = submitAnswersService.processOptOut(getTestHeader(),
                                                                                           pcqAnswerRequest);

            assertNotNull(responseEntity, RESPONSE_NULL_MSG);

            Object responseMap = responseEntity.getBody();
            assertNotNull(responseMap, RESPONSE_BODY_NULL_MSG);
            assertEquals(500, responseEntity.getStatusCode().value(), "Expected 500 status code");


        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage(), e);
        }

    }

    @Test
    void testGetProtectedCharacteristicsPositive() {
        String pcqId = TEST_PCQ_ID;

        try {

            ProtectedCharacteristics targetObject = new ProtectedCharacteristics();
            targetObject.setPcqId(pcqId);
            Optional<ProtectedCharacteristics> protectedCharacteristicsOptional = Optional.of(targetObject);

            when(protectedCharacteristicsRepository.findByPcqId(pcqId,null))
                .thenReturn(protectedCharacteristicsOptional);

            ProtectedCharacteristics actualObject = submitAnswersService.getProtectedCharacteristicsById(pcqId);

            assertNotNull(actualObject, RESPONSE_NULL_MSG);
            assertEquals(pcqId, actualObject.getPcqId(), "Not expected pcq id");

            verify(protectedCharacteristicsRepository, times(1)).findByPcqId(pcqId,null);

        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage(), e);
        }

    }

    @Test
    void testGetProtectedCharacteristicsNegative() {
        String pcqId = TEST_PCQ_ID;

        try {

            Optional<ProtectedCharacteristics> protectedCharacteristicsOptional = Optional.empty();

            when(protectedCharacteristicsRepository.findByPcqId(pcqId,null))
                .thenReturn(protectedCharacteristicsOptional);

            ProtectedCharacteristics actualObject = submitAnswersService.getProtectedCharacteristicsById(pcqId);

            assertNull(actualObject, RESPONSE_NULL_MSG);

            verify(protectedCharacteristicsRepository, times(1)).findByPcqId(pcqId,null);

        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage(), e);
        }

    }

    @Test
    void testSubmitMainLanguageEnglish() {
        when(environment.getProperty(SCHEMA_FILE_PROPERTY)).thenReturn(SCHEMA_FILE);
        when(environment.getProperty(API_VERSION_PROPERTY)).thenReturn("1");
        when(environment.getProperty(CREATED_MESSAGE_PROPERTY)).thenReturn("Successfully created english language");
        String pcqId = TEST_PCQ_ID;
        try {

            ProtectedCharacteristics targetObject = new ProtectedCharacteristics();
            targetObject.setPcqId(pcqId);
            Optional<ProtectedCharacteristics> protectedCharacteristicsOptional = Optional.of(targetObject);
            int resultCount = 1;
            int dobProvided = 1;
            int mainLanguage = 4;
            Date testDob = new Date(PcqUtils.getTimeFromString(TEST_DOB).getTime());
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
                                                                          testTimeStamp, false,pcqId, testTimeStamp)
            ).thenReturn(resultCount);

            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/MainLanguageAnswerEnglish.json");
            PcqAnswerRequest pcqAnswerRequest = jsonObjectFromString(jsonStringRequest);
            ResponseEntity<Object> responseEntity = submitAnswersService.processPcqAnswers(getTestHeader(),
                                                                                           pcqAnswerRequest);

            assertNotNull(responseEntity, RESPONSE_NULL_MSG);

            Object responseMap = responseEntity.getBody();
            assertNotNull(responseMap, RESPONSE_BODY_NULL_MSG);
            assertEquals(201, responseEntity.getStatusCode().value(), STATUS_CODE_201_MSG);


        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage(), e);
        }

    }

    @Test
    void testSubmitMainLanguageWelsh() {
        when(environment.getProperty(SCHEMA_FILE_PROPERTY)).thenReturn(SCHEMA_FILE);
        when(environment.getProperty(API_VERSION_PROPERTY)).thenReturn("1");
        when(environment.getProperty(CREATED_MESSAGE_PROPERTY)).thenReturn("Successfully created welsh language");
        String pcqId = TEST_PCQ_ID;
        try {

            ProtectedCharacteristics targetObject = new ProtectedCharacteristics();
            targetObject.setPcqId(pcqId);
            Optional<ProtectedCharacteristics> protectedCharacteristicsOptional = Optional.of(targetObject);
            int resultCount = 1;
            int dobProvided = 1;
            int mainLanguage = 3;
            Date testDob = new Date(PcqUtils.getTimeFromString(TEST_DOB).getTime());
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
                                                                          testTimeStamp, false,pcqId, testTimeStamp)
            ).thenReturn(resultCount);

            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/MainLanguageAnswerWelsh.json");
            PcqAnswerRequest pcqAnswerRequest = jsonObjectFromString(jsonStringRequest);
            ResponseEntity<Object> responseEntity = submitAnswersService.processPcqAnswers(getTestHeader(),
                                                                                           pcqAnswerRequest);

            assertNotNull(responseEntity, RESPONSE_NULL_MSG);

            Object responseMap = responseEntity.getBody();
            assertNotNull(responseMap, RESPONSE_BODY_NULL_MSG);
            assertEquals(201, responseEntity.getStatusCode().value(), STATUS_CODE_201_MSG);


        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage(), e);
        }

    }

}

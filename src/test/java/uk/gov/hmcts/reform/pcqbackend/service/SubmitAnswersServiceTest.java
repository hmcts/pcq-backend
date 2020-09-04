package uk.gov.hmcts.reform.pcqbackend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.pcqbackend.domain.ProtectedCharacteristics;
import uk.gov.hmcts.reform.pcqbackend.model.PcqAnswerRequest;
import uk.gov.hmcts.reform.pcqbackend.repository.ProtectedCharacteristicsRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.Security;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings({"PMD.ExcessiveImports", "PMD.TooManyMethods", "PMD.JUnit4TestShouldUseTestAnnotation"})
public class SubmitAnswersServiceTest {


    @Mock
    Environment environment;

    @Mock
    ProtectedCharacteristicsRepository protectedCharacteristicsRepository;

    @InjectMocks
    SubmitAnswersService submitAnswersService;


    private static final String CO_RELATION_ID_FOR_TEST = "Test-Id";

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

    @BeforeEach
    public void setUp() {

        MockitoAnnotations.initMocks(this);
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }


    @Test
    public void testNoHeaders() {
        when(environment.getProperty(INVALID_ERROR_PROPERTY)).thenReturn(INVALID_ERROR);

        try {
            PcqAnswerRequest pcqAnswerRequest = new PcqAnswerRequest("C1234");
            ResponseEntity<Object> responseEntity = submitAnswersService.processPcqAnswers(null, pcqAnswerRequest);

            assertNotNull(responseEntity, RESPONSE_NULL_MSG);

            Object responseMap = responseEntity.getBody();
            assertNotNull(responseMap, RESPONSE_BODY_NULL_MSG);
            assertEquals(400, responseEntity.getStatusCodeValue(), STATUS_CODE_400_MSG);


        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage(), e);
        }

    }

    @Test
    public void testNoHeadersProcessOptOut() {
        when(environment.getProperty(INVALID_ERROR_PROPERTY)).thenReturn(INVALID_ERROR);

        try {
            PcqAnswerRequest pcqAnswerRequest = new PcqAnswerRequest("C1234");
            ResponseEntity<Object> responseEntity = submitAnswersService.processOptOut(null, pcqAnswerRequest);

            assertNotNull(responseEntity, RESPONSE_NULL_MSG);

            Object responseMap = responseEntity.getBody();
            assertNotNull(responseMap, RESPONSE_BODY_NULL_MSG);
            assertEquals(400, responseEntity.getStatusCodeValue(), STATUS_CODE_400_MSG);


        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage(), e);
        }

    }

    @Test
    public void testInvalidJson() {
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
            assertEquals(400, responseEntity.getStatusCodeValue(),STATUS_CODE_400_MSG);


        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage(), e);
        }

    }

    @Test
    public void testInvalidJsonProcessOptOut() {
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
            assertEquals(400, responseEntity.getStatusCodeValue(),STATUS_CODE_400_MSG);


        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage(), e);
        }

    }

    @Test
    public void testInvalidPaperChannel() {
        when(environment.getProperty(INVALID_ERROR_PROPERTY)).thenReturn(INVALID_ERROR);
        when(environment.getProperty(SCHEMA_FILE_PROPERTY)).thenReturn(SCHEMA_FILE);
        when(environment.getProperty(API_VERSION_PROPERTY)).thenReturn("1");

        try {
            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/FirstSubmitAnswerDcnMissing.json");
            PcqAnswerRequest pcqAnswerRequest = jsonObjectFromString(jsonStringRequest);
            ResponseEntity<Object> responseEntity = submitAnswersService.processOptOut(getTestHeader(),
                                                                                       pcqAnswerRequest);

            assertNotNull(responseEntity, RESPONSE_NULL_MSG);

            Object responseMap = responseEntity.getBody();
            assertNotNull(responseMap, RESPONSE_BODY_NULL_MSG);
            assertEquals(400, responseEntity.getStatusCodeValue(),STATUS_CODE_400_MSG);


        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage(), e);
        }

    }

    @Test
    public void testInvalidVersion() {
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
            assertEquals(403, responseEntity.getStatusCodeValue(), "Expected 403 status code");


        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage(), e);
        }

    }

    @Test
    public void testInvalidVersionProcessOptOut() {
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
            assertEquals(403, responseEntity.getStatusCodeValue(), "Expected 403 status code");


        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage(), e);
        }

    }

    @Test
    public void testSubmitFirstTime() {
        when(environment.getProperty(SCHEMA_FILE_PROPERTY)).thenReturn(SCHEMA_FILE);
        when(environment.getProperty(API_VERSION_PROPERTY)).thenReturn("1");
        when(environment.getProperty("api-error-messages.created")).thenReturn("Successfully created");
        when(environment.getProperty(DB_ENCRYPTION_KEY)).thenReturn("ThisIsATestKeyForEncryption");
        String pcqId = TEST_PCQ_ID;

        try {
            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/FirstSubmitAnswer.json");
            PcqAnswerRequest pcqAnswerRequest = jsonObjectFromString(jsonStringRequest);

            Optional<ProtectedCharacteristics> protectedCharacteristicsOptional = Optional.empty();
            ProtectedCharacteristics targetObject = new ProtectedCharacteristics();

            when(protectedCharacteristicsRepository.findById(pcqId)).thenReturn(protectedCharacteristicsOptional);
            when(protectedCharacteristicsRepository.save(any(ProtectedCharacteristics.class))).thenReturn(targetObject);

            ResponseEntity<Object> responseEntity = submitAnswersService.processPcqAnswers(getTestHeader(),
                                                                                           pcqAnswerRequest);

            assertNotNull(responseEntity, RESPONSE_NULL_MSG);

            Object responseMap = responseEntity.getBody();
            assertNotNull(responseMap, RESPONSE_BODY_NULL_MSG);
            assertEquals(201, responseEntity.getStatusCodeValue(), "Expected 201 status code");


        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage(), e);
        }

    }

    @Test
    public void testSubmitPaperChannel() {
        when(environment.getProperty(SCHEMA_FILE_PROPERTY)).thenReturn(SCHEMA_FILE);
        when(environment.getProperty(API_VERSION_PROPERTY)).thenReturn("1");
        when(environment.getProperty("api-error-messages.created")).thenReturn("Successfully created");
        when(environment.getProperty(DB_ENCRYPTION_KEY)).thenReturn("ThisIsATestKeyForEncryption");
        String pcqId = TEST_PCQ_ID;

        try {
            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/FirstSubmitAnswerPaperChannel.json");
            PcqAnswerRequest pcqAnswerRequest = jsonObjectFromString(jsonStringRequest);

            Optional<ProtectedCharacteristics> protectedCharacteristicsOptional = Optional.empty();
            ProtectedCharacteristics targetObject = new ProtectedCharacteristics();

            when(protectedCharacteristicsRepository.findById(pcqId)).thenReturn(protectedCharacteristicsOptional);
            when(protectedCharacteristicsRepository.save(any(ProtectedCharacteristics.class))).thenReturn(targetObject);

            ResponseEntity<Object> responseEntity = submitAnswersService.processPcqAnswers(getTestHeader(),
                                                                                           pcqAnswerRequest);

            assertNotNull(responseEntity, RESPONSE_NULL_MSG);

            Object responseMap = responseEntity.getBody();
            assertNotNull(responseMap, RESPONSE_BODY_NULL_MSG);
            assertEquals(201, responseEntity.getStatusCodeValue(), "Expected 201 status code");


        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage(), e);
        }

    }

    @Test
    public void testOptOutSuccess() {
        when(environment.getProperty(SCHEMA_FILE_PROPERTY)).thenReturn(SCHEMA_FILE);
        when(environment.getProperty(API_VERSION_PROPERTY)).thenReturn("1");
        when(environment.getProperty("api-error-messages.accepted")).thenReturn("Success");
        String pcqId = TEST_PCQ_ID;

        try {
            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/FirstSubmitAnswerOptOut.json");
            PcqAnswerRequest pcqAnswerRequest = jsonObjectFromString(jsonStringRequest);

            int resultCount = 1;
            when(protectedCharacteristicsRepository.deletePcqRecord(pcqId)).thenReturn(resultCount);

            ResponseEntity<Object> responseEntity = submitAnswersService.processOptOut(getTestHeader(),
                                                                                           pcqAnswerRequest);

            assertNotNull(responseEntity, RESPONSE_NULL_MSG);

            Object responseMap = responseEntity.getBody();
            assertNotNull(responseMap, RESPONSE_BODY_NULL_MSG);
            assertEquals(200, responseEntity.getStatusCodeValue(), "Expected 200 status code");


        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage(), e);
        }

    }

    @Test
    public void testOptOutFailRecordNotFound() {
        when(environment.getProperty(INVALID_ERROR_PROPERTY)).thenReturn(INVALID_ERROR);
        when(environment.getProperty(SCHEMA_FILE_PROPERTY)).thenReturn(SCHEMA_FILE);
        when(environment.getProperty(API_VERSION_PROPERTY)).thenReturn("1");
        when(environment.getProperty("api-error-messages.accepted")).thenReturn("Success");
        String pcqId = TEST_PCQ_ID;

        try {
            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/FirstSubmitAnswerOptOut.json");
            PcqAnswerRequest pcqAnswerRequest = jsonObjectFromString(jsonStringRequest);

            int resultCount = 0;
            when(protectedCharacteristicsRepository.deletePcqRecord(pcqId)).thenReturn(resultCount);

            ResponseEntity<Object> responseEntity = submitAnswersService.processOptOut(getTestHeader(),
                                                                                       pcqAnswerRequest);

            assertNotNull(responseEntity, RESPONSE_NULL_MSG);

            Object responseMap = responseEntity.getBody();
            assertNotNull(responseMap, RESPONSE_BODY_NULL_MSG);
            assertEquals(400, responseEntity.getStatusCodeValue(), STATUS_CODE_400_MSG);


        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage(), e);
        }

    }

    @Test
    public void testSubmitSecondTime() {
        when(environment.getProperty(SCHEMA_FILE_PROPERTY)).thenReturn(SCHEMA_FILE);
        when(environment.getProperty(API_VERSION_PROPERTY)).thenReturn("1");
        when(environment.getProperty("api-error-messages.created")).thenReturn("Successfully created");
        String pcqId = TEST_PCQ_ID;
        try {

            ProtectedCharacteristics targetObject = new ProtectedCharacteristics();
            targetObject.setPcqId(pcqId);
            Optional<ProtectedCharacteristics> protectedCharacteristicsOptional = Optional.of(targetObject);
            int resultCount = 1;
            int dobProvided = 1;
            Date testDob = new Date(getTimeFromString("1970-01-01T00:00:00.000Z").getTime());
            Timestamp testTimeStamp = getTimeFromString("2020-03-05T09:13:45.000Z");

            when(protectedCharacteristicsRepository.findById(pcqId)).thenReturn(protectedCharacteristicsOptional);
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
                                                                          testTimeStamp, pcqId, testTimeStamp)
            ).thenReturn(resultCount);

            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/DobSubmitAnswer.json");
            PcqAnswerRequest pcqAnswerRequest = jsonObjectFromString(jsonStringRequest);
            ResponseEntity<Object> responseEntity = submitAnswersService.processPcqAnswers(getTestHeader(),
                                                                                           pcqAnswerRequest);

            assertNotNull(responseEntity, RESPONSE_NULL_MSG);

            Object responseMap = responseEntity.getBody();
            assertNotNull(responseMap, RESPONSE_BODY_NULL_MSG);
            assertEquals(201, responseEntity.getStatusCodeValue(), "Expected 201 status code");


        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage(), e);
        }

    }

    @Test
    public void testStaleCompletedDate() {
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
            Date testDob = new Date(getTimeFromString("1970-01-01T00:00:00.000Z").getTime());
            Timestamp testTimeStamp = getTimeFromString("2020-03-05T09:13:45.000Z");

            when(protectedCharacteristicsRepository.findById(pcqId)).thenReturn(protectedCharacteristicsOptional);
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
                                                                          testTimeStamp, pcqId, testTimeStamp)
            ).thenReturn(resultCount);

            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/DobSubmitAnswer.json");
            PcqAnswerRequest pcqAnswerRequest = jsonObjectFromString(jsonStringRequest);
            ResponseEntity<Object> responseEntity = submitAnswersService.processPcqAnswers(getTestHeader(),
                                                                                           pcqAnswerRequest);

            assertNotNull(responseEntity, RESPONSE_NULL_MSG);

            Object responseMap = responseEntity.getBody();
            assertNotNull(responseMap, RESPONSE_BODY_NULL_MSG);
            assertEquals(202, responseEntity.getStatusCodeValue(), "Expected 202 status code");


        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage(), e);
        }

    }

    @Test
    public void testInternalError() {
        when(environment.getProperty(SCHEMA_FILE_PROPERTY)).thenReturn(SCHEMA_FILE);
        when(environment.getProperty(API_VERSION_PROPERTY)).thenReturn("1");
        when(environment.getProperty("api-error-messages.internal_error")).thenReturn("Unknown error occurred");
        when(environment.getProperty(DB_ENCRYPTION_KEY)).thenReturn("ThisIsATestKeyForEncryption");
        String pcqId = TEST_PCQ_ID;

        try {
            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/FirstSubmitAnswer.json");
            PcqAnswerRequest pcqAnswerRequest = jsonObjectFromString(jsonStringRequest);

            Optional<ProtectedCharacteristics> protectedCharacteristicsOptional = Optional.empty();

            when(protectedCharacteristicsRepository.findById(pcqId)).thenReturn(protectedCharacteristicsOptional);
            when(protectedCharacteristicsRepository.save(any(ProtectedCharacteristics.class))).thenThrow(
                NullPointerException.class);

            ResponseEntity<Object> responseEntity = submitAnswersService.processPcqAnswers(getTestHeader(),
                                                                                           pcqAnswerRequest);

            assertNotNull(responseEntity, RESPONSE_NULL_MSG);

            Object responseMap = responseEntity.getBody();
            assertNotNull(responseMap, RESPONSE_BODY_NULL_MSG);
            assertEquals(500, responseEntity.getStatusCodeValue(), "Expected 500 status code");


        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage(), e);
        }

    }

    @Test
    public void testInternalErrorForOptOut() {
        when(environment.getProperty(SCHEMA_FILE_PROPERTY)).thenReturn(SCHEMA_FILE);
        when(environment.getProperty(API_VERSION_PROPERTY)).thenReturn("1");
        when(environment.getProperty("api-error-messages.internal_error")).thenReturn("Unknown error occurred");
        String pcqId = TEST_PCQ_ID;

        try {
            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/FirstSubmitAnswerOptOut.json");
            PcqAnswerRequest pcqAnswerRequest = jsonObjectFromString(jsonStringRequest);

            when(protectedCharacteristicsRepository.deletePcqRecord(pcqId)).thenThrow(
                NullPointerException.class);

            ResponseEntity<Object> responseEntity = submitAnswersService.processOptOut(getTestHeader(),
                                                                                           pcqAnswerRequest);

            assertNotNull(responseEntity, RESPONSE_NULL_MSG);

            Object responseMap = responseEntity.getBody();
            assertNotNull(responseMap, RESPONSE_BODY_NULL_MSG);
            assertEquals(500, responseEntity.getStatusCodeValue(), "Expected 500 status code");


        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage(), e);
        }

    }

    @Ignore
    public void testIllegalStateError() {
        when(environment.getProperty(SCHEMA_FILE_PROPERTY)).thenReturn(SCHEMA_FILE);
        when(environment.getProperty(API_VERSION_PROPERTY)).thenReturn("1");
        when(environment.getProperty("api-error-messages.internal_error")).thenReturn("Unknown error occurred");
        when(environment.getProperty(DB_ENCRYPTION_KEY)).thenReturn(null);
        String pcqId = TEST_PCQ_ID;

        try {
            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/FirstSubmitAnswer.json");
            PcqAnswerRequest pcqAnswerRequest = jsonObjectFromString(jsonStringRequest);

            Optional<ProtectedCharacteristics> protectedCharacteristicsOptional = Optional.empty();

            when(protectedCharacteristicsRepository.findById(pcqId)).thenReturn(protectedCharacteristicsOptional);

            ResponseEntity<Object> responseEntity = submitAnswersService.processPcqAnswers(getTestHeader(),
                                                                                           pcqAnswerRequest);

            assertNotNull(responseEntity, RESPONSE_NULL_MSG);

            Object responseMap = responseEntity.getBody();
            assertNotNull(responseMap, RESPONSE_BODY_NULL_MSG);
            assertEquals(500, responseEntity.getStatusCodeValue(), "Expected 500 status code");


        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage(), e);
        }

    }

    @Test
    public void testGetProtectedCharacteristicsPositive() {
        String pcqId = TEST_PCQ_ID;

        try {

            ProtectedCharacteristics targetObject = new ProtectedCharacteristics();
            targetObject.setPcqId(pcqId);
            Optional<ProtectedCharacteristics> protectedCharacteristicsOptional = Optional.of(targetObject);

            when(protectedCharacteristicsRepository.findById(pcqId)).thenReturn(protectedCharacteristicsOptional);

            ProtectedCharacteristics actualObject = submitAnswersService.getProtectedCharacteristicsById(pcqId);

            assertNotNull(actualObject, RESPONSE_NULL_MSG);
            assertEquals(pcqId, actualObject.getPcqId(), "Not expected pcq id");

            verify(protectedCharacteristicsRepository, times(1)).findById(pcqId);

        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage(), e);
        }

    }

    @Test
    public void testGetProtectedCharacteristicsNegative() {
        String pcqId = TEST_PCQ_ID;

        try {

            Optional<ProtectedCharacteristics> protectedCharacteristicsOptional = Optional.empty();

            when(protectedCharacteristicsRepository.findById(pcqId)).thenReturn(protectedCharacteristicsOptional);

            ProtectedCharacteristics actualObject = submitAnswersService.getProtectedCharacteristicsById(pcqId);

            assertNull(actualObject, RESPONSE_NULL_MSG);

            verify(protectedCharacteristicsRepository, times(1)).findById(pcqId);

        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage(), e);
        }

    }


    /**
     * Obtains a JSON String from a JSON file in the classpath (Resources directory).
     * @param fileName - The name of the Json file from classpath.
     * @return - JSON String from the file.
     * @throws IOException - If there is any issue when reading from the file.
     */
    public static String jsonStringFromFile(String fileName) throws IOException {
        File resource = new ClassPathResource(fileName).getFile();
        return new String(Files.readAllBytes(resource.toPath()));
    }

    public static PcqAnswerRequest jsonObjectFromString(String jsonString) throws IOException {
        return new ObjectMapper().readValue(jsonString, PcqAnswerRequest.class);
    }

    public static List<String> getTestHeader() {
        List<String> headerList =  new ArrayList<>();
        headerList.add(CO_RELATION_ID_FOR_TEST);

        return headerList;
    }

    private Timestamp getTimeFromString(String timeStr) {
        String pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        LocalDateTime localDateTime = LocalDateTime.from(formatter.parse(timeStr));

        return Timestamp.valueOf(localDateTime);
    }

}

package uk.gov.hmcts.reform.pcqbackend.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.reform.pcqbackend.repository.ProtectedCharacteristicsRepository;
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
@SuppressWarnings({"PMD.ExcessiveImports", "PMD.TooManyMethods"})
public class PcqAnswersControllerTest {

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

    @BeforeEach
    public void setUp() {
        this.environment = mock(Environment.class);
        this.protectedCharacteristicsRepository = mock(ProtectedCharacteristicsRepository.class);
        SubmitAnswersService submitAnswersService = new SubmitAnswersService(
            protectedCharacteristicsRepository,
            environment
        );
        this.pcqAnswersController = new PcqAnswersController(submitAnswersService, environment);
        MockitoAnnotations.initMocks(this);

        when(environment.getProperty(INVALID_ERROR_PROPERTY)).thenReturn(INVALID_ERROR);
        when(environment.getProperty(SCHEMA_FILE_PROPERTY)).thenReturn(SCHEMA_FILE);
        when(environment.getProperty("api-version-number")).thenReturn("1");
        when(environment.getProperty("api-error-messages.created")).thenReturn("Successfully created");
        when(environment.getProperty("api-error-messages.internal_error")).thenReturn("Unknown error occurred");
    }

    /**
     * This method tests the submitAnswers API when it is called with all valid parameters and that the answers record
     * is successfully added to the database. The response status code will be 201.
     */
    @DisplayName("Should submit the answers successfully and return with 201 response code")
    @Test
    public void testSubmitAnswersFirstTime()  {

        int pcqId = 1234;
        try {
            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/FirstSubmitAnswer.json");
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);
            //logger.info("testSubmitAnswersFirstTime - Generated Json String is " + jsonStringRequest);

            Optional<ProtectedCharacteristics> protectedCharacteristicsOptional = Optional.empty();
            ProtectedCharacteristics targetObject = new ProtectedCharacteristics();

            when(protectedCharacteristicsRepository.findById(pcqId)).thenReturn(protectedCharacteristicsOptional);
            when(protectedCharacteristicsRepository.save(any(ProtectedCharacteristics.class))).thenReturn(targetObject);
            when(environment.getProperty(HEADER_API_PROPERTY)).thenReturn(HEADER_KEY);
            HttpHeaders mockHeaders = getMockHeader();
            when(mockHeaders.get(HEADER_KEY)).thenReturn(getTestHeader());

            ResponseEntity<Object> actual = pcqAnswersController.submitAnswers(mockHeaders, answerRequest);

            assertNotNull(actual, RESPONSE_NULL_MSG);
            assertEquals(HttpStatus.CREATED, actual.getStatusCode(), "Expected 201 status code");


            verify(environment, times(1)).getProperty(HEADER_API_PROPERTY);
            verify(protectedCharacteristicsRepository, times(1)).findById(pcqId);
            verify(protectedCharacteristicsRepository, times(1)).save(any(
                ProtectedCharacteristics.class));
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
    public void testSubmitAnswersSecondTime()  {

        int pcqId = 1234;
        try {
            //logger.info("testSubmitAnswersFirstTime - Generated Json String is " + jsonStringRequest);

            ProtectedCharacteristics targetObject = new ProtectedCharacteristics();
            targetObject.setPcqId(pcqId);
            Optional<ProtectedCharacteristics> protectedCharacteristicsOptional = Optional.of(targetObject);
            int resultCount = 1;
            int dobProvided = 1;
            Date testDob = Date.valueOf(LocalDate.of(1970, Month.JANUARY, 1));
            Timestamp testTimeStamp = getTimeFromString();

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
                                                                          null, pcqId, testTimeStamp)
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
            verify(protectedCharacteristicsRepository, times(1)).findById(pcqId);
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
                null, pcqId, testTimeStamp);
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
    public void testCompletedDateStale()  {

        int pcqId = 1234;
        try {
            //logger.info("testSubmitAnswersFirstTime - Generated Json String is " + jsonStringRequest);

            ProtectedCharacteristics targetObject = new ProtectedCharacteristics();
            targetObject.setPcqId(pcqId);
            Optional<ProtectedCharacteristics> protectedCharacteristicsOptional = Optional.of(targetObject);
            int resultCount = 0;
            int dobProvided = 1;
            Date testDob = Date.valueOf(LocalDate.of(1970, Month.JANUARY, 1));
            Timestamp testTimeStamp = getTimeFromString();

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
                                                                          null, pcqId, testTimeStamp)
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
            verify(protectedCharacteristicsRepository, times(1)).findById(pcqId);
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
                null, pcqId, testTimeStamp);
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
    public void testInvalidVersionNumber()  {

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
     * This method tests the submitAnswers API when it is called with all valid parameters but the
     * header does not contain the required attribute. The response status code will be 400.
     */
    @DisplayName("Should return with an Invalid Request error code 400")
    @Test
    public void testInvalidRequestForMissingHeader()  {
        int pcqId = 1234;

        try {
            String jsonStringRequest = asJsonString(new PcqAnswerRequest(pcqId));
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
     * This method tests the submitAnswers API when it is called with invalid Json.
     * The response status code will be 400.
     */
    @DisplayName("Should return with an Invalid Request error code 400")
    @Test
    public void testRequestForInvalidJson()  {

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
     * This method tests the submitAnswers API when it is called with all valid parameters but the database call
     * returns an exception. The response status code will be 500.
     */
    @DisplayName("Should return with an 500 error code")
    @Test
    public void testInternalError()  {

        int pcqId = 1234;
        try {
            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/FirstSubmitAnswer.json");
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);
            //logger.info("testSubmitAnswersFirstTime - Generated Json String is " + jsonStringRequest);

            Optional<ProtectedCharacteristics> protectedCharacteristicsOptional = Optional.empty();

            when(protectedCharacteristicsRepository.findById(pcqId)).thenReturn(protectedCharacteristicsOptional);
            when(protectedCharacteristicsRepository.save(any(ProtectedCharacteristics.class)))
                .thenThrow(NullPointerException.class);
            when(environment.getProperty(HEADER_API_PROPERTY)).thenReturn(HEADER_KEY);
            HttpHeaders mockHeaders = getMockHeader();
            when(mockHeaders.get(HEADER_KEY)).thenReturn(getTestHeader());

            ResponseEntity<Object> actual = pcqAnswersController.submitAnswers(mockHeaders, answerRequest);

            assertNotNull(actual, RESPONSE_NULL_MSG);
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actual.getStatusCode(), "Expected 500 status code");


            verify(environment, times(1)).getProperty(HEADER_API_PROPERTY);
            verify(protectedCharacteristicsRepository, times(1)).findById(pcqId);
            verify(protectedCharacteristicsRepository, times(1)).save(any(
                ProtectedCharacteristics.class));
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
    public void testGetAnswerFound()  {

        int pcqId = 1234;
        try {

            ProtectedCharacteristics targetObject = new ProtectedCharacteristics();
            targetObject.setPcqId(1234);
            Optional<ProtectedCharacteristics> protectedCharacteristicsOptional = Optional.of(targetObject);

            when(protectedCharacteristicsRepository.findById(pcqId)).thenReturn(protectedCharacteristicsOptional);

            ResponseEntity<PcqAnswerResponse> actual = pcqAnswersController.getAnswersByPcqId(pcqId);

            assertNotNull(actual, RESPONSE_NULL_MSG);
            assertEquals(HttpStatus.OK, actual.getStatusCode(), "Expected 200 status code");

            PcqAnswerResponse actualBody = actual.getBody();
            assertNotNull(actualBody, RESPONSE_NULL_MSG);

            verify(protectedCharacteristicsRepository, times(1)).findById(pcqId);

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
    public void testGetAnswerNotFound()  {

        int pcqId = 1234;
        try {

            Optional<ProtectedCharacteristics> protectedCharacteristicsOptional = Optional.empty();

            when(protectedCharacteristicsRepository.findById(pcqId)).thenReturn(protectedCharacteristicsOptional);

            assertThrows(EmptyResultDataAccessException.class, () -> pcqAnswersController.getAnswersByPcqId(pcqId));

            verify(protectedCharacteristicsRepository, times(1)).findById(pcqId);

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

    private HttpHeaders getMockHeader() {
        HttpHeaders mockHeader = mock(HttpHeaders.class);
        mockHeader.set("HTTP_X-Correlation-Id", CO_RELATION_ID_FOR_TEST);

        return mockHeader;
    }


    private Timestamp getTimeFromString() {
        String pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        LocalDateTime localDateTime = LocalDateTime.from(formatter.parse("2020-03-05T09:13:45.000Z"));

        return Timestamp.valueOf(localDateTime);
    }

}

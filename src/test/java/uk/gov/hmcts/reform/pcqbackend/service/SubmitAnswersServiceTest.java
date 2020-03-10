package uk.gov.hmcts.reform.pcqbackend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.pcqbackend.model.PcqAnswerRequest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;


public class SubmitAnswersServiceTest {


    @Mock
    Environment environment;

    @InjectMocks
    SubmitAnswersService submitAnswersService;


    private static final String CO_RELATION_ID_FOR_TEST = "Test-Id";

    private static final String ERROR_MSG_PREFIX = "Test failed because of exception during execution. Message is ";

    private static final String INVALID_ERROR = "Invalid Request";

    private static final String INVALID_ERROR_PROPERTY = "api-error-messages.bad_request";

    private static final String RESPONSE_NULL_MSG = "Response is null";

    private static final String RESPONSE_BODY_NULL_MSG = "Response Body is null";

    private static final String SCHEMA_FILE_PROPERTY = "api-schema-file.submitanswer-schema";

    private static final String SCHEMA_FILE = "submitAnswersSchema.json";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }


    @Test
    public void testNoHeaders() {
        when(environment.getProperty(INVALID_ERROR_PROPERTY)).thenReturn(INVALID_ERROR);

        try {
            PcqAnswerRequest pcqAnswerRequest = new PcqAnswerRequest(1234);
            ResponseEntity<Object> responseEntity = submitAnswersService.processPcqAnswers(null, pcqAnswerRequest);

            assertNotNull(responseEntity, RESPONSE_NULL_MSG);

            Object responseMap = responseEntity.getBody();
            assertNotNull(responseMap, RESPONSE_BODY_NULL_MSG);
            assertEquals(responseEntity.getStatusCodeValue(), 400, "Expected 400 status code");


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
            assertEquals(responseEntity.getStatusCodeValue(), 400, "Expected 400 status code");


        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage(), e);
        }

    }

    @Test
    public void testInvalidVersion() {
        when(environment.getProperty(INVALID_ERROR_PROPERTY)).thenReturn(INVALID_ERROR);
        when(environment.getProperty(SCHEMA_FILE_PROPERTY)).thenReturn(SCHEMA_FILE);
        when(environment.getProperty("api-version-number")).thenReturn("1");

        try {
            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/InvalidVersion.json");
            PcqAnswerRequest pcqAnswerRequest = jsonObjectFromString(jsonStringRequest);
            ResponseEntity<Object> responseEntity = submitAnswersService.processPcqAnswers(getTestHeader(),
                                                                                           pcqAnswerRequest);

            assertNotNull(responseEntity, RESPONSE_NULL_MSG);

            Object responseMap = responseEntity.getBody();
            assertNotNull(responseMap, RESPONSE_BODY_NULL_MSG);
            assertEquals(responseEntity.getStatusCodeValue(), 403, "Expected 403 status code");


        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage(), e);
        }

    }

    @Test
    public void testSubmitFirstTime() {
        when(environment.getProperty(INVALID_ERROR_PROPERTY)).thenReturn(INVALID_ERROR);
        when(environment.getProperty(SCHEMA_FILE_PROPERTY)).thenReturn(SCHEMA_FILE);
        when(environment.getProperty("api-version-number")).thenReturn("1");
        when(environment.getProperty("api-error-messages.created")).thenReturn("Successfully created");

        try {
            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/FirstSubmitAnswer.json");
            PcqAnswerRequest pcqAnswerRequest = jsonObjectFromString(jsonStringRequest);
            ResponseEntity<Object> responseEntity = submitAnswersService.processPcqAnswers(getTestHeader(),
                                                                                           pcqAnswerRequest);

            assertNotNull(responseEntity, RESPONSE_NULL_MSG);

            Object responseMap = responseEntity.getBody();
            assertNotNull(responseMap, RESPONSE_BODY_NULL_MSG);
            assertEquals(responseEntity.getStatusCodeValue(), 201, "Expected 201 status code");


        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage(), e);
        }

    }

    @Test
    public void testSubmitSecondTime() {
        when(environment.getProperty(INVALID_ERROR_PROPERTY)).thenReturn(INVALID_ERROR);
        when(environment.getProperty(SCHEMA_FILE_PROPERTY)).thenReturn(SCHEMA_FILE);
        when(environment.getProperty("api-version-number")).thenReturn("1");
        when(environment.getProperty("api-error-messages.created")).thenReturn("Successfully created");

        try {
            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/DobSubmitAnswer.json");
            PcqAnswerRequest pcqAnswerRequest = jsonObjectFromString(jsonStringRequest);
            ResponseEntity<Object> responseEntity = submitAnswersService.processPcqAnswers(getTestHeader(),
                                                                                           pcqAnswerRequest);

            assertNotNull(responseEntity, RESPONSE_NULL_MSG);

            Object responseMap = responseEntity.getBody();
            assertNotNull(responseMap, RESPONSE_BODY_NULL_MSG);
            assertEquals(responseEntity.getStatusCodeValue(), 201, "Expected 201 status code");


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

    /**
     * Converts an Java Object to JSon String.
     * @param obj - The Java object.
     * @return - JSON String representation of the Java object.
     */
    public static String asJsonString(final Object obj) throws JsonProcessingException {
        return  new ObjectMapper().writeValueAsString(obj);
    }

}

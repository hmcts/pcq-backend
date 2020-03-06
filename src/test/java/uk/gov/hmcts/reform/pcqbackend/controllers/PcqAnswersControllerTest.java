package uk.gov.hmcts.reform.pcqbackend.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.system.OutputCaptureRule;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.hmcts.reform.pcqbackend.model.PcqAnswerRequest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import javax.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(PcqAnswersController.class)
public class PcqAnswersControllerTest {

    Logger logger = LoggerFactory.getLogger(PcqAnswersControllerTest.class);

    @Autowired
    private transient MockMvc mvc;

    @Autowired
    private Environment environment;

    @Rule
    public OutputCaptureRule capture = new OutputCaptureRule();

    private static String submitAnswerApiUrl;

    private static String headerKey;

    private static String apiErrorMessageCreated;

    private static String apiErrorMessageBadRequest;

    private static final String JSON_PATH_PCQID = "$.pcqId";

    private static final String JSON_PATH_RESPONSE_STATUS = "$.responseStatus";

    private static final String JSON_PATH_RESPONSE_STATUS_CODE = "$.responseStatusCode";

    private static final String ERROR_MSG_PREFIX = "Test failed because of exception during execution. Message is ";

    private static final String CO_RELATION_ID_FOR_TEST = "Test-Id";

    @Before
    public void setupEnvironmentVariables() {
        submitAnswerApiUrl = environment.getProperty("unit-test.api-urls.submit_answer");
        headerKey = environment.getProperty("api-required-header-keys.co-relationid");
        apiErrorMessageCreated = environment.getProperty("api-error-messages.created");
        apiErrorMessageBadRequest = environment.getProperty("api-error-messages.bad_request");
    }

    /**
     * This method tests the submitAnswers API when it is called with all valid parameters and that the answers record
     * is successfully added to the database. The response status code will be 201.
     */
    @DisplayName("Should submit the answers successfully and return with 201 response code")
    @Test
    public void testSubmitAnswersFirstTime()  {
        HttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        //Mock DAO when ready.

        try {
            int pcqId = 1234;
            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/FirstSubmitAnswer.json");
            //logger.info("testSubmitAnswersFirstTime - Generated Json String is " + jsonStringRequest);
            mvc.perform(MockMvcRequestBuilders
                                .post(submitAnswerApiUrl)
                                .header(headerKey, CO_RELATION_ID_FOR_TEST)
                                .content(jsonStringRequest)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                                .andExpect(status().isCreated())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                                .andExpect(jsonPath(JSON_PATH_PCQID).value(pcqId))
                                .andExpect(jsonPath(JSON_PATH_RESPONSE_STATUS).value(apiErrorMessageCreated))
                                .andExpect(jsonPath(JSON_PATH_RESPONSE_STATUS_CODE).value("201"));

            checkLogsForKeywords();


        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage());
        }

    }

    /**
     * This method tests the submitAnswers API when it is called with all valid parameters and that the answers record
     * is successfully updated in the database. The response status code will be 201.
     */
    @DisplayName("Should update the answers successfully and return with 201 response code")
    @Test
    public void testSubmitAnswersSecondTime()  {
        HttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        //Mock DAO when ready.

        try {
            int pcqId = 1234;
            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/DobSubmitAnswer.json");
            //logger.info("testSubmitAnswersFirstTime - Generated Json String is " + jsonStringRequest);
            mvc.perform(MockMvcRequestBuilders
                            .post(submitAnswerApiUrl)
                            .header(headerKey, CO_RELATION_ID_FOR_TEST)
                            .content(jsonStringRequest)
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath(JSON_PATH_PCQID).value(pcqId))
                .andExpect(jsonPath(JSON_PATH_RESPONSE_STATUS).value(apiErrorMessageCreated))
                .andExpect(jsonPath(JSON_PATH_RESPONSE_STATUS_CODE).value("201"));

            checkLogsForKeywords();

        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage());
        }

    }

    /**
     * This method tests the submitAnswers API when it is called with incorrect version number.
     * The response status code will be 403.
     */
    @DisplayName("Should validate the version number and return with 403 response code")
    @Test
    public void testInvalidVersionNumber()  {
        HttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        //Mock DAO when ready.

        try {
            int pcqId = 1234;
            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/InvalidVersion.json");
            //logger.info("testSubmitAnswersFirstTime - Generated Json String is " + jsonStringRequest);
            mvc.perform(MockMvcRequestBuilders
                            .post(submitAnswerApiUrl)
                            .header(headerKey, CO_RELATION_ID_FOR_TEST)
                            .content(jsonStringRequest)
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath(JSON_PATH_PCQID).value(pcqId))
                .andExpect(jsonPath(JSON_PATH_RESPONSE_STATUS).value(apiErrorMessageBadRequest))
                .andExpect(jsonPath(JSON_PATH_RESPONSE_STATUS_CODE).value("403"));

            checkLogsForKeywords();

        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage());
        }

    }

    /**
     * This method tests the submitAnswers API when it is called with all valid parameters but the
     * header does not contain the required attribute. The response status code will be 400.
     */
    @DisplayName("Should return with an Invalid Request error code 400")
    @Test
    public void testInvalidRequestForMissingHeader()  {
        HttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        //Mock DAO when ready.

        try {
            int pcqId = 123;
            String jsonStringRequest = asJsonString(new PcqAnswerRequest(pcqId));
            //logger.info("testInvalidRequestForMissingHeader - Generated Json String is " + jsonStringRequest);
            mvc.perform(MockMvcRequestBuilders
                            .post(submitAnswerApiUrl)
                            .content(jsonStringRequest)
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath(JSON_PATH_PCQID).value(pcqId))
                .andExpect(jsonPath(JSON_PATH_RESPONSE_STATUS).value(apiErrorMessageBadRequest))
                .andExpect(jsonPath(JSON_PATH_RESPONSE_STATUS_CODE).value("400"));


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
        HttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        //Mock DAO when ready.

        try {
            int pcqId = 1234;
            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/InvalidJson1.json");
            //logger.info("testInvalidRequestForInvalidJson - Generated Json String is " + jsonStringRequest);
            mvc.perform(MockMvcRequestBuilders
                            .post(submitAnswerApiUrl)
                            .header(headerKey, CO_RELATION_ID_FOR_TEST)
                            .content(jsonStringRequest)
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath(JSON_PATH_PCQID).value(pcqId))
                .andExpect(jsonPath(JSON_PATH_RESPONSE_STATUS).value(apiErrorMessageBadRequest))
                .andExpect(jsonPath(JSON_PATH_RESPONSE_STATUS_CODE).value("400"));

            checkLogsForKeywords();

        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage());
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

    private void checkLogsForKeywords() {
        assertTrue(capture.getAll().contains("Co-Relation Id : " + CO_RELATION_ID_FOR_TEST),
                   "Co-Relation Id was not logged in log files.");
    }

}

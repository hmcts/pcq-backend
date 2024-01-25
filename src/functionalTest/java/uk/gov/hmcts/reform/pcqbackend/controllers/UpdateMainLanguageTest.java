package uk.gov.hmcts.reform.pcqbackend.controllers;

import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pcq.commons.model.PcqAnswerRequest;

import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.pcq.commons.tests.utils.TestUtils.jsonObjectFromString;
import static uk.gov.hmcts.reform.pcq.commons.tests.utils.TestUtils.jsonStringFromFile;

@RunWith(SpringIntegrationSerenityRunner.class)
@WithTags({@WithTag("testType:Functional")})
@ActiveProfiles("functional")
@Slf4j
@SuppressWarnings({"PMD.JUnitTestsShouldIncludeAssert"})
public class UpdateMainLanguageTest extends PcqBaseFunctionalTest {
    public static final String RESPONSE_KEY_2 = "responseStatusCode";
    public static final String RESPONSE_KEY_3 = "responseStatus";
    public static final String HTTP_CREATED = "201";
    public static final String RESPONSE_CREATED_MSG = "Successfully created";
    public static final String RESPONSE_STATUS_CODE_NOT_VALID = "Response Status Code not valid";
    public static final String RESPONSE_STATUS_NOT_VALID = "Response Status not valid";

    @Test
    public void updateMainLanguageEnglish() {
        String fileName = "JsonTestFiles/UpdateMainLanguageEnglish.json";
        updateRecord(fileName);
    }

    @Test
    public void updateMainLanguageWelsh() {
        String fileName = "JsonTestFiles/UpdateMainLanguageWelsh.json";
        updateRecord(fileName);
    }

    //Test English or welsh option as well for backward compatibility
    @Test
    public void updateMainLanguageEnglishOrWelsh() {
        String fileName = "JsonTestFiles/UpdateMainLanguageEnglishOrWelsh.json";
        updateRecord(fileName);
    }

    public void updateRecord(String fileName) {
        try {

            //Create a record before updating.
            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/FirstSubmitAnswer.json");
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);
            answerRequest.setPcqId(generateUuid());
            Map<String, Object> response = pcqBackEndServiceClient.createAnswersRecord(answerRequest);

            assertEquals(RESPONSE_STATUS_CODE_NOT_VALID, HTTP_CREATED, response.get(RESPONSE_KEY_2));
            assertEquals(RESPONSE_STATUS_NOT_VALID, RESPONSE_CREATED_MSG,
                         response.get(RESPONSE_KEY_3));

            //Update the record
            jsonStringRequest = jsonStringFromFile(fileName);
            PcqAnswerRequest updateAnswerRequest = jsonObjectFromString(jsonStringRequest);

            //Use the same PCQ ID as above
            updateAnswerRequest.setPcqId(answerRequest.getPcqId());

            response = pcqBackEndServiceClient.createAnswersRecord(updateAnswerRequest);

            assertEquals(RESPONSE_STATUS_CODE_NOT_VALID, HTTP_CREATED, response.get(RESPONSE_KEY_2));
            assertEquals(RESPONSE_STATUS_NOT_VALID, RESPONSE_CREATED_MSG,
                         response.get(RESPONSE_KEY_3));

            //Prepare for clearing down.
            clearTestPcqAnswers.add(answerRequest);

            //Get the record
            Map<String, Object> validateGetResponse = pcqBackEndServiceClient.getAnswersRecord(
                updateAnswerRequest.getPcqId(), HttpStatus.OK);

            checkAssertionsOnResponse(validateGetResponse, updateAnswerRequest);

        } catch (IOException e) {
            log.error("Error during test execution", e);
        }

    }
}

package uk.gov.hmcts.reform.pcqbackend.controllers;

import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.serenitybdd.annotations.WithTag;
import net.serenitybdd.annotations.WithTags;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pcq.commons.model.PcqAnswerRequest;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.pcq.commons.tests.utils.TestUtils.jsonObjectFromString;
import static uk.gov.hmcts.reform.pcq.commons.tests.utils.TestUtils.jsonStringFromFile;

@RunWith(SpringIntegrationSerenityRunner.class)
@WithTags({@WithTag("testType:Functional")})
@ActiveProfiles("functional")
@Slf4j
public class UpdateMainLanguageTest extends PcqBaseFunctionalTest {
    public static final String RESPONSE_KEY_2 = "responseStatusCode";
    public static final String RESPONSE_KEY_3 = "responseStatus";
    public static final String HTTP_CREATED = "201";
    public static final String RESPONSE_CREATED_MSG = "Successfully created";
    public static final String RESPONSE_STATUS_CODE_NOT_VALID = "Response Status Code not valid";
    public static final String RESPONSE_STATUS_NOT_VALID = "Response Status not valid";

    @Test
    public void updateMainLanguageEnglish() throws IOException {
        String fileName = "JsonTestFiles/UpdateMainLanguageEnglish.json";
        assertRecordUpdated(fileName);
    }

    @Test
    public void updateMainLanguageWelsh() throws IOException {
        String fileName = "JsonTestFiles/UpdateMainLanguageWelsh.json";
        assertRecordUpdated(fileName);
    }

    //Test English or welsh option as well for backward compatibility
    @Test
    public void updateMainLanguageEnglishOrWelsh() throws IOException {
        String fileName = "JsonTestFiles/UpdateMainLanguageEnglishOrWelsh.json";
        assertRecordUpdated(fileName);
    }

    public void assertRecordUpdated(String fileName) throws IOException {
        //Create a record before updating.
        String jsonStringRequest = jsonStringFromFile("JsonTestFiles/FirstSubmitAnswer.json");
        PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);
        answerRequest.setPcqId(generateUuid());
        Map<String, Object> response = pcqBackEndServiceClient.createAnswersRecord(answerRequest);

        assertEquals(HTTP_CREATED, response.get(RESPONSE_KEY_2), RESPONSE_STATUS_CODE_NOT_VALID);
        assertEquals(RESPONSE_CREATED_MSG, response.get(RESPONSE_KEY_3), RESPONSE_STATUS_NOT_VALID);

        //Update the record
        jsonStringRequest = jsonStringFromFile(fileName);
        PcqAnswerRequest updateAnswerRequest = jsonObjectFromString(jsonStringRequest);

        //Use the same PCQ ID as above
        updateAnswerRequest.setPcqId(answerRequest.getPcqId());

        response = pcqBackEndServiceClient.createAnswersRecord(updateAnswerRequest);

        assertEquals(HTTP_CREATED, response.get(RESPONSE_KEY_2), RESPONSE_STATUS_CODE_NOT_VALID);
        assertEquals(RESPONSE_CREATED_MSG, response.get(RESPONSE_KEY_3), RESPONSE_STATUS_NOT_VALID);

        //Prepare for clearing down.
        clearTestPcqAnswers.add(answerRequest);

        //Get the record
        Map<String, Object> validateGetResponse = pcqBackEndServiceClient.getAnswersRecord(
            updateAnswerRequest.getPcqId(), HttpStatus.OK);

        checkAssertionsOnResponse(validateGetResponse, updateAnswerRequest);
    }
}

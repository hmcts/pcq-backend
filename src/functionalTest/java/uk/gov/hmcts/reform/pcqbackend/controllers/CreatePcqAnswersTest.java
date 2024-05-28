package uk.gov.hmcts.reform.pcqbackend.controllers;

import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.annotations.WithTag;
import net.serenitybdd.annotations.WithTags;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
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
public class CreatePcqAnswersTest extends PcqBaseFunctionalTest {

    public static final String RESPONSE_KEY_1 = "pcqId";

    public static final String RESPONSE_KEY_2 = "responseStatusCode";

    public static final String RESPONSE_KEY_3 = "responseStatus";

    public static final String HTTP_CREATED = "201";

    public static final String RESPONSE_CREATED_MSG = "Successfully created";

    @Test
    public void createPcqAnswersWithoutCaseId() throws IOException {
        String jsonStringRequest = jsonStringFromFile("JsonTestFiles/FirstSubmitAnswer.json");
        PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);
        answerRequest.setPcqId(generateUuid());
        Map<String, Object> response = pcqBackEndServiceClient.createAnswersRecord(answerRequest);

        assertEquals(HTTP_CREATED, response.get(RESPONSE_KEY_2), "Response Status Code not valid");
        assertEquals(RESPONSE_CREATED_MSG, response.get(RESPONSE_KEY_3), "Response Status not valid");

        //Prepare for clearing down.
        clearTestPcqAnswers.add(answerRequest);

        Map<String, Object> validateGetResponse = pcqBackEndServiceClient.getAnswersRecord(
            answerRequest.getPcqId(), HttpStatus.OK);

        checkAssertionsOnResponse(validateGetResponse, answerRequest);
    }

    @Test
    public void createPcqAnswersWithCaseId() throws IOException {
        String jsonStringRequest = jsonStringFromFile("JsonTestFiles/FirstSubmitAnswerWithCase.json");
        PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);
        answerRequest.setPcqId(generateUuid());
        Map<String, Object> response = pcqBackEndServiceClient.createAnswersRecord(answerRequest);

        assertEquals(HTTP_CREATED, response.get(RESPONSE_KEY_2), "Response Status Code not valid");
        assertEquals(RESPONSE_CREATED_MSG, response.get(RESPONSE_KEY_3), "Response Status not valid");

        //Prepare for clearing down.
        clearTestPcqAnswers.add(answerRequest);

        Map<String, Object> validateGetResponse = pcqBackEndServiceClient.getAnswersRecord(
            answerRequest.getPcqId(), HttpStatus.OK);

        checkAssertionsOnResponse(validateGetResponse, answerRequest);
    }
}

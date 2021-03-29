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
@ActiveProfiles("functional")
@Slf4j
@WithTags({@WithTag("testType:Functional")})
public class PaperChannelPcqAnswersTest extends PcqBaseFunctionalTest {

    public static final String RESPONSE_KEY_2 = "responseStatusCode";

    public static final String RESPONSE_KEY_3 = "responseStatus";

    public static final String HTTP_CREATED = "201";

    public static final String RESPONSE_CREATED_MSG = "Successfully created";

    @Test
    public void createPcqAnswersWithoutCaseId() {

        try {

            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/FirstSubmitDcnAnswer.json");
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);
            answerRequest.setPcqId(generateUuid());
            answerRequest.setDcnNumber("DCN_" + generateUuid());
            Map<String, Object> response = pcqBackEndServiceClient.createAnswersRecord(answerRequest);

            assertEquals("Response Status Code not valid", HTTP_CREATED, response.get(RESPONSE_KEY_2));
            assertEquals("Response Status not valid", RESPONSE_CREATED_MSG,
                         response.get(RESPONSE_KEY_3));

            //Prepare for clearing down.
            clearTestPcqAnswers.add(answerRequest);

            Map<String, Object> validateGetResponse = pcqBackEndServiceClient.getAnswersRecord(
                answerRequest.getPcqId(), HttpStatus.OK);

            checkAssertionsOnResponse(validateGetResponse, answerRequest);


        } catch (IOException e) {
            log.error("Error during test execution", e);
        }

    }
}

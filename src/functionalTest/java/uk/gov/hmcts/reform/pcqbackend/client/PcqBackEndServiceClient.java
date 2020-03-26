package uk.gov.hmcts.reform.pcqbackend.client;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.rest.SerenityRest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.pcqbackend.model.PcqAnswerRequest;

import java.util.Map;

import static net.serenitybdd.rest.SerenityRest.given;
import static net.serenitybdd.rest.SerenityRest.with;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Slf4j
@SuppressWarnings("unchecked")
public class PcqBackEndServiceClient {

    private static final String AUTHORIZATION_HEADER = "X-Correlation-Id";
    private static final String SUBMIT_ANSWERS_URL = "/pcq/backend/submitAnswers";
    private static final String INFO_MSG_CONSTANT_1 = "Update answers record response: ";

    private final String pcqBackEndApiUrl;

    public PcqBackEndServiceClient(String pcqBackEndApiUrl) {
        this.pcqBackEndApiUrl = pcqBackEndApiUrl;
    }

    @SuppressWarnings("PMD.LawOfDemeter")
    public String getWelcomePage() {
        return SerenityRest
            .get(pcqBackEndApiUrl)
            .then()
            .statusCode(HttpStatus.OK.value())
            .and()
            .extract()
            .body()
            .asString();
    }

    @SuppressWarnings("PMD.LawOfDemeter")
    public String getHealthPage() {
        return SerenityRest
            .get(pcqBackEndApiUrl + "/health")
            .then()
            .statusCode(HttpStatus.OK.value())
            .and()
            .extract()
            .body()
            .asString();
    }


    public Map<String, Object> createAnswersRecord(PcqAnswerRequest answerRequest) {
        Response response = getMultipleAuthHeaders()
            .body(answerRequest)
            .post(SUBMIT_ANSWERS_URL)
            .andReturn();

        if (response.statusCode() != CREATED.value()) {
            log.info("Create answers record response: " + response.asString());
        }

        response.then()
            .assertThat()
            .statusCode(CREATED.value());

        return response.body().as(Map.class);
    }

    public Map<String, Object> getAnswersRecord(String pcqId, HttpStatus status) {

        Response response = withUnauthenticatedRequest()
            .get("/pcq/backend/getAnswer/" + pcqId)
            .andReturn();
        response.then()
            .assertThat()
            .statusCode(status.value());

        if (status == HttpStatus.UNAUTHORIZED) {
            return null;
        }
        return response.body().as(Map.class);
    }

    public Map<String, Object> updateAnswersRecord(PcqAnswerRequest answerRequest) {
        Response response = getMultipleAuthHeaders()
            .body(answerRequest)
            .post(SUBMIT_ANSWERS_URL)
            .andReturn();

        if (response.statusCode() != CREATED.value()) {
            log.info(INFO_MSG_CONSTANT_1 + response.asString());
        }

        response.then()
            .assertThat()
            .statusCode(CREATED.value());

        return response.body().as(Map.class);
    }

    public Map<String, Object> staleAnswersNotRecorded(PcqAnswerRequest answerRequest) {
        Response response = getMultipleAuthHeaders()
            .body(answerRequest)
            .post(SUBMIT_ANSWERS_URL)
            .andReturn();

        if (response.statusCode() != ACCEPTED.value()) {
            log.info(INFO_MSG_CONSTANT_1 + response.asString());
        }

        response.then()
            .assertThat()
            .statusCode(ACCEPTED.value());

        return response.body().as(Map.class);
    }

    public Map<String, Object> invalidJSonRecord(PcqAnswerRequest answerRequest) {
        Response response = getMultipleAuthHeaders()
            .body(answerRequest)
            .post(SUBMIT_ANSWERS_URL)
            .andReturn();

        if (response.statusCode() != BAD_REQUEST.value()) {
            log.info(INFO_MSG_CONSTANT_1 + response.asString());
        }

        response.then()
            .assertThat()
            .statusCode(BAD_REQUEST.value());

        return response.body().as(Map.class);
    }

    public Map<String, Object> invalidVersion(PcqAnswerRequest answerRequest) {
        Response response = getMultipleAuthHeaders()
            .body(answerRequest)
            .post(SUBMIT_ANSWERS_URL)
            .andReturn();

        if (response.statusCode() != FORBIDDEN.value()) {
            log.info(INFO_MSG_CONSTANT_1 + response.asString());
        }

        response.then()
            .assertThat()
            .statusCode(FORBIDDEN.value());

        return response.body().as(Map.class);
    }

    public Map<String, Object> unRecoverableError(PcqAnswerRequest answerRequest) {
        Response response = getMultipleAuthHeaders()
            .body(answerRequest)
            .post(SUBMIT_ANSWERS_URL)
            .andReturn();

        if (response.statusCode() != INTERNAL_SERVER_ERROR.value()) {
            log.info(INFO_MSG_CONSTANT_1 + response.asString());
        }

        response.then()
            .assertThat()
            .statusCode(INTERNAL_SERVER_ERROR.value());

        return response.body().as(Map.class);
    }


    private RequestSpecification withUnauthenticatedRequest() {
        return given()
            .relaxedHTTPSValidation()
            .baseUri(pcqBackEndApiUrl)
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .header("Accepts", MediaType.APPLICATION_JSON_VALUE);
    }

    public RequestSpecification getMultipleAuthHeaders() {
        return with()
            .relaxedHTTPSValidation()
            .baseUri(pcqBackEndApiUrl)
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .header("Accepts", MediaType.APPLICATION_JSON_VALUE)
            .header(AUTHORIZATION_HEADER, "FUNC-TEST-PCQ");
    }

}

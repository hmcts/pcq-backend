package uk.gov.hmcts.reform.pcqbackend.client;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.rest.SerenityRest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.pcq.commons.model.PcqAnswerRequest;
import uk.gov.hmcts.reform.pcq.commons.model.PcqRecordWithoutCaseResponse;
import uk.gov.hmcts.reform.pcq.commons.utils.PcqUtils;

import java.util.Map;

import static net.serenitybdd.rest.SerenityRest.given;
import static net.serenitybdd.rest.SerenityRest.with;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Slf4j
@SuppressWarnings({"unchecked", "PMD.AvoidDuplicateLiterals"})
public class PcqBackEndServiceClient {

    private static final String CO_RELATION_HEADER = "X-Correlation-Id";
    private static final String SUBMIT_ANSWERS_URL = "/pcq/backend/submitAnswers";
    private static final String INFO_MSG_CONSTANT_1 = "Update answers record response: ";
    private static final String SUBJECT = "TEST";
    private static final String TEST_AUTHORITIES = "TEST_AUTHORITY";

    private final String pcqBackEndApiUrl;
    private final String jwtSecretKey;

    public PcqBackEndServiceClient(String pcqBackEndApiUrl, String jwtSecretKey) {

        this.pcqBackEndApiUrl = pcqBackEndApiUrl;
        this.jwtSecretKey = jwtSecretKey;
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
        Response response = getMultipleAuthHeaders(jwtSecretKey)
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

    public Map<String, Object> updateAnswersRecord(PcqAnswerRequest answerRequest, HttpStatus status) {
        Response response = getMultipleAuthHeaders(jwtSecretKey)
            .body(answerRequest)
            .post(SUBMIT_ANSWERS_URL)
            .andReturn();

        if (response.statusCode() != CREATED.value()) {
            log.info(INFO_MSG_CONSTANT_1 + response.asString());
        }

        response.then()
            .assertThat()
            .statusCode(status.value());

        return response.body().as(Map.class);
    }

    public Map<String, Object> deleteAnswersRecord(String pcqId, HttpStatus status) {
        Response response = getMultipleAuthHeaders(jwtSecretKey)
            .delete("/pcq/backend/deletePcqRecord/" + pcqId)
            .andReturn();
        response.then()
            .assertThat()
            .statusCode(status.value());

        if (status == HttpStatus.UNAUTHORIZED) {
            return null;
        }
        return response.body().as(Map.class);
    }

    public Map<String, Object> staleAnswersNotRecorded(PcqAnswerRequest answerRequest) {
        Response response = getMultipleAuthHeaders(jwtSecretKey)
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
        Response response = getMultipleAuthHeaders(jwtSecretKey)
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
        Response response = getMultipleAuthHeaders(jwtSecretKey)
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
        Response response = getMultipleAuthHeaders(jwtSecretKey)
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

    public Map<String, Object> getAnswerRecordWithoutCase(HttpStatus status) {

        Response response = getCoRelationHeaders()
            .get("pcq/backend/consolidation/pcqWithoutCase")
            .andReturn();
        response.then()
            .assertThat()
            .statusCode(status.value());

        if (status == HttpStatus.UNAUTHORIZED || status == INTERNAL_SERVER_ERROR) {
            return null;
        }
        return response.body().as(Map.class);
    }

    public PcqRecordWithoutCaseResponse getAnswerRecordsWithoutCase(HttpStatus status) {

        Response response = getCoRelationHeaders()
            .get("pcq/backend/consolidation/pcqRecordWithoutCase")
            .andReturn();
        response.then()
            .assertThat()
            .statusCode(status.value());

        if (status == HttpStatus.UNAUTHORIZED || status == INTERNAL_SERVER_ERROR) {
            return null;
        }
        return response.body().as(PcqRecordWithoutCaseResponse.class);
    }

    public Map<String, Object> addCaseForPcq(String pcqId, String caseId, HttpStatus status) {
        Response response = getMultipleAuthHeadersWithQueryParams("caseId", caseId)
            .put("pcq/backend/consolidation/addCaseForPCQ/" + pcqId)
            .andReturn();
        response.then()
            .assertThat()
            .statusCode(status.value());

        if (status == HttpStatus.UNAUTHORIZED) {
            return null;
        }
        return response.body().as(Map.class);
    }


    private RequestSpecification withUnauthenticatedRequest() {
        return given()
            .relaxedHTTPSValidation()
            .baseUri(pcqBackEndApiUrl)
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .header("Accepts", MediaType.APPLICATION_JSON_VALUE);
    }

    public RequestSpecification getMultipleAuthHeaders(String secretKey) {
        return with()
            .relaxedHTTPSValidation()
            .baseUri(pcqBackEndApiUrl)
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .header("Accepts", MediaType.APPLICATION_JSON_VALUE)
            .header(CO_RELATION_HEADER, "FUNC-TEST-PCQ")
            .header("Authorization", "Bearer "
                + PcqUtils.generateAuthorizationToken(secretKey, SUBJECT, TEST_AUTHORITIES));
    }

    public RequestSpecification getCoRelationHeaders() {
        return with()
            .relaxedHTTPSValidation()
            .baseUri(pcqBackEndApiUrl)
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .header("Accepts", MediaType.APPLICATION_JSON_VALUE)
            .header(CO_RELATION_HEADER, "FUNC-TEST-PCQ");
    }

    public RequestSpecification getMultipleAuthHeadersWithQueryParams(String paramName, String paramValue) {
        return with()
            .relaxedHTTPSValidation()
            .baseUri(pcqBackEndApiUrl)
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .header("Accepts", MediaType.APPLICATION_JSON_VALUE)
            .header(CO_RELATION_HEADER, "FUNC-TEST-PCQ")
            .queryParam(paramName, paramValue);
    }

}

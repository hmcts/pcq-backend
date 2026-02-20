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

import java.util.HashMap;
import java.util.Map;

import static net.serenitybdd.rest.SerenityRest.given;
import static net.serenitybdd.rest.SerenityRest.with;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Slf4j
@SuppressWarnings({"unchecked", "PMD.AvoidDuplicateLiterals", "PMD.TooManyMethods"})
public class PcqBackEndServiceClient {

    private static final String CO_RELATION_HEADER = "X-Correlation-Id";
    private static final String SERVICE_AUTHORISATION_HEADER = "ServiceAuthorization";
    private static final String SUBMIT_ANSWERS_URL = "/pcq/backend/submitAnswers";
    private static final String INFO_MSG_CONSTANT_1 = "Update answers record response: ";
    private static final String SUBJECT = "TEST";
    private static final String TEST_AUTHORITIES = "TEST_AUTHORITY";

    private final String pcqBackEndApiUrl;
    private final String jwtSecretKey;
    private final String s2sName;
    private final String s2sSecret;
    private final String s2sUrl;
    private final IdamServiceClient idamServiceClient = new IdamServiceClient();

    public PcqBackEndServiceClient(String pcqBackEndApiUrl, String jwtSecretKey,
                                   String s2sName, String s2sSecret, String s2sUrl) {

        this.pcqBackEndApiUrl = pcqBackEndApiUrl;
        this.jwtSecretKey = jwtSecretKey;
        this.s2sName = s2sName;
        this.s2sSecret = s2sSecret;
        this.s2sUrl = s2sUrl;
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
            return new HashMap<String, Object>();
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
            return new HashMap<String, Object>();
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

    public PcqRecordWithoutCaseResponse getAnswerRecordsWithoutCase(HttpStatus status) {

        Response response = getCoRelationAndServiceAuthHeaders()
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
        Response response = getCoRelationAndServiceAuthHeadersWithQueryParams("caseId", caseId)
            .put("pcq/backend/consolidation/addCaseForPCQ/" + pcqId)
            .andReturn();
        response.then()
            .assertThat()
            .statusCode(status.value());

        if (status == HttpStatus.UNAUTHORIZED) {
            return new HashMap<String, Object>();
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

    public RequestSpecification getCoRelationAndServiceAuthHeaders() {
        return with()
            .relaxedHTTPSValidation()
            .baseUri(pcqBackEndApiUrl)
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .header("Accepts", MediaType.APPLICATION_JSON_VALUE)
            .header(CO_RELATION_HEADER, "FUNC-TEST-PCQ")
            .header(SERVICE_AUTHORISATION_HEADER, getServiceAuthorisationValue());
    }

    public RequestSpecification getCoRelationAndServiceAuthHeadersWithQueryParams(String paramName,
                                                                                   String paramValue) {
        return with()
            .relaxedHTTPSValidation()
            .baseUri(pcqBackEndApiUrl)
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .header("Accepts", MediaType.APPLICATION_JSON_VALUE)
            .header(CO_RELATION_HEADER, "FUNC-TEST-PCQ")
            .header(SERVICE_AUTHORISATION_HEADER, getServiceAuthorisationValue())
            .queryParam(paramName, paramValue);
    }

    private String getServiceAuthorisationValue() {
        String s2sToken = idamServiceClient.s2sSignIn(s2sName, s2sSecret, s2sUrl);
        return "Bearer " + s2sToken;
    }

}

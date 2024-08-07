package uk.gov.hmcts.reform.pcqbackend.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.reform.pcq.commons.model.PcqAnswerRequest;
import uk.gov.hmcts.reform.pcq.commons.model.PcqRecordWithoutCaseResponse;
import uk.gov.hmcts.reform.pcq.commons.utils.PcqUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@SuppressWarnings({"PMD.AvoidDuplicateLiterals", "PMD.TooManyMethods"})
public class PcqBackEndClient {

    private static final String APP_BASE_PATH = "/pcq/backend";
    private static final String SUBJECT = "TEST";
    private static final String TEST_AUTHORITIES = "TEST_AUTHORITY";
    private static final String SECRET_KEY = "a-very-long-and-boring-secret-key";

    private final int prdApiPort;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();
    private final String baseUrl;

    public PcqBackEndClient(int prdApiPort) {
        this.prdApiPort = prdApiPort;
        this.baseUrl = "http://localhost:" + prdApiPort + APP_BASE_PATH;
    }

    public Map<String, Object> createPcqAnswer(PcqAnswerRequest request) {
        return postRequest(baseUrl + "/submitAnswers", request);
    }

    public Map<String, Object> checkAuthValidation(PcqAnswerRequest request) {
        return postRequestNoAuthHeader(baseUrl + "/submitAnswers", request);
    }

    public Map<String, Object> findAnswerByPcq(String pcqId) {
        return getRequest(APP_BASE_PATH + "/getAnswer/{pcqId}", pcqId);
    }

    public Map<String, Object> deletePcqRecord(String pcqId) {
        return deleteRequest(APP_BASE_PATH + "/deletePcqRecord/{pcqId}", pcqId);
    }

    public Map<String, Object> getPcqRecordWithoutCase() {
        return getPcqRecordRequestObject(APP_BASE_PATH + "/consolidation/pcqRecordWithoutCase");
    }

    public Map<String, Object> addCaseForPcq(String pcqId, String caseId) {
        return putRequest(APP_BASE_PATH + "/consolidation/addCaseForPCQ/" + pcqId, caseId);
    }

    public Map<String, Object> getPcqBlobStorageSasToken(String serviceName) {
        return getRequestForSasToken(APP_BASE_PATH + "/token/" + serviceName);
    }

    @SuppressWarnings({"rawtypes", "PMD.DataflowAnomalyAnalysis"})
    private <T> Map<String, Object> postRequest(String uriPath, T requestBody) {

        HttpEntity<T> request = new HttpEntity<>(requestBody, getS2sTokenHeaders());
        ResponseEntity<Map> responseEntity;

        try {
            responseEntity = restTemplate.postForEntity(
                uriPath,
                request,
                Map.class);

        } catch (RestClientResponseException ex) {
            HashMap<String, Object> statusAndBody = new HashMap<>(2);
            statusAndBody.put("http_status", String.valueOf(ex.getRawStatusCode()));
            statusAndBody.put("response_body", ex.getResponseBodyAsString());
            return getResponse(statusAndBody);
        }

        return getResponse(responseEntity);
    }

    @SuppressWarnings({"rawtypes", "PMD.DataflowAnomalyAnalysis"})
    private <T> Map<String, Object> postRequestNoAuthHeader(String uriPath, T requestBody) {

        HttpEntity<T> request = new HttpEntity<>(requestBody, getCoRelationTokenHeaders());
        ResponseEntity<Map> responseEntity;

        try {

            responseEntity = restTemplate.postForEntity(
                uriPath,
                request,
                Map.class);

        } catch (RestClientResponseException ex) {
            HashMap<String, Object> statusAndBody = new HashMap<>(2);
            statusAndBody.put("http_status", String.valueOf(ex.getRawStatusCode()));
            statusAndBody.put("response_body", ex.getResponseBodyAsString());
            return getResponse(statusAndBody);
        }

        return getResponse(responseEntity);
    }

    @SuppressWarnings({"rawtypes", "PMD.DataflowAnomalyAnalysis"})
    private <T> Map<String, Object> putRequest(String uriPath, Object... params) {

        HttpEntity<T> request = new HttpEntity<>(getCoRelationTokenHeaders());
        ResponseEntity<Map> responseEntity;
        //adding the query params to the URL
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl("http://localhost:" + prdApiPort + uriPath)
            .queryParam("caseId", params[0]);
        try {

            responseEntity = restTemplate.exchange(uriBuilder.toUriString(),
                                                     HttpMethod.PUT,
                                                     request,
                                                     Map.class);

        } catch (RestClientResponseException ex) {
            HashMap<String, Object> statusAndBody = new HashMap<>(2);
            statusAndBody.put("http_status", String.valueOf(ex.getRawStatusCode()));
            statusAndBody.put("response_body", ex.getResponseBodyAsString());
            return getResponse(statusAndBody);
        }

        return getResponse(responseEntity);
    }

    @SuppressWarnings({"rawtypes", "PMD.DataflowAnomalyAnalysis"})
    private Map<String, Object> getRequest(String uriPath, Object... params) {

        ResponseEntity<Map> responseEntity;

        try {
            HttpEntity<?> request = new HttpEntity<>(getCoRelationTokenHeaders());
            responseEntity = restTemplate
                .exchange("http://localhost:" + prdApiPort + uriPath,
                          HttpMethod.GET,
                          request,
                          Map.class,
                          params);
        } catch (HttpStatusCodeException ex) {
            HashMap<String, Object> statusAndBody = new HashMap<>(2);
            statusAndBody.put("http_status", String.valueOf(ex.getRawStatusCode()));
            statusAndBody.put("response_body", ex.getResponseBodyAsString());
            return statusAndBody;
        }

        return getResponse(responseEntity);
    }

    @SuppressWarnings({"rawtypes", "PMD.DataflowAnomalyAnalysis"})
    private Map<String, Object> getRequestForSasToken(String uriPath, Object... params) {

        ResponseEntity<Map> responseEntity;

        try {
            HttpEntity<?> request = new HttpEntity<>(getServiceAuthorisationHeader());
            responseEntity = restTemplate
                .exchange("http://localhost:" + prdApiPort + uriPath,
                          HttpMethod.GET,
                          request,
                          Map.class,
                          params);
        } catch (HttpStatusCodeException ex) {
            HashMap<String, Object> statusAndBody = new HashMap<>(2);
            statusAndBody.put("http_status", String.valueOf(ex.getRawStatusCode()));
            statusAndBody.put("response_body", ex.getResponseBodyAsString());
            return statusAndBody;
        }

        return getResponse(responseEntity);
    }

    @SuppressWarnings({"PMD.DataflowAnomalyAnalysis"})
    private Map<String, Object> getPcqRecordRequestObject(String uriPath, Object... params) {

        ResponseEntity<PcqRecordWithoutCaseResponse> responseEntity;

        try {
            HttpEntity<?> request = new HttpEntity<>(getCoRelationTokenHeaders());
            responseEntity = restTemplate
                .exchange("http://localhost:" + prdApiPort + uriPath,
                          HttpMethod.GET,
                          request,
                          PcqRecordWithoutCaseResponse.class,
                          params);
        } catch (HttpStatusCodeException ex) {
            HashMap<String, Object> statusAndBody = new HashMap<>(2);
            statusAndBody.put("http_status", String.valueOf(ex.getRawStatusCode()));
            statusAndBody.put("response_body", ex.getResponseBodyAsString());
            return statusAndBody;
        }

        return getResponseObject(responseEntity);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getResponse(ResponseEntity<Map> responseEntity) {

        Map<String, Object> response = objectMapper
            .convertValue(
                responseEntity.getBody(),
                Map.class);

        response.put("http_status", responseEntity.getStatusCode().toString());
        response.put("headers", responseEntity.getHeaders().toString());

        return response;
    }

    @SuppressWarnings({"unchecked", "PMD.LooseCoupling"})
    private Map<String, Object> getResponse(HashMap<String, Object> responseBody) {

        Map<String, Object> response = objectMapper
            .convertValue(responseBody, Map.class);

        response.put("http_status", responseBody.get("http_status"));

        return response;
    }

    private Map<String, Object> getResponseObject(ResponseEntity<PcqRecordWithoutCaseResponse> responseEntity) {

        Map<String, Object> response = new ConcurrentHashMap<>();
        response.put("response_body", responseEntity);
        response.put("http_status", responseEntity.getStatusCode().toString());
        response.put("headers", responseEntity.getHeaders().toString());

        return response;
    }

    private HttpHeaders getS2sTokenHeaders() {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("X-Correlation-Id", "INTEG-TEST-PCQ");
        headers.add("Authorization", "Bearer "
            + PcqUtils.generateAuthorizationToken(SECRET_KEY, SUBJECT, TEST_AUTHORITIES));
        return headers;
    }

    private HttpHeaders getCoRelationTokenHeaders() {

        HttpHeaders headers = new HttpHeaders();
        //headers.setContentType(APPLICATION_JSON);
        headers.add("X-Correlation-Id", "INTEG-TEST-PCQ");
        return headers;
    }

    private HttpHeaders getServiceAuthorisationHeader() {

        HttpHeaders headers = new HttpHeaders();
        headers.add("ServiceAuthorization", "INTEG-TEST-PCQ");
        return headers;
    }

    @SuppressWarnings({"PMD.DataflowAnomalyAnalysis"})
    private Map<String, Object> deleteRequest(String uriPath, Object... params) {

        ResponseEntity<Map> responseEntity;

        try {
            HttpEntity<?> request = new HttpEntity<>(getCoRelationTokenHeaders());
            responseEntity = restTemplate
                .exchange("http://localhost:" + prdApiPort + uriPath,
                          HttpMethod.DELETE,
                          request,
                          Map.class,
                          params);
        } catch (HttpStatusCodeException ex) {
            HashMap<String, Object> statusAndBody = new HashMap<>(2);
            statusAndBody.put("http_status", String.valueOf(ex.getRawStatusCode()));
            statusAndBody.put("response_body", ex.getResponseBodyAsString());
            return statusAndBody;
        }

        return getResponse(responseEntity);
    }
}

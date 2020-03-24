package uk.gov.hmcts.reform.pcqbackend.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.pcqbackend.model.PcqAnswerRequest;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class PcqBackEndClient {

    private static final String APP_BASE_PATH = "/pcq/backend";

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

    public Map<String, Object> findAnswerByPcq(String pcqId) {
        return getRequest(APP_BASE_PATH + "/getAnswer/{pcqId}", pcqId);
    }

    @SuppressWarnings({"rawtypes", "PMD.DataflowAnomalyAnalysis"})
    private <T> Map<String, Object> postRequest(String uriPath, T requestBody) {

        HttpEntity<T> request = new HttpEntity<>(requestBody, getS2sTokenHeaders());
        ResponseEntity<Map> responseEntity = null;

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
    private Map<String, Object> getRequest(String uriPath, Object... params) {

        ResponseEntity<Map> responseEntity = null;

        try {
            HttpEntity<?> request = new HttpEntity<>(getS2sTokenHeaders());
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

    private HttpHeaders getS2sTokenHeaders() {

        HttpHeaders headers = new HttpHeaders();
        //headers.setContentType(APPLICATION_JSON);
        headers.add("X-Correlation-Id", "INTEG-TEST-PCQ");
        return headers;
    }


}

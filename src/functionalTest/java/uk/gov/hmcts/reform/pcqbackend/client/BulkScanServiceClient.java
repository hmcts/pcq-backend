package uk.gov.hmcts.reform.pcqbackend.client;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class BulkScanServiceClient {

    public static final String SERVICE_AUTHORISATION_HEADER = "ServiceAuthorization";

    public String fetchTokenResponse(String tokenUrl, String serviceAuthentication) {
        Response response = RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(tokenUrl)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header(SERVICE_AUTHORISATION_HEADER, "Bearer " + serviceAuthentication)
            .when()
            .get()
            .andReturn();



        assertThat(response.getStatusCode()).isEqualTo(200);

        return response
            .getBody()
            .asString();
    }

}

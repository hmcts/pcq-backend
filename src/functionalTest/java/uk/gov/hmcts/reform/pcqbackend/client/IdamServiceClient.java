package uk.gov.hmcts.reform.pcqbackend.client;

import com.google.common.collect.ImmutableMap;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.pcqbackend.util.TotpGenerator;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class IdamServiceClient {

    public String s2sSignIn(String s2sName, String s2sSecret, String s2sUrl) {

        Map<String, Object> params = ImmutableMap.of(
            "microservice", s2sName,
            "oneTimePassword", TotpGenerator.generate(s2sSecret)
        );

        Response response = RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(s2sUrl)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(params)
            .when()
            .post("/lease")
            .andReturn();

        assertThat(response.getStatusCode()).isEqualTo(200);

        return response
            .getBody()
            .asString();
    }

}

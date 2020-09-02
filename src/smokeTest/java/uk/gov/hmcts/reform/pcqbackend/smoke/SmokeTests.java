package uk.gov.hmcts.reform.pcqbackend.smoke;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static io.restassured.RestAssured.given;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {SmokeTestConfiguration.class})
public class SmokeTests {

    @Value("${test.instance.uri}")
    private String url;

    private static final int HTTP_OK = HttpStatus.OK.value();
    private static final int HTTP_NOT_FOUND = HttpStatus.NOT_FOUND.value();

    RequestSpecification requestSpec;

    @Before
    public void setUp() {
        RequestSpecBuilder builder = new RequestSpecBuilder();
        builder.addParam("http.connection.timeout", "60000");
        builder.addParam("http.socket.timeout", "60000");
        builder.addParam("http.connection-manager.timeout", "60000");
        builder.setRelaxedHTTPSValidation();
        requestSpec = builder.build();
    }

    @Test
    public void shouldGetOkStatusFromHealthEndpointForPcqBackend() {

        ValidatableResponse response = given().spec(requestSpec)
            .when()
            .get(url + "/health")
            .then()
            .statusCode(HTTP_OK);
        assertResponse("Health endpoint should be HTTP 200 (ok)", response, HTTP_OK);
    }

    @Test
    public void shouldGetOkStatusFromInfoEndpointForPcqBackend() {
        ValidatableResponse response = given().spec(requestSpec)
            .when()
            .get(url + "/info")
            .then()
            .statusCode(HTTP_OK)
            .body("git.commit.id", notNullValue())
            .body("git.commit.time", notNullValue());
        assertResponse("Info endpoint should be HTTP 200 (ok)", response, HTTP_OK);
    }

    @Test
    public void shouldGetNotFoundStatusFromGetAnswerEndpointForPcqBackend() {
        ValidatableResponse response = given().spec(requestSpec)
            .when()
            .get(url + "/pcq/backend/getAnswer/smoke-test")
            .then()
            .statusCode(HTTP_NOT_FOUND);
        assertResponse("getAnswer endpoint should be HTTP 404 (not found)", response, HTTP_NOT_FOUND);
    }

    private void assertResponse(String message, ValidatableResponse response, int expectedStatus) {
        int statusCode = response.extract().statusCode();
        Boolean asExpected = statusCode == expectedStatus ? Boolean.TRUE : Boolean.FALSE;
        assertTrue(message, asExpected);
    }
}

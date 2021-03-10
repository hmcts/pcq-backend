package uk.gov.hmcts.reform.pcqbackend.controllers;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.core.PathUtility;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.pcqbackend.util.PcqIntegrationTest;

import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@TestPropertySource(locations = "/application.properties")
@RunWith(SpringIntegrationSerenityRunner.class)
@WithTags({@WithTag("testType:Integration")})
public class SasTokenControllerTest extends PcqIntegrationTest {

    public static final String SERVICE_BULKSCAN = "bulkscan";
    public static final String SERVICE_RANDOM = "random";
    public static final String EXCEPTION_MSG = "Exception while executing test";
    private static final String RESPONSE_SAS_KEY = "sas_token";
    private static final String RESPONSE_HTTP_STATUS = "http_status";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String JSON_RESPONSE = "application/json;charset=UTF-8";
    private static final String EXPECTED_STATUS_MESSAGE = "Should retreive expected HTTP status";
    private static final String EXPECTED_STATUS_OK = "200 OK";
    private static final String EXPECTED_STATUS_UNAUTHORISED = "401";
    private static final String EXPECTED_STATUS_NOT_FOUND = "404";
    private static final int SAS_TOKEN_EXPIRY = 3600;

    @Rule
    public WireMockRule wireMockServer = new WireMockRule(WireMockConfiguration.options().port(4554));

    @Test
    public void testShouldGetSasTokenSuccess() {
        try {
            //Setup authentication stubs
            setupAuthorisationStubs();

            //Now call the actual method.
            Map<String, Object> responseMap = pcqBackEndClient.getPcqBlobStorageSasToken(SERVICE_BULKSCAN);

            //Test the assertions
            assertEquals(responseMap.get(RESPONSE_HTTP_STATUS), EXPECTED_STATUS_OK, EXPECTED_STATUS_MESSAGE);
            verifySasTokenProperties(responseMap.get(RESPONSE_SAS_KEY).toString());

        } catch (Exception e) {
            log.error(EXCEPTION_MSG, e);
        }
    }

    @Test
    public void testShouldGetSasTokenNotFoundService() {
        try {
            //Setup authentication stubs
            setupAuthorisationStubs();

            //Now call the actual method.
            Map<String, Object> responseMap = pcqBackEndClient.getPcqBlobStorageSasToken(SERVICE_RANDOM);

            //Test the assertions
            assertEquals(responseMap.get(RESPONSE_HTTP_STATUS), EXPECTED_STATUS_NOT_FOUND, EXPECTED_STATUS_MESSAGE);

        } catch (Exception e) {
            log.error(EXCEPTION_MSG, e);
        }
    }

    @Test
    public void testShouldGetSasTokenUnauthorised() {
        try {
            //Setup authentication stubs
            setupAuthorisationBadAuthStubs();

            //Now call the actual method.
            Map<String, Object> responseMap = pcqBackEndClient.getPcqBlobStorageSasToken(SERVICE_BULKSCAN);

            //Test the assertions
            assertEquals(responseMap.get(RESPONSE_HTTP_STATUS), EXPECTED_STATUS_UNAUTHORISED, EXPECTED_STATUS_MESSAGE);

        } catch (Exception e) {
            log.error(EXCEPTION_MSG, e);
        }
    }

    private void verifySasTokenProperties(String tokenResponse) throws java.io.IOException, StorageException {
        Map<String, String[]> queryParams = PathUtility.parseQueryString(tokenResponse);
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            .format(OffsetDateTime.now(ZoneOffset.UTC).plusSeconds(SAS_TOKEN_EXPIRY));

        assertThat(queryParams.get("sig")).isNotNull();//this is a generated hash of the resource string
        assertThat(queryParams.get("se")[0]).startsWith(currentDate);//the expiry date/time for the signature
        assertThat(queryParams.get("sv")).contains("2020-04-08");//azure api version is latest
        assertThat(queryParams.get("sp")).contains("rcwl");//access permissions(read-r,create-c,write-w,list-l)
    }

    private void setupAuthorisationStubs() {
        wireMockServer.resetAll();
        wireMockServer.stubFor(get(urlPathMatching("/details"))
                                   .willReturn(aResponse()
                                                   .withHeader(CONTENT_TYPE_HEADER, JSON_RESPONSE)
                                                   .withStatus(200)
                                                   .withBody("reform_scan_blob_router")));
    }

    private void setupAuthorisationBadAuthStubs() {
        wireMockServer.resetAll();
        wireMockServer.stubFor(get(urlPathMatching("/details"))
                                   .willReturn(aResponse()
                                                   .withHeader(CONTENT_TYPE_HEADER, JSON_RESPONSE)
                                                   .withStatus(200)
                                                   .withBody("mickey_mouse_processor")));
    }
}

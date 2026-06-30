package uk.gov.hmcts.reform.pcqbackend.controllers;

import com.azure.storage.blob.BlobServiceVersion;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.core.PathUtility;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.annotations.WithTag;
import net.serenitybdd.annotations.WithTags;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.runner.RunWith;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.pcqbackend.util.PcqIntegrationTest;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
class SasTokenControllerIntegrationTest extends PcqIntegrationTest {

    private static final String SERVICE_BULKSCAN = "bulkscan";
    private static final String SERVICE_RANDOM = "random";
    private static final String RESPONSE_SAS_KEY = "sas_token";
    private static final String RESPONSE_HTTP_STATUS = "http_status";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String JSON_RESPONSE = "application/json;charset=UTF-8";
    private static final String EXPECTED_STATUS_MESSAGE = "Should retreive expected HTTP status";
    private static final String EXPECTED_STATUS_OK = "200 OK";
    private static final String EXPECTED_STATUS_UNAUTHORISED = "401 UNAUTHORIZED";
    private static final String EXPECTED_STATUS_NOT_FOUND = "404 NOT_FOUND";
    private static final int SAS_TOKEN_EXPIRY = 3600;

    @RegisterExtension
    static final WireMockExtension WIREMOCK_SERVER =
        WireMockExtension.newInstance()
            .options(WireMockConfiguration.wireMockConfig().port(4554))
            .build();

    @Test
    void testShouldGetSasTokenSuccess() throws StorageException {
        //Setup authentication stubs
        setupAuthorisationStubs();

        //Now call the actual method.
        Map<String, Object> responseMap = pcqBackEndClient.getPcqBlobStorageSasToken(SERVICE_BULKSCAN);

        //Test the assertions
        assertEquals(EXPECTED_STATUS_OK, responseMap.get(RESPONSE_HTTP_STATUS), EXPECTED_STATUS_MESSAGE);
        verifySasTokenProperties(responseMap.get(RESPONSE_SAS_KEY).toString());
    }

    @Test
    void testShouldGetSasTokenNotFoundService() {
        //Setup authentication stubs
        setupAuthorisationStubs();

        //Now call the actual method.
        Map<String, Object> responseMap = pcqBackEndClient.getPcqBlobStorageSasToken(SERVICE_RANDOM);

        //Test the assertions
        assertEquals(EXPECTED_STATUS_NOT_FOUND, responseMap.get(RESPONSE_HTTP_STATUS), EXPECTED_STATUS_MESSAGE);

    }

    @Test
    void testShouldGetSasTokenUnauthorised() {
        //Setup authentication stubs
        setupAuthorisationBadAuthStubs();

        //Now call the actual method.
        Map<String, Object> responseMap = pcqBackEndClient.getPcqBlobStorageSasToken(SERVICE_BULKSCAN);

        //Test the assertions
        assertEquals(EXPECTED_STATUS_UNAUTHORISED, responseMap.get(RESPONSE_HTTP_STATUS), EXPECTED_STATUS_MESSAGE);
    }

    private void verifySasTokenProperties(String tokenResponse) throws StorageException {
        Map<String, String[]> queryParams = PathUtility.parseQueryString(tokenResponse);

        DateTimeFormatter formatter = DateTimeFormatter
            .ofPattern("yyyy-MM-dd")
            .withLocale(Locale.ENGLISH)
            .withZone(ZoneId.of("UTC"));
        Instant now = Instant.now().plusSeconds(SAS_TOKEN_EXPIRY);
        String currentDate = formatter.format(now);

        BlobServiceVersion latest = BlobServiceVersion.getLatest();
        assertThat(queryParams.get("sig")).isNotNull();//this is a generated hash of the resource string
        assertThat(queryParams.get("se")[0]).startsWith(currentDate);//the expiry date/time for the signature
        assertThat(queryParams.get("sv")).contains(latest.getVersion());//azure api version is latest
        assertThat(queryParams.get("sp")).contains("rcwl");//access permissions(read-r,create-c,write-w,list-l)
    }

    private void setupAuthorisationStubs() {
        WIREMOCK_SERVER.resetAll();
        WIREMOCK_SERVER.stubFor(get(urlPathMatching("/details"))
                                   .willReturn(aResponse()
                                                   .withHeader(CONTENT_TYPE_HEADER, JSON_RESPONSE)
                                                   .withStatus(200)
                                                   .withBody("reform_scan_blob_router")));
    }

    private void setupAuthorisationBadAuthStubs() {
        WIREMOCK_SERVER.resetAll();
        WIREMOCK_SERVER.stubFor(get(urlPathMatching("/details"))
                                   .willReturn(aResponse()
                                                   .withHeader(CONTENT_TYPE_HEADER, JSON_RESPONSE)
                                                   .withStatus(200)
                                                   .withBody("mickey_mouse_processor")));
    }
}

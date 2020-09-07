package uk.gov.hmcts.reform.pcqbackend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.core.PathUtility;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.apache.commons.io.IOUtils;
import org.assertj.core.util.DateUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pcqbackend.client.BulkScanServiceClient;
import uk.gov.hmcts.reform.pcqbackend.client.IdamServiceClient;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringIntegrationSerenityRunner.class)
@Configuration
@SpringBootTest
@ComponentScan("uk.gov.hmcts.reform.pcqbackend")
@ActiveProfiles("functional")
@Slf4j
public class SasTokenControllerTest {

    private static final String IO_EXCEPTION_MSG = "Error during test execution";

    private static final String UTF8_ENCODING = "UTF-8";

    @Value("${idam.s2s-auth.name-cs}")
    protected String s2sName;

    @Value("${idam.s2s-auth.secret-cs}")
    protected String s2sSecret;

    @Value("${idam.s2s-auth.url}")
    private String s2sUrl;

    @Value("${targetInstance}")
    protected String pcqBackEndApiUrl;

    @Value("${storage.url}")
    protected String storageUrl;

    @Value("${storage.blob_pcq_container}")
    private String storagePcqContainer;

    protected IdamServiceClient idamServiceClient;

    protected BulkScanServiceClient bulkScanServiceClient;

    @Before
    public void setUp() {
        idamServiceClient = new IdamServiceClient();
        bulkScanServiceClient = new BulkScanServiceClient();
    }

    @Test
    public void testGetSasTokenEndpointSuccess() {

        try {
            String s2sString = idamServiceClient.s2sSignIn(s2sName, s2sSecret, s2sUrl);
            assertNotNull("S2S String should contain a value", s2sString);

            String sasTokenResponse = bulkScanServiceClient
                .fetchTokenResponse(pcqBackEndApiUrl + "/pcq/backend/token/bulkscan", s2sString);

            assertNotNull("SAS Token should contain a value", sasTokenResponse);
            verifySasTokenProperties(sasTokenResponse);

        } catch (Exception e) {
            log.error(IO_EXCEPTION_MSG, e);
            fail("Unable to successfully retrieve SAS token: " + e.getMessage());
        }
    }

    @Test
    public void testValidateSasTokenAgainstStorageEndpointSuccess() {

        try {
            String s2sString = idamServiceClient.s2sSignIn(s2sName, s2sSecret, s2sUrl);
            assertNotNull("S2S String should contain a value", s2sString);

            String sasTokenResponse = bulkScanServiceClient
                .fetchTokenResponse(pcqBackEndApiUrl + "/pcq/backend/token/bulkscan", s2sString);

            assertNotNull("SAS Token should contain a value", sasTokenResponse);
            verifySasTokenWithStorageContainer(storageUrl, storagePcqContainer, sasTokenResponse);

        } catch (Exception e) {
            log.error(IO_EXCEPTION_MSG, e);
            fail("Unable to successfully validate SAS token with Blob Storage: " + e.getMessage());
        }
    }

    @SuppressWarnings("PMD.LawOfDemeter")
    public void verifySasTokenWithStorageContainer(String storageUrl, String container, String sasTokenResponse)
        throws IOException {
        final ObjectNode node = new ObjectMapper().readValue(sasTokenResponse, ObjectNode.class);
        String authenticatedString = storageUrl + "/" + container + "?comp=list&restype=container&"
            + node.get("sas_token").asText();

        log.info("Storage URL = {}", storageUrl + authenticatedString);
        URL url = new URL(authenticatedString);
        String result = IOUtils.toString(url.openStream(), UTF8_ENCODING);

        log.info("Storage Response = {}", result);
        assertThat(result).isNotNull();
        assertThat(result).contains("Blobs");
        assertThat(result).contains(container);
    }

    private void verifySasTokenProperties(String sasTokenResponse) throws IOException, StorageException {
        final ObjectNode node = new ObjectMapper().readValue(sasTokenResponse, ObjectNode.class);
        Map<String, String[]> queryParams = PathUtility.parseQueryString(node.get("sas_token").asText());

        Date tokenExpiry = DateUtil.parseDatetime(queryParams.get("se")[0]);
        assertThat(tokenExpiry).isNotNull();
        assertThat(queryParams.get("sig")).isNotNull(); //this is a generated hash of the resource string
        assertThat(queryParams.get("sv")).contains("2019-12-12"); //azure api version is latest
        assertThat(queryParams.get("sp")).contains("cwl"); //access permissions(create-c,write-w,list-l)
    }

}

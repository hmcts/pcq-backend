package uk.gov.hmcts.reform.pcqbackend.service;

import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.core.PathUtility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SasTokenServiceTest {

    private static final String BLOB_DEV_CONNECTION_STRING = "UseDevelopmentStorage=true";

    private static final String NOT_NULL_MESSAGE = "Should be not null.";
    private static final String ERROR_MSG_PREFIX = "Test failed because of exception during execution. Message is ";

    private SasTokenService sasTokenService;

    @BeforeEach
    void setUp() {
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
            .connectionString(BLOB_DEV_CONNECTION_STRING)
            .buildClient();

        this.sasTokenService = new SasTokenService(blobServiceClient);
    }

    @Test
    void testGenerateSasTokenSuccess() {
        try {
            String actualSasToken = sasTokenService.generateSasToken("bulkscan");
            assertNotNull(actualSasToken, NOT_NULL_MESSAGE);
            verifySasTokenProperties(actualSasToken);

        } catch (Exception e) {
            fail(ERROR_MSG_PREFIX + e.getMessage(), e);
        }
    }

    private void verifySasTokenProperties(String tokenResponse) throws java.io.IOException, StorageException {
        Map<String, String[]> queryParams = PathUtility.parseQueryString(tokenResponse);
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(new Date());

        assertThat(queryParams.get("sig")).isNotNull();//this is a generated hash of the resource string
        assertThat(queryParams.get("se")[0]).startsWith(currentDate);//the expiry date/time for the signature
        assertThat(queryParams.get("sv")).contains("2022-11-02");//azure api version is latest
        assertThat(queryParams.get("sp")).contains("rcwl");//access permissions(read-r,create-c,write-w,list-l)
    }
}

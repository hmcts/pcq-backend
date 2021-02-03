package uk.gov.hmcts.reform.pcqbackend.service;

import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.sas.BlobContainerSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pcqbackend.exceptions.UnableToGenerateSasTokenException;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Slf4j
@Service
public class SasTokenService {

    private final BlobServiceClient blobServiceClient;

    private static final String PERMISSION_CREATE_READ_WRITE_LIST = "rcwl";

    @Value("${storage.blob-pcq-container}")
    private String pcqContainer;

    @Value("${storage.sas-token-expiry}")
    private int sasTokenExpiryInSeconds;

    public SasTokenService(
        BlobServiceClient blobServiceClient
    ) {
        this.blobServiceClient = blobServiceClient;
    }

    public String generateSasToken(String serviceName) {
        String storageAccountName = blobServiceClient.getAccountName();
        log.info("SAS Token request received for service {}. Account: {}", serviceName, storageAccountName);

        try {
            String sasToken = blobServiceClient
                .getBlobContainerClient(pcqContainer)
                .generateSas(createSharedAccessPolicy());
            log.info("Generated SAS Token = {}...", sasToken.substring(0,30));
            return sasToken;

        } catch (Exception exception) {
            log.error("Error occured while creating SAS token", exception);
            throw new UnableToGenerateSasTokenException(exception);
        }
    }

    private BlobServiceSasSignatureValues createSharedAccessPolicy() {
        return new BlobServiceSasSignatureValues(
            OffsetDateTime.now(ZoneOffset.UTC).plusSeconds(sasTokenExpiryInSeconds),
            BlobContainerSasPermission.parse(PERMISSION_CREATE_READ_WRITE_LIST)
        );
    }

}

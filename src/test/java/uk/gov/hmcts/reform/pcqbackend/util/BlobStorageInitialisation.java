package uk.gov.hmcts.reform.pcqbackend.util;

import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuppressWarnings("HideUtilityClassConstructor")
public class BlobStorageInitialisation {

    private static final String DEV_CONNECTION_STRING = "UseDevelopmentStorage=true";

    private static final String DEV_PCQ_CONTAINER_NAME = "pcq";

    public static void main(String[] args) {
        log.info("Connecting to Azurite Blob Storage");
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
            .connectionString(DEV_CONNECTION_STRING)
            .buildClient();

        log.info("Checking {} container exists", DEV_PCQ_CONTAINER_NAME);
        try {
            blobServiceClient.getBlobContainerClient(DEV_PCQ_CONTAINER_NAME);
            log.info("Container {} already exists", DEV_PCQ_CONTAINER_NAME);

        } catch (Exception excep) {
            log.info("Creating {} container", DEV_PCQ_CONTAINER_NAME);
            blobServiceClient.createBlobContainer(DEV_PCQ_CONTAINER_NAME);
        }
    }
}

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
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
            .connectionString(DEV_CONNECTION_STRING)
            .buildClient();
        try {
            blobServiceClient.getBlobContainerClient(DEV_PCQ_CONTAINER_NAME);
        } catch (Exception excep) {
            blobServiceClient.createBlobContainer(DEV_PCQ_CONTAINER_NAME);
        }
    }
}

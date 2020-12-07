package uk.gov.hmcts.reform.pcqbackend.config;

import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class BlobStorageConfiguration {

    @Bean
    public BlobServiceClient getStorageClient(
        @Value("${storage.connection-string}") String connectionString
    ) {
        return new BlobServiceClientBuilder()
            .connectionString(connectionString)
            .buildClient();
    }
}

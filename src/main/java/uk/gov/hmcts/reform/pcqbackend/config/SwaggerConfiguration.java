package uk.gov.hmcts.reform.pcqbackend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfiguration {

    @Bean
    public OpenAPI springOpenApi() {
        String description = """
            This is the Protected Characteristics Back-End API that will:

            - Save user's answers to the database.
            - Serve the consolidation service to fetch PCQ Ids that don't have an associated
              case record and add case information to a PCQ record in the database.
            - Serve authentication tasks for other services and components to generate a
              Service SAS token for the PCQ Storage Account container 'pcq' - this will
              provide Write, List and Create access.

            Reform Common Components.
            """;

        Info info = new Info()
            .title("PCQ Back-End APIs")
            .description(description)
            .version("1.0");
        return new OpenAPI().info(info);
    }
}

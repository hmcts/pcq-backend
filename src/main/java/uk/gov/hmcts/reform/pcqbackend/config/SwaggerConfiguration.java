package uk.gov.hmcts.reform.pcqbackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import uk.gov.hmcts.reform.pcqbackend.Application;

import java.util.Collections;

@Configuration
@EnableSwagger2
public class SwaggerConfiguration {

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
            .useDefaultResponseMessages(false)
            .select()
            .apis(RequestHandlerSelectors.basePackage(Application.class.getPackage().getName() + ".controllers"))
            .paths(PathSelectors.any())
            .build()
            .apiInfo(apiDetails());
    }

    private ApiInfo apiDetails() {
        return new ApiInfo(
            "PCQ Back-End APIs",
            "This is the Protected Characteristics Back-End API that will:\n\n"
                + "- Save user's answers to the database.\n"
                + "- Serve the consolidation service to fetch PCQ Ids that don't have an associated "
                + "case record and add case information to a PCQ record in the database.\n"
                + "- Serve authentication tasks for other services and components to generate a Service SAS "
                + "token for the PCQ Storage Account container 'pcq' - this will provide "
                + "Write, List and Create access.",
            "1.0",
            "",
            new Contact("", "", ""),
            "Reform Common Components",
            "",
            Collections.emptyList()
        );
    }

}

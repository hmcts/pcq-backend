package uk.gov.hmcts.reform.pcqbackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.pcqbackend.service.SubmitAnswersService;

@Configuration
@ComponentScan("uk.gov.hmcts.reform.pcqbackend.service")
public class AppConfig {

    @Bean
    public SubmitAnswersService getSubmitAnswersService() {
        return new SubmitAnswersService();
    }
}

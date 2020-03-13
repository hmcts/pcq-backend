package uk.gov.hmcts.reform.pcqbackend.smoke;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;

@ComponentScan("uk.gov.hmcts.reform.pcqbackend.smoke")
@PropertySource("application.properties")
public class SmokeTestConfiguration {
}

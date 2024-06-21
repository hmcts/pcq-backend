package uk.gov.hmcts.reform.pcqbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
public class Application {

    public static void main(final String[] args) {

        if ("true".equals(System.getenv("PCQ_DISPOSER_JOB"))) {
            System.exit(SpringApplication.exit(SpringApplication.run(Application.class, args)));
        }
        SpringApplication.run(Application.class, args);
    }
}

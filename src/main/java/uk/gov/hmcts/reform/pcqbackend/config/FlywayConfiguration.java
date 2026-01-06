package uk.gov.hmcts.reform.pcqbackend.config;

import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.pcqbackend.util.FlywayNoOpStrategy;

@Configuration
@ConditionalOnClass(Flyway.class)
@ConditionalOnProperty(prefix = "spring.flyway", name = "enabled", matchIfMissing = true)
public class FlywayConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "flyway.noop", name = "strategy", matchIfMissing = true)
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return new FlywayNoOpStrategy();
    }

    @Bean
    @ConditionalOnProperty(prefix = "flyway.noop", name = "strategy", havingValue = "false")
    public FlywayMigrationStrategy flywayVoidMigrationStrategy() {
        return flyway -> {
            // do nothing (explicit no-op)
        };
    }
}


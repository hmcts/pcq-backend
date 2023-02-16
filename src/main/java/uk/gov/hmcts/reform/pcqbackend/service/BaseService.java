package uk.gov.hmcts.reform.pcqbackend.service;

import org.springframework.core.env.Environment;

public class BaseService {

    Environment environment;

    BaseService(Environment environment) {
        this.environment = environment;
    }

    protected String getEncryptionKey() {
        return environment.getProperty("security.db.backend-encryption-key");
    }
}

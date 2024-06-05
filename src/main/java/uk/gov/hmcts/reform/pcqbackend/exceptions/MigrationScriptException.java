package uk.gov.hmcts.reform.pcqbackend.exceptions;

import java.io.Serial;

public class MigrationScriptException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 4L;

    public MigrationScriptException(String script) {
        super("Found migration not yet applied " + script);
    }
}

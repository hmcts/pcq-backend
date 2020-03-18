package uk.gov.hmcts.reform.pcqbackend.exceptions;

public class MigrationScriptException extends RuntimeException {

    private static final long serialVersionUID = 4L;

    public MigrationScriptException(String script) {
        super("Found migration not yet applied " + script);
    }
}

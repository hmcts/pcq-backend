package uk.gov.hmcts.reform.pcqbackend.util;

import com.eatthepath.otp.TimeBasedOneTimePasswordGenerator;
import org.apache.commons.codec.binary.Base32;

import java.time.Instant;
import java.util.Locale;
import javax.crypto.spec.SecretKeySpec;

public final class TotpGenerator {

    private static final TimeBasedOneTimePasswordGenerator TOTP =
        new TimeBasedOneTimePasswordGenerator();

    private TotpGenerator() {
    }

    public static String generate(String base32Secret) {
        byte[] secretBytes = new Base32().decode(base32Secret.toUpperCase(Locale.ROOT));
        SecretKeySpec key = new SecretKeySpec(secretBytes, "HmacSHA1");

        try {
            return TOTP.generateOneTimePasswordString(key, Instant.now(),Locale.ROOT);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to generate TOTP", ex);
        }
    }

}

package uk.gov.hmcts.reform.pcqbackend.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import uk.gov.hmcts.reform.pcqbackend.exceptions.SecurityConfigurationException;

import static org.assertj.core.api.Fail.fail;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SecurityConfigurationTest {

    @Test
    void shouldThrowSecurityConfigurationExceptionWhenHttpSecurityFails() {
        SecurityConfiguration config = new SecurityConfiguration();
        HttpSecurity httpSecurity = mock(HttpSecurity.class);
        JwtConfiguration jwtConfiguration = mock(JwtConfiguration.class);

        try {
            // Simulate an exception when calling csrf()
            when(httpSecurity.csrf(any())).thenThrow(new RuntimeException("Simulated failure"));

            assertThrows(
                SecurityConfigurationException.class, () ->
                    config.configure(httpSecurity, jwtConfiguration)
            );
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }
}


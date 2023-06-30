package uk.gov.hmcts.reform.pcqbackend.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletResponse;
import java.security.Security;

@Configuration
@EnableWebSecurity
@SuppressWarnings({"PMD.SignatureDeclareThrowsException"})

public class SecurityConfiguration {

    @Autowired
    private JwtConfiguration jwtConfiguration;

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers(
            "/swagger-ui.html",
            "/webjars/springfox-swagger-ui/**",
            "/swagger-resources/**",
            "/health",
            "/health/liveness",
            "/health/readiness",
            "/v2/api-docs/**",
            "/info",
            "/favicon.ico",
            "/"
        );
    }

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        http
        .csrf(csrf -> csrf.disable()) //NOSONAR not used in secure contexts
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

        .exceptionHandling(exc -> exc.authenticationEntryPoint(
            (req, rsp, e) -> rsp.sendError(HttpServletResponse.SC_UNAUTHORIZED)))
        .addFilterAfter(new JwtTokenFilter(jwtConfiguration), UsernamePasswordAuthenticationFilter.class)
        .authorizeRequests()
        .requestMatchers("/pcq/backend/getAnswer/**").permitAll()
        .requestMatchers("/pcq/backend/consolidation/**").permitAll()
        .requestMatchers("/pcq/backend/token/**").permitAll()
        .requestMatchers("/pcq/backend/deletePcqRecord/**").permitAll()
        .requestMatchers("/pcq/backend/submitAnswers**").authenticated()
        .requestMatchers("/v2/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll();
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        return http.build();
    }
}



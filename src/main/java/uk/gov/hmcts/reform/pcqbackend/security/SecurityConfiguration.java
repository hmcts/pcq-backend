package uk.gov.hmcts.reform.pcqbackend.security;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@SuppressWarnings({"PMD.SignatureDeclareThrowsException"})
@RequiredArgsConstructor
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain configure(HttpSecurity http, JwtConfiguration jwtConfiguration) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exc -> exc.authenticationEntryPoint(
                (req, rsp, e) -> rsp.sendError(HttpServletResponse.SC_UNAUTHORIZED)))
            .addFilterAfter(new JwtTokenFilter(jwtConfiguration), UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(
                authz -> authz
                    .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                    .requestMatchers(
                        "/pcq/backend/getAnswer/**",
                        "/pcq/backend/consolidation/**",
                        "/pcq/backend/token/**",
                        "/pcq/backend/deletePcqRecord/**",
                        "/health",
                        "/health/liveness",
                        "/health/readiness",
                        "/info",
                        "/favicon.ico",
                        "/v2/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/"
                    ).permitAll()
                    .requestMatchers("/pcq/backend/submitAnswers**").authenticated()
            );
        return http.build();
    }
}

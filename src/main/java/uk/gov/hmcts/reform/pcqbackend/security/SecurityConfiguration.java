package uk.gov.hmcts.reform.pcqbackend.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;
import jakarta.servlet.http.HttpServletResponse;

import java.security.Security;

@Configuration
@EnableWebSecurity
@SuppressWarnings({"PMD.SignatureDeclareThrowsException"})

public class SecurityConfiguration {

    @Autowired
    private JwtConfiguration jwtConfiguration;

    @Autowired
    HandlerMappingIntrospector introspector;

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers(
            new MvcRequestMatcher(introspector, "/swagger-ui.html"),
            new MvcRequestMatcher(introspector, "/webjars/springfox-swagger-ui/**"),
            new MvcRequestMatcher(introspector, "/swagger-resources/**"),
            new MvcRequestMatcher(introspector, "/health"),
            new MvcRequestMatcher(introspector, "/health/liveness"),
            new MvcRequestMatcher(introspector, "/health/readiness"),
            new MvcRequestMatcher(introspector, "/v2/api-docs/**"),
            new MvcRequestMatcher(introspector, "/info"),
            new MvcRequestMatcher(introspector, "/favicon.ico"),
            new MvcRequestMatcher(introspector, "/")
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
            .requestMatchers(new MvcRequestMatcher(introspector, "/pcq/backend/getAnswer/**")).permitAll()
            .requestMatchers(new MvcRequestMatcher(introspector, "/pcq/backend/consolidation/**")).permitAll()
            .requestMatchers(new MvcRequestMatcher(introspector, "/pcq/backend/token/**")).permitAll()
            .requestMatchers(new MvcRequestMatcher(introspector, "/pcq/backend/deletePcqRecord/**")).permitAll()
            .requestMatchers(new MvcRequestMatcher(introspector, "/pcq/backend/submitAnswers**")).authenticated()
            .requestMatchers(new MvcRequestMatcher(introspector, "/v2/api-docs/**")).permitAll()
            .requestMatchers(new MvcRequestMatcher(introspector, "/swagger-ui/**")).permitAll()
            .requestMatchers(new MvcRequestMatcher(introspector, "/swagger-ui.html")).permitAll();
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        return http.build();
    }
}



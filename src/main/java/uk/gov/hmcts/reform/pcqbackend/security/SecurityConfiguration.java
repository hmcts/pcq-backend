package uk.gov.hmcts.reform.pcqbackend.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import javax.servlet.http.HttpServletResponse;
import java.security.Security;

@Configuration
@EnableWebSecurity
@SuppressWarnings({"PMD.SignatureDeclareThrowsException"})

public class SecurityConfiguration {

    @Autowired
    private JwtConfiguration jwtConfiguration;

    @Autowired
    private HandlerMappingIntrospector introspector;

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
    public SecurityFilterChain configure(HttpSecurity http,MvcRequestMatcher.Builder mvc) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) //NOSONAR not used in secure contexts
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exc -> exc.authenticationEntryPoint(
                (req, rsp, e) -> rsp.sendError(HttpServletResponse.SC_UNAUTHORIZED)))
            .addFilterAfter(new JwtTokenFilter(jwtConfiguration), UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(authorize -> authorize
            .requestMatchers(mvc.pattern("/pcq/backend/getAnswer/**\"")).permitAll()
            .requestMatchers(mvc.pattern("/pcq/backend/getAnswer/**")).permitAll()
            .requestMatchers(mvc.pattern("/pcq/backend/consolidation/**")).permitAll()
            .requestMatchers(mvc.pattern("/pcq/backend/token/**")).permitAll()
            .requestMatchers(mvc.pattern("/pcq/backend/deletePcqRecord/**")).permitAll()
            .requestMatchers(mvc.pattern("/pcq/backend/submitAnswers**")).authenticated()
            .requestMatchers(mvc.pattern("/v2/api-docs/**")).permitAll()
            .requestMatchers(mvc.pattern("/swagger-ui/**")).permitAll()
            .requestMatchers(mvc.pattern("/swagger-ui.html")).permitAll());
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        return http.build();
    }

    @Scope("prototype")
    @Bean
    MvcRequestMatcher.Builder mvc(HandlerMappingIntrospector introspector) {
        return new MvcRequestMatcher.Builder(introspector);
    }

}



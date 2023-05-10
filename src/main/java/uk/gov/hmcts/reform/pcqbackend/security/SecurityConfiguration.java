package uk.gov.hmcts.reform.pcqbackend.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        http
        .csrf().disable() //NOSONAR not used in secure contexts
        .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
        .exceptionHandling().authenticationEntryPoint((req, rsp, e) -> rsp.sendError(HttpServletResponse
                                                                                         .SC_UNAUTHORIZED))
        .and()
        .addFilterAfter(new JwtTokenFilter(jwtConfiguration), UsernamePasswordAuthenticationFilter.class)
        .authorizeRequests()
        .requestMatchers("/pcq/backend/getAnswer/**").permitAll()
        .requestMatchers("/pcq/backend/consolidation/**").permitAll()
        .requestMatchers("/pcq/backend/token/**").permitAll()
        .requestMatchers("/pcq/backend/deletePcqRecord/**").permitAll()
        .requestMatchers("/pcq/backend/submitAnswers**").authenticated()
        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll();
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        return http.build();
    }
}



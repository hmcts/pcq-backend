package uk.gov.hmcts.reform.pcqbackend.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.security.Security;
import javax.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
@Order(1)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private JwtConfiguration jwtConfiguration;

    @Override
    public void configure(WebSecurity web) {
        web.ignoring().antMatchers("/swagger-ui.html",
                                   "/webjars/springfox-swagger-ui/**",
                                   "/swagger-resources/**",
                                   "/health",
                                   "/health/liveness",
                                   "/v2/api-docs/**",
                                   "/info",
                                   "/favicon.ico",
                                   "/");

    }

    @Override
    public void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .exceptionHandling().authenticationEntryPoint((req, rsp, e) -> rsp.sendError(HttpServletResponse
                                                                                             .SC_UNAUTHORIZED))
            .and()
            .addFilterAfter(new JwtTokenFilter(jwtConfiguration), UsernamePasswordAuthenticationFilter.class)
            .authorizeRequests()
            .antMatchers("/pcq/backend/getAnswer/**").permitAll()
            .antMatchers("/pcq/backend/consolidation/**").permitAll()
            .antMatchers("/pcq/backend/submitAnswers**").authenticated();

        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }
}

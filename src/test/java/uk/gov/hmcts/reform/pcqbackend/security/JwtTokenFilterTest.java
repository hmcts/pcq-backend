package uk.gov.hmcts.reform.pcqbackend.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Slf4j
public class JwtTokenFilterTest {

    JwtTokenFilter jwtTokenFilter;
    JwtConfiguration jwtConfiguration;

    private static final String AUTH_NOT_NULL = "Authentication is not null";
    private static final String AUTH_NULL = "Authentication is null";
    private static final String EXCEPTION_SERVLET_MSG = "ServletException thrown while test execution ";
    private static final String EXCEPTION_IO_MSG = "IOException thrown while test execution ";
    private static final String TOKEN_PREFIX = "Bearer ";
    private static final String SECRET_KEY = "Filter-Test-Key-1";

    @BeforeEach
    public void setUp() {
        jwtConfiguration = mock(JwtConfiguration.class);
        jwtTokenFilter = new JwtTokenFilter(jwtConfiguration);
    }

    @Test
    public void testNoHeaders() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        FilterChain mockChain = mock(FilterChain.class);

        try {

            jwtTokenFilter.doFilterInternal(mockRequest, mockResponse, mockChain);
            assertNull(SecurityContextHolder.getContext().getAuthentication(), AUTH_NOT_NULL);

        } catch (ServletException e) {
            log.error(EXCEPTION_SERVLET_MSG, e);
        } catch (IOException e) {
            log.error(EXCEPTION_IO_MSG, e);
        }
    }

    @Test
    public void testEmptyAuthHeader() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        FilterChain mockChain = mock(FilterChain.class);

        when(mockRequest.getHeader(jwtConfiguration.getHeader())).thenReturn("Tearer");
        when(jwtConfiguration.getPrefix()).thenReturn(TOKEN_PREFIX);

        try {

            jwtTokenFilter.doFilterInternal(mockRequest, mockResponse, mockChain);
            assertNull(SecurityContextHolder.getContext().getAuthentication(), AUTH_NOT_NULL);

        } catch (ServletException e) {
            log.error(EXCEPTION_SERVLET_MSG, e);
        } catch (IOException e) {
            log.error(EXCEPTION_IO_MSG, e);
        }

        verify(mockRequest, times(1)).getHeader(jwtConfiguration.getHeader());
    }

    @Test
    public void testAuthSuccess() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        FilterChain mockChain = mock(FilterChain.class);

        when(mockRequest.getHeader(jwtConfiguration.getHeader())).thenReturn("Bearer "
                                                                                 + generateTestToken());
        when(jwtConfiguration.getPrefix()).thenReturn(TOKEN_PREFIX);
        when(jwtConfiguration.getSecret()).thenReturn(SECRET_KEY);

        try {

            jwtTokenFilter.doFilterInternal(mockRequest, mockResponse, mockChain);
            assertNotNull(SecurityContextHolder.getContext().getAuthentication(), AUTH_NULL);
            UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) SecurityContextHolder
                .getContext().getAuthentication();
            assertNotNull(token, "Token should not be null");
            assertEquals("TEST", token.getPrincipal(), "Principal is not correct");

        } catch (ServletException e) {
            log.error(EXCEPTION_SERVLET_MSG, e);
        } catch (IOException e) {
            log.error(EXCEPTION_IO_MSG, e);
        }

        verify(mockRequest, times(1)).getHeader(jwtConfiguration.getHeader());
    }

    @Test
    public void testAuthFailure() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        FilterChain mockChain = mock(FilterChain.class);

        when(mockRequest.getHeader(jwtConfiguration.getHeader())).thenReturn("Bearer "
                                                                                 + generateTestToken());
        when(jwtConfiguration.getPrefix()).thenReturn(TOKEN_PREFIX);
        when(jwtConfiguration.getSecret()).thenReturn("Invalid Key");

        try {
            jwtTokenFilter.doFilterInternal(mockRequest, mockResponse, mockChain);
            assertNull(SecurityContextHolder.getContext().getAuthentication(), AUTH_NOT_NULL);
        } catch (ServletException e) {
            log.error(EXCEPTION_SERVLET_MSG, e);
        } catch (IOException e) {
            log.error(EXCEPTION_IO_MSG, e);
        }

        verify(mockRequest, times(1)).getHeader(jwtConfiguration.getHeader());
    }

    @SuppressWarnings("PMD.LawOfDemeter")
    private String generateTestToken() {
        List<String> authorities = new ArrayList<>();
        long currentTime = System.currentTimeMillis();
        authorities.add("TEST_AUTHORITY");

        return Jwts.builder()
            .setSubject("TEST")
            .claim("authorities", authorities)
            .setIssuedAt(new Date(currentTime))
            .setExpiration(new Date(currentTime + 500_000))  // in milliseconds
            .signWith(SignatureAlgorithm.HS256, JwtTokenFilterTest.SECRET_KEY.getBytes())
            .compact();
    }
}

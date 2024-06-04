package uk.gov.hmcts.reform.pcqbackend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.pcq.commons.utils.PcqUtils.generateAuthorizationToken;

@Slf4j
class JwtTokenFilterTest {

    private JwtTokenFilter jwtTokenFilter;
    private JwtConfiguration jwtConfiguration;

    private static final String AUTH_NOT_NULL = "Authentication is not null";
    private static final String AUTH_NULL = "Authentication is null";
    private static final String EXCEPTION_SERVLET_MSG = "ServletException thrown while test execution ";
    private static final String EXCEPTION_IO_MSG = "IOException thrown while test execution ";
    private static final String TOKEN_PREFIX = "Bearer ";
    private static final String SECRET_KEY = "a-very-long-and-boring-secret-key";
    private static final String SUBJECT = "TEST";
    private static final String TEST_AUTHORITIES = "TEST_AUTHORITY";

    @BeforeEach
    void setUp() {
        jwtConfiguration = mock(JwtConfiguration.class);
        jwtTokenFilter = new JwtTokenFilter(jwtConfiguration);
    }

    @Test
    void testNoHeaders() {
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
    void testEmptyAuthHeader() {
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
    void testAuthSuccess() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        FilterChain mockChain = mock(FilterChain.class);

        when(mockRequest.getHeader(jwtConfiguration.getHeader())).thenReturn(
            "Bearer " + generateAuthorizationToken(JwtTokenFilterTest.SECRET_KEY, SUBJECT, TEST_AUTHORITIES));
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
    void testAuthFailure() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        FilterChain mockChain = mock(FilterChain.class);

        when(mockRequest.getHeader(jwtConfiguration.getHeader())).thenReturn(
            "Bearer " + generateAuthorizationToken(JwtTokenFilterTest.SECRET_KEY, SUBJECT, TEST_AUTHORITIES));
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
}

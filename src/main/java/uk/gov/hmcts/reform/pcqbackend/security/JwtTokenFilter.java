package uk.gov.hmcts.reform.pcqbackend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import javax.crypto.SecretKey;


@Slf4j
public class JwtTokenFilter extends OncePerRequestFilter {

    private final JwtConfiguration jwtConfiguration;

    public JwtTokenFilter(JwtConfiguration jwtConfiguration) {
        super();
        this.jwtConfiguration = jwtConfiguration;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

        String header = request.getHeader(jwtConfiguration.getHeader());
        if (header == null || !header.startsWith(jwtConfiguration.getPrefix())) {
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.replace(jwtConfiguration.getPrefix(), "").strip();
        try {
            SecretKey secretKey = Keys.hmacShaKeyFor(jwtConfiguration.getSecret().getBytes());

            Claims claims = (Claims) Jwts.parser().verifyWith(secretKey).build().parse(token).getPayload();

            String partyId = claims.getSubject();
            if (partyId != null) {
                @SuppressWarnings("unchecked")
                List<String> authorities = (List<String>) claims.get("authorities");
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    partyId, null, authorities.stream().map(SimpleGrantedAuthority::new).toList());

                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }

        } catch (Exception e) {
            log.error("Exception during JWT claims validation - {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);

    }
}

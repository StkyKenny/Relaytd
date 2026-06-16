package me.stky.relaytd.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import me.stky.relaytd.api.service.JWTService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class JwtCookieAuthenticationFilter extends OncePerRequestFilter {

    @Value("${spring.security.jwt.access.name}")
    private String jwtAccessName;

    @Autowired
    private JwtEncoder jwtEncoder;

    @Autowired
    private JwtDecoder jwtDecoder;

    private final JWTService jwtService;

    public JwtCookieAuthenticationFilter(JWTService jwtService) {
        this.jwtService = jwtService;
    }

    /**
     * Authenticate if a valid Jwt Access Token is found
     *
     * @param request
     * @param response
     * @param filterChain
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getServletPath();
        if (path.contains("/auth/") || path.startsWith("/swagger")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (jwtAccessName.equals(cookie.getName()) && jwtService.validateCookie(cookie)) {
                    System.out.println(" cookie: " + cookie.getName() + " = " + cookie.getValue());
                    Jwt jwt = jwtDecoder.decode(cookie.getValue());
                    String username = jwtService.extractUsername(cookie.getValue());

                    log.info("Extracted username: " + username);
                    cookie.getAttributes().entrySet().stream()
                            .forEach(entry -> System.out.println(entry.getKey() + " = " + entry.getValue()));

                    List<GrantedAuthority> authorities = new ArrayList<>();
                    jwt.getClaimAsStringList("roles").stream().forEach(role -> authorities.add(new SimpleGrantedAuthority(role)));

                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(username, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    log.info("Authentification correct : " + auth);
                    break;
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}

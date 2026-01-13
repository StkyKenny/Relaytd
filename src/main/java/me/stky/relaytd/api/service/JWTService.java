package me.stky.relaytd.api.service;


import jakarta.servlet.http.Cookie;
import lombok.extern.slf4j.Slf4j;
import me.stky.relaytd.api.model.UserInfo;
import me.stky.relaytd.api.repository.UserRepository;
import me.stky.relaytd.config.Roles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@Slf4j
public class JWTService {


    @Value("${spring.security.jwt.key}")
    private String jwtKey;

    @Value("${spring.security.jwt.refresh.name}")
    private String jwtRefreshName;
    @Value("${spring.security.jwt.access.name}")
    private String jwtAccessName;
    private String jwtValidationKeyName = "validationkey";

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    private final int refreshTokenExpirationMinutes = 60 * 24 * 7;

    @Autowired
    private UserRepository userRepository;

    private AuthentificationService authentificationService;

    public JWTService(AuthentificationService authentificationService, JwtEncoder jwtEncoder, JwtDecoder jwtDecoder) {
        this.authentificationService = authentificationService;
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
    }

    /**
     * Generate roles string for Jwt Token
     *
     * @param authentication
     * @return
     */
    public List<String> createRoles(Authentication authentication) {
        List<String> authorities = new ArrayList<>();
        if (authentication instanceof OAuth2AuthenticationToken) {
            List<GrantedAuthority> newAuthorities = List.of(
                    new SimpleGrantedAuthority(Roles.ROLE_OAUTH_USER.name()),
                    Roles.toAuthority(Roles.ROLE_USER), // Showing both method to create authority
                    new SimpleGrantedAuthority("ROLE_customedroleforgiggles")
            );
            authorities = newAuthorities.stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
        } else if (authentication instanceof UsernamePasswordAuthenticationToken) {
            authorities = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
        } else {
            throw new IllegalStateException("Authentification token isn't supported");
        }
        log.debug("Roles attributed : " + authorities);

        return authorities;
    }

    /**
     * Generate Jwt Token with a validation key
     *
     * @param authentication
     * @return
     */
    public String generateToken(Authentication authentication) {

        String validationKey = "";
        if (userRepository.findByUsername(authentificationService.fetchDBUsernameFromAuth(authentication)).isPresent()) {
            validationKey = updateNewValidationKey(authentication);
        }

        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plus(refreshTokenExpirationMinutes, ChronoUnit.MINUTES))
                .subject(authentificationService.fetchDBUsernameFromAuth(authentication))
                .claim("roles", createRoles(authentication))
                .claim(jwtValidationKeyName, validationKey)
                .build();
        JwtEncoderParameters jwtEncoderParameters = JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims);
        log.info("Generated this jwt token :" + this.jwtEncoder.encode(jwtEncoderParameters).getTokenValue());
        claims.getClaims().forEach((key, value) -> System.out.println(key + ":" + value));
        return this.jwtEncoder.encode(jwtEncoderParameters).getTokenValue();
    }

    public ResponseCookie generateAccessCookie(String jwtToken) {
        return ResponseCookie.from(jwtAccessName, jwtToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofSeconds(20))
                .sameSite("None") // or "Strict" or "None" or "Lax"
                .build();
    }

    public ResponseCookie generateRefreshCookie(String jwtToken) {
        return ResponseCookie.from(jwtRefreshName, jwtToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofMinutes(refreshTokenExpirationMinutes))
                .sameSite("None") // or "Strict" or "None" or "Lax"
                .build();
    }

    public ResponseCookie invalidateCookie() {
        return ResponseCookie.from(jwtRefreshName, "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("None") // or "Strict" or "None" or "Lax"
                .maxAge(0)
                .build();
    }

    public String extractUsername(String token) {
        Jwt jwt = this.jwtDecoder.decode(token);
        return jwt.getSubject();
    }

    /**
     * Verify the jwt in the cookie is valid (decodable and has the correct validation key)
     *
     * @param cookie Cookie containing the jwt to verify
     * @return True if the cookie is valid
     */
    public boolean validateCookie(Cookie cookie) {
        try {
            Jwt jwtToken = this.jwtDecoder.decode(cookie.getValue());
            String username = extractUsername(jwtToken.getTokenValue());
            if (username.equalsIgnoreCase("visitor") || username.equalsIgnoreCase("visitor2")) {
                return true;
            }
            Optional<UserInfo> userOpt = userRepository.findByUsername(username);
            return userOpt.map(userInfo -> userInfo.getValidation_key().equals(jwtToken.getClaimAsString(jwtValidationKeyName))).orElse(false);
        } catch (JwtException e) {
            return false;
        }
    }


    /**
     * Generate a random string of length 'length' including characters, numbers and special characters
     *
     * @param length Length of the string
     * @return A random string
     */
    public String generateRandomValidationKey(int length) {
        StringBuilder validationKey = new StringBuilder(length);
        Random random = new Random();
        int lowerLimit = 33;
        int upperLimit = 126; // included

        for (int i = 0; i < length; i++) {
            int randomIndex = lowerLimit + (int) (random.nextFloat() * (upperLimit - lowerLimit + 1));
            validationKey.append((char) randomIndex);
        }
        return validationKey.toString();
    }


    /**
     * Will provide and save a new validation key (for refresh token)
     *
     * @param authentication that will use the validation key
     * @return a new validation key
     */
    public String updateNewValidationKey(Authentication authentication) {
        String randomValidationKey = generateRandomValidationKey(128);

        String username = authentificationService.fetchDBUsernameFromAuth(authentication);
        UserInfo user = userRepository.findByUsername(username).orElseThrow(() -> new IllegalStateException("Could not find user [" + username + "] for changing its validation key."));

        user.setValidation_key(randomValidationKey);
        log.debug("Generated this validation key [" + randomValidationKey + "], for this user [" + user.getUsername() + "]");
        userRepository.save(user);
        return randomValidationKey;
    }


    public List<SimpleGrantedAuthority> getRoles(Jwt jwt) {
        return jwt.getClaimAsStringList("roles").stream().map(SimpleGrantedAuthority::new).toList();
    }
}
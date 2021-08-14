package com.azierets.restapijwt.security.jwt;

import com.azierets.restapijwt.exceptionhandler.exception.JwtAuthException;
import com.azierets.restapijwt.model.User;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.Base64;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtService {
    @Value("${jwt.token.expired}")
    private Long expirationTimeInMs;

    @Value("${jwt.token.secret}")
    private String tokenSecret;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @PostConstruct
    private void encodeTokenSecret() {
        this.tokenSecret = Base64.getEncoder().encodeToString(tokenSecret.getBytes());
    }

    public String generateToken(User user) {
        Date currentTime = new Date();
        return Jwts.builder()
                .setSubject(user.getEmail())
                .setIssuedAt(currentTime)
                .setExpiration(new Date(currentTime.getTime() + expirationTimeInMs))
                .signWith(SignatureAlgorithm.HS256, tokenSecret)
                .compact();
    }

    public String getUserEmail(String token) {
        return Jwts.parser().setSigningKey(tokenSecret).parseClaimsJws(token).getBody().getSubject();
    }

    public String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    public boolean isTokenValid(String token) {
        try {
            Jws<Claims> claims = Jwts.parser().setSigningKey(tokenSecret).parseClaimsJws(token);
            return !claims.getBody().getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            throw new JwtAuthException("Token is expired or invalid");
        }
    }
}

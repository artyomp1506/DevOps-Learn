package com.example.demo.security;

import java.util.Base64;
import java.util.Date;
import java.util.HashMap;

import com.example.demo.entity.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component

public class JwtUtil {
    @Value("${jwt.access-secret}")
    private String accessSecret;
    @Value("${jwt.refresh-secret}")
    private String refreshSecret;
    @Value("${jwt.access-token-expiration-time}")
    private Long accessTokenExpirationTime;
    @Value("${jwt.refresh-token-expiration-time}")
    private Long refreshTokenExpirationTime;

    private Claims getClaimsFromToken(String authToken, String secret) {
        String key = Base64.getEncoder().encodeToString(secret.getBytes());
        return Jwts.parser().setSigningKey(key).build().parseClaimsJws(authToken).getBody();
    }
    public Long extractUserId(String authToken) {
        return Long.valueOf(getClaimsFromToken(authToken, accessSecret)
                .getSubject());
    }
    public boolean validateAccessToken(String authToken) {
        return getClaimsFromToken(authToken, accessSecret)
                .getExpiration()
                .after(new Date());
    }
    public boolean validateRefreshToken(String authToken) {
        return getClaimsFromToken(authToken, refreshSecret)
                .getExpiration()
                .after(new Date());
    }
    public String generateAccessToken(User user) {
        HashMap<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole());

        Date creationDate = new Date();
        Date expirationDate = new Date(creationDate.getTime() + accessTokenExpirationTime * 1000);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(String.valueOf(user.getId()))
                .setIssuedAt(creationDate)
                .setExpiration(expirationDate)
                .signWith(Keys.hmacShaKeyFor(accessSecret.getBytes()))
                .compact();
    }
    public String generateRefreshToken(User user) {
        Date creationDate = new Date();
        Date expirationDate = new Date(creationDate.getTime() + refreshTokenExpirationTime);

        return Jwts.builder()
                .setSubject(String.valueOf(user.getId()))
                .setIssuedAt(creationDate)
                .setExpiration(expirationDate)
                .signWith(Keys.hmacShaKeyFor(refreshSecret.getBytes()))
                .compact();
    }
}

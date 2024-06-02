package com.example.demo.security;

import com.example.demo.dto.JwtResponse;
import com.example.demo.entity.user.User;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtFactory {
    @Value("${jwt.access-token-expiration-time}")
    private Long accessTokenExpirationTime;
    private final JwtUtil jwtUtil;

    public JwtFactory(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    public JwtResponse getJwtResponse(User user){
        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);
        return new JwtResponse(accessToken, refreshToken, new Date().getTime() + accessTokenExpirationTime * 1000);
    }
}

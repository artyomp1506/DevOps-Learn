package com.example.demo.entity.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class JwtTokenPair {
    private String accessToken;
    private String refreshToken;
}

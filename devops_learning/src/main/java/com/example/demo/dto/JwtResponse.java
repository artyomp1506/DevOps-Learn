package com.example.demo.dto;

import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class JwtResponse {
    private String accessToken;
    private String refreshToken;
    private Long expirationDate;
}

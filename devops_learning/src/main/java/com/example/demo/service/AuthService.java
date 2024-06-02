package com.example.demo.service;

import com.example.demo.dto.JwtRequest;
import com.example.demo.dto.JwtResponse;
import com.example.demo.dto.UserDto;
import com.example.demo.entity.JwtToken;
import com.example.demo.entity.user.JwtTokenPair;
import com.example.demo.entity.user.User;
import com.example.demo.repository.TokenRepository;
import com.example.demo.security.AuthJwtFilter;
import com.example.demo.security.JwtFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service

public class AuthService {
    private AuthJwtFilter jwtFilter;
    private TokenRepository tokenRepository;
    private UserService userService;
    private JwtFactory jwtFactory;
   private Long accessTokenTime;
    private Long expireTimeToken;
    public AuthService(AuthJwtFilter jwtFilter, TokenRepository tokenRepository, UserService userService, JwtFactory jwtFactory) {
        this.jwtFilter = jwtFilter;
        this.tokenRepository = tokenRepository;
        this.userService = userService;
        this.jwtFactory = jwtFactory;
        this.accessTokenTime = 86400L;
        this.expireTimeToken = 2629800000L;
    }
    public JwtResponse signUp(UserDto userDto) {
        User user = userService.createUser(userDto.getUsername(), userDto.getPassword());
        return jwtFactory.getJwtResponse(user);
    }
    public JwtResponse login(JwtRequest jwtRequest) {
        User user = userService.login(jwtRequest);
        return jwtFactory.getJwtResponse(user);
    }
    public String logout(JwtTokenPair jwtTokenPair){
        // Save used tokens to redis blacklist
        tokenRepository.saveAll(List.of(
                new JwtToken(jwtTokenPair.getAccessToken(), accessTokenTime),
                new JwtToken(jwtTokenPair.getRefreshToken(), expireTimeToken))
        );
        return "Вы вышли из системы";
    }

}

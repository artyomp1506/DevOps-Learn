package com.example.demo.controller;

import com.example.demo.dto.JwtRequest;
import com.example.demo.dto.JwtResponse;
import com.example.demo.dto.UserDto;
import com.example.demo.entity.user.JwtTokenPair;
import com.example.demo.service.AuthService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private AuthService authService;

    @PostMapping(value = "/signup", produces = "application/json; charset=utf-8")
    public JwtResponse signUp(@Valid @RequestBody UserDto userDto) {
        return authService.signUp(userDto);
    }

    @PostMapping(value = "/login", produces = "application/json; charset=utf-8")
    public JwtResponse login(@Valid @RequestBody JwtRequest jwtRequest) {
        return authService.login(jwtRequest);
    }
    @PostMapping(value = "/logout", produces = "application/json; charset=utf-8")
    public String logout(@Valid @RequestBody JwtTokenPair jwtTokenPair){
        return authService.logout(jwtTokenPair);
    }
}

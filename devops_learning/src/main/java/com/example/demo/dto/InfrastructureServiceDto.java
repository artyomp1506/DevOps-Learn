package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class InfrastructureServiceDto {
    private String[] addresses;
    private String state;
}

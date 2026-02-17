package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiParameterDto {
    private String name;
    private String title;
    private String value;
    private boolean inUserInput;
}

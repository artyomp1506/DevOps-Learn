package com.example.demo.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Max;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class APIHeaderDto {
    private String title;
    private String key;

    private String value;
    private boolean inUserInput;
}

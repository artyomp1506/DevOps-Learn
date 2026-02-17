package com.example.demo.checker;

import jakarta.persistence.Entity;

@Entity
public class ApiResponseCheck {
    private String key;
    private String value;
    private ApiCheckerType type;
}

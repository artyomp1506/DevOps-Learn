package com.example.demo.dto;


import com.example.demo.entity.ApiCheckerType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ApiResponseCheckDto {
    private String key;
    private String value;
    private ApiCheckerType type;
    private float score;
    private String failMessage;
}

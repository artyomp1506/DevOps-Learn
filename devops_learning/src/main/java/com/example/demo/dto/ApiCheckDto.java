package com.example.demo.dto;

import com.example.demo.entity.ApiHeader;
import com.example.demo.entity.ApiResponseCheck;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ApiCheckDto {
    private String name;
    private String method;
    private String url;
    private String body;
    private List<APIHeaderDto> headers;
    private List<ApiResponseCheckDto> checks;
}

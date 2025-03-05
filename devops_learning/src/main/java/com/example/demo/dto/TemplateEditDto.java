package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.json.simple.JSONObject;

@AllArgsConstructor
@Getter
public class TemplateEditDto {
    private JSONObject configBody;
}

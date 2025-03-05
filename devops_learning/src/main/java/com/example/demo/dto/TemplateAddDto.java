package com.example.demo.dto;

import io.swagger.v3.core.util.Json;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.json.simple.JSONObject;

@AllArgsConstructor
@Getter
public class TemplateAddDto {
   private String name;
   private JSONObject configBody;
}

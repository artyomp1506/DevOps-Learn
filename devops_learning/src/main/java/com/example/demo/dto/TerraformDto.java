package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class TerraformDto {
    private String name;
    private String filePath;
    private String manifest;
}

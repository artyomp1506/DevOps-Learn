package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ApiCheckExecuteDto {
    private long taskId;
    private long apiCheckId;
}

package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class LocalTaskDTO {
    private long templateId;
    private String[] ipAddresses;
}

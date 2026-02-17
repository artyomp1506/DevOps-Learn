package com.example.demo.dto;

import lombok.Getter;

import java.util.Date;
import java.util.List;

@Getter
public class UniversalTaskDto {
    private String name;
    private String description;
    private List<Long> ansibleIds;
    private List<Long> terraformIds;
    private List<Long> apiCheckIds;
    private Date deadLine;
    private int maxScore;
}

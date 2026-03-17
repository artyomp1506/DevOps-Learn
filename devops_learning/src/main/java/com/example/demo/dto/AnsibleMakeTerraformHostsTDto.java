package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.json.simple.JSONArray;

@Getter
@AllArgsConstructor
public class AnsibleMakeTerraformHostsTDto {
    private long ansibleId;
    private long terraformId;
    private long taskId;
    private JSONArray hostKeys;
}

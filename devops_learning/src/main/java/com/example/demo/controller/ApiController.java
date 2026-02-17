package com.example.demo.controller;

import com.example.demo.dto.ApiCheckDto;
import com.example.demo.dto.ApiCheckExecuteDto;
import com.example.demo.dto.ApiParameterDto;
import com.example.demo.entity.ApiEntity;
import com.example.demo.entity.check_results.Result;
import com.example.demo.service.ApiService;
import lombok.AllArgsConstructor;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@AllArgsConstructor
public class ApiController {
    private ApiService apiService;
    @PostMapping("/api-task/create/")
    public ApiEntity createTask (@RequestBody ApiCheckDto dto)
    {
        return apiService.createCheck(dto.getName(), dto.getMethod(), dto.getUrl(), dto.getBody(), dto.getHeaders(), dto.getChecks());
    }
    @PostMapping("/api-task/execute")
    public List<Result> executeAndCheck(@RequestBody ApiCheckExecuteDto dto) throws ParseException, IOException, InterruptedException {
        return apiService.executeAndCheck(dto.getTaskId(), dto.getApiCheckId());
    }
    @PostMapping("/api-task/parameter/create/{checkId}")
    public ApiParameterDto createParameter(@RequestBody ApiParameterDto dto, @PathVariable long checkId) {
        this.apiService.createParameter(dto, checkId);
        return dto;
    }

}

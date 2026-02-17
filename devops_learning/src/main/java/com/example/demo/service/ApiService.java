package com.example.demo.service;

import com.example.demo.checker.ApiExecutor;
import com.example.demo.dto.*;
import com.example.demo.entity.*;
import com.example.demo.entity.check_results.Check;
import com.example.demo.entity.check_results.Result;
import com.example.demo.entity.check_results.State;
import com.example.demo.entity.task.Task;
import com.example.demo.repository.*;
import com.google.gson.JsonParser;
import lombok.AllArgsConstructor;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
@AllArgsConstructor
public class ApiService {
    private Map<String, String> variables;
    private ApiHeaderRepository apiHeaderRepository;
    private ApiCheckRepository apiCheckRepository;
    private ApiTaskRepository apiTaskRepository;
    private ITaskRepository taskRepository;
    private CheckerRepository checkerRepository;
    private ApiParameterRepository parameterRepository;

    public ApiEntity createCheck(String name, String method, String baseUrl, String body, List<APIHeaderDto> headers,
                                 List<ApiResponseCheckDto> apiResponseCheckDtos) {
    var requestHeaders = new ArrayList<ApiHeader>();
    var responseChecks = new ArrayList<ApiResponseCheck>();
    for (var header:headers)
        requestHeaders.add(new ApiHeader(header.getTitle(), header.getKey(), header.getValue(), header.isInUserInput()));
    apiHeaderRepository.saveAll(requestHeaders);
    for (var responseCheck:apiResponseCheckDtos)
        responseChecks.add(new ApiResponseCheck(responseCheck.getKey(), responseCheck.getValue(), responseCheck.getType(), responseCheck.getScore(), responseCheck.getFailMessage()));
    apiCheckRepository.saveAll(responseChecks);
        var apiEntity = new ApiEntity(name, method, baseUrl, body, requestHeaders, responseChecks, new ArrayList<APIParameterEntity>());
        apiTaskRepository.save(apiEntity);
        return apiEntity;
    }
    public List<Result> executeAndCheck(long taskId, long apiEntityId) throws ParseException, IOException, InterruptedException {
        var task = taskRepository.findById(taskId).get();
        var check = new Check(taskId);
        checkerRepository.save(check);
        var apiEntity = apiTaskRepository.findById(apiEntityId).get();
        if (apiEntity.getMethod().equals("get"))
            return checkGetMethod(apiEntity.getUrl(), apiEntity.getHeaders(), apiEntity.getChecks(), task, check);
        if (apiEntity.getMethod()=="post")
            return checkPostMethod(apiEntity.getUrl(), apiEntity.getBody(), apiEntity.getHeaders(), apiEntity.getChecks(), task, check);
        return new ArrayList<Result>();
    }

    private List<Result> checkPostMethod(String url, String body, List<ApiHeader> headerList, List<ApiResponseCheck> checks, Task task, Check taskCheck) throws IOException, InterruptedException, ParseException {
        var requestHeaders = getHeaders(headerList);
        var executor = new ApiExecutor(url, requestHeaders);
        var response = executor.sendPostRequest(body);
        return checkResponse(response, checks, task, taskCheck);
    }

    private List<Result> checkGetMethod(String url, List<ApiHeader> headerList, List<ApiResponseCheck> checks, Task task, Check taskCheck) throws ParseException, IOException, InterruptedException {
        var headers =  new HashMap<String, String>();
        var requestHeaders = getHeaders(headerList);
        var executor = new ApiExecutor(url, requestHeaders);
        var response = executor.sendGetResponse();
        return checkResponse(response, checks, task, taskCheck);
    }

    private  ArrayList<Map<String, String>> getHeaders(List<ApiHeader> headerList) {
        var requestHeaders = new ArrayList<Map<String, String>>();
        for (var header: headerList)
        {
            var headerMap = new HashMap<String, String>();
            headerMap.put("key", header.getKey());
            headerMap.put("value", header.getValue());
            requestHeaders.add(headerMap);
        }
        return requestHeaders;
    }

    private List<Result> checkResponse(String response, List<ApiResponseCheck> checks, Task task, Check taskCheck) throws ParseException {
        var results = new ArrayList<Result>();
        if (response.startsWith("{") || response.startsWith("["))
        {
            var jsonResponse = (JSONObject) new JSONParser().parse(response);
            for (var check:checks)
            {
                var key = check.getKey();
                var value = check.getValue();
                var responseValue = getValueFromPath(jsonResponse, key);
                if (check.getType() == ApiCheckerType.Contains) {

                   if (!responseValue.contains(value))
                       results.add(new Result(task, check.getFailMessage()!=null? check.getFailMessage() : String.format("В ответе ключ %s не содержит %s", key, value), State.Wrong, taskCheck));

                }
                else if (check.getType() == ApiCheckerType.Equals) {
                    if (!responseValue.equals(value))
                        results.add(new Result(task, check.getFailMessage()!=null? check.getFailMessage() : String.format("В ответе ключ %s не равен %s", key, value), State.Wrong, taskCheck));

                }
                else if (check.getType()==ApiCheckerType.Regex) {
                    if (!responseValue.matches(value))
                        results.add(new Result(task, check.getFailMessage()!=null? check.getFailMessage() : String.format("В ответе ключ %s не совпадает по регуляярному выражению с %s", key, value), State.Wrong, taskCheck));
                }

            }
        }
        return results;
    }

    private String getValueFromPath(JSONObject response, String path) {
        if (path.contains(".")) {
            var keys = path.split("\\.");
            JSONObject currentObject = response;
            for (var key : keys) {
                var obj = currentObject.get(key);
                if (obj instanceof JSONObject)
                    currentObject = (JSONObject) obj;
                else return (String) obj;
            }
        }
        return (String) response.get(path);
    }

    private String convertToValid(String parametersString) {
        System.out.println(parametersString);
        if (parametersString == null)
            return null;
        Pattern pattern = Pattern.compile("(\\$\\{(.*?)\\})",
                Pattern.MULTILINE);
        StringBuilder result = new StringBuilder(parametersString);
        var matcher = pattern.matcher(result);
        int startIndex=0;
        while (matcher.find(startIndex)) {
            System.out.println(matcher.group(2));
            var replace = (String) variables.get(matcher.group(2));
            result.replace(matcher.start(), matcher.end(), replace);
            System.out.println(replace);
            startIndex = matcher.start()+replace.length();
        }
        return result.toString();
    }

    public APIParameterEntity createParameter(ApiParameterDto dto, long checkId) {
        var parameterEntity = new APIParameterEntity(dto.getName(), dto.getValue(), dto.getTitle(), dto.isInUserInput());
        var checkEntity = this.apiTaskRepository.findById(checkId).get();
        checkEntity.getParameters().add(parameterEntity);
        parameterRepository.save(parameterEntity);
        apiTaskRepository.save(checkEntity);
        return parameterEntity;


    }
    public List<HeaderResponseDto> listUserHeaders(long checkId) {
        var results = new ArrayList<HeaderResponseDto>();
        var apiCheck = apiTaskRepository.findById(checkId).get();
        for (var header:apiCheck.getHeaders())
            if (header.isInUserInput())
                results.add(new HeaderResponseDto(header.getTitle(), header.getKey()));
        return results;
    }
    public List<ParameterResponseDto> getUserParameterKeys(long checkId) {
        var results = new ArrayList<ParameterResponseDto>();
        var parameters = this.apiTaskRepository.findById(checkId).get().getParameters();
        for (var parameter:parameters)
            if (parameter.isInUserInput())
                results.add(new ParameterResponseDto(parameter.getTitle(), parameter.getKey()));
        return results;
    }
}


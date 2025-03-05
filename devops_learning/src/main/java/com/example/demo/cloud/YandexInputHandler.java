package com.example.demo.cloud;

import com.example.demo.InputFieldsHandler;
import com.example.demo.dto.InputParameterDto;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class YandexInputHandler implements InputFieldsHandler {
    @Override
    public List<InputParameterDto> getInputFields(JSONObject taskObject) {
        var results = new ArrayList<InputParameterDto>();
        var yandex = (JSONObject) taskObject.get("yandex");
        var inputs = (JSONArray) yandex.get("input");
        for (var input:inputs)
        {
            var jsonInput = (JSONObject) input;
            var title = (String) jsonInput.get("title");
            var configName = (String) jsonInput.get("config_name");
            results.add(new InputParameterDto(title, configName));
        }
         return results;
    }
}

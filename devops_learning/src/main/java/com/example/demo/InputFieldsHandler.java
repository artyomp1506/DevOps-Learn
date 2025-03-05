package com.example.demo;

import com.example.demo.dto.InputParameterDto;
import org.json.simple.JSONObject;

import java.util.List;

public interface InputFieldsHandler {
     List<InputParameterDto> getInputFields(JSONObject taskObject);
}

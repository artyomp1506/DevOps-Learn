package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.json.simple.JSONObject;

@AllArgsConstructor
@Getter
public class YandexCheckDto {
    private String ycToken;
    private String ycFolderId;
    private JSONObject inputParameters;
    @Override
    public String toString() {
        return ycToken+' '+ycFolderId+' '+inputParameters.toString();
    }
}

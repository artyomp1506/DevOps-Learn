package com.example.demo.cloud;

import com.example.demo.entity.check_results.Check;
import com.example.demo.entity.task.Task;
import lombok.Getter;
import org.json.simple.JSONObject;

import java.util.regex.Pattern;

@Getter
public abstract class YandexChecker {
    private String ycToken;
    private String ycFolderId;
    private Task task;
    private Check check;
    private CloudService cloudService;
    private JSONObject inputParameters;
    public YandexChecker(String ycToken, String ycFolderId, JSONObject inputParameters, Task task, Check check)
    {
        this.ycToken = ycToken;
        this.ycFolderId = ycFolderId;
        this.cloudService = new CloudService(this.ycFolderId, null, this.ycToken);
        this.inputParameters = inputParameters;
        this.task = task;
        this.check = check;
    }
    protected String getValueFromInput(String input) {
        System.out.println(input);
        if (input == null)
            return null;
        Pattern pattern = Pattern.compile("(\\[(.*?)\\])",
                Pattern.MULTILINE);
        StringBuilder result = new StringBuilder(input);
        var matcher = pattern.matcher(result);
        int startIndex=0;
        while (matcher.find(startIndex)) {
            System.out.println(matcher.group(2));
            var replace = (String) inputParameters.get(matcher.group(2));
            result.replace(matcher.start(), matcher.end(), replace);
            System.out.println(replace);
            startIndex = matcher.start()+replace.length();
        }
        return result.toString();
    }
}

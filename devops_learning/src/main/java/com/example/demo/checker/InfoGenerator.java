package com.example.demo.checker;

import com.example.demo.entity.task.TaskInfo;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class InfoGenerator {
    private String filePath;
    private String username;
    private long taskId;

    public InfoGenerator(String filePath, String username, long taskId) {
        this.filePath = filePath;
        this.username = username;
        this.taskId = taskId;
    }
    public List<TaskInfo> generateInfo() throws IOException, ParseException {
        var parser = new JSONParser();
        var results = new ArrayList<TaskInfo>();
        var sourceFile = (JSONObject) parser.parse(new FileReader(filePath));
        var sourceInfo = (JSONArray) sourceFile.get("info");
        if (sourceInfo==null)
            return results;
        for (var infoobj:sourceInfo)
        {
            var info = (JSONObject) infoobj;
            var generated = info.get("generated");
            var value ="";
            var name = (String) info.get("name");
            var title = (String) info.get("title");
           var fileValue = (String) info.get("value");
           if (generated==null)
            value = fileValue.equals("${generated}")? generateValue():fileValue;
           else value=generateFromArray((JSONArray) generated);
           var result = new TaskInfo(name, title, value, taskId);
           results.add(result);
        }
        return results;
    }
    private String generateFromArray(JSONArray array)
    {
        var random = new Random();
        var index = random.nextInt(array.size());
        return (String) array.get(index);
    }

    private String generateValue() {

        var random = new Random();
        var builder = new StringBuilder();
        builder.append(String.format("%s-", username));
        for (int i=0; i<8; i++)
            builder.append((char)('A'+random.nextInt(40, 57)));
        return builder.toString();
    }
}

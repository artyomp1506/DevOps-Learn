package com.example.demo.service;

import com.example.demo.entity.task.TaskTemplate;
import com.example.demo.repository.TemplateRepository;
import lombok.AllArgsConstructor;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.json.simple.parser.*;
@Service
@AllArgsConstructor
public class TemplateService {
    private TemplateRepository templateRepository;
    public List<TaskTemplate> getTemplates()
    {
        var templates = new ArrayList<TaskTemplate>();
        for (var template:templateRepository.findAll())
            templates.add(template);
        return templates;
    }
    public List<TaskTemplate> updateRepository()
    {
        var parser = new JSONParser();
        List<Path> taskFiles;
    try(var paths= Files.walk(Paths.get("./tasks/"))) {
        taskFiles = paths.map(Path::normalize)
                .filter(Files::isRegularFile)
                .collect(Collectors.toList());

        for (var path : taskFiles) {
            var absolutePath = path.toAbsolutePath().toString();
            System.out.println(absolutePath);
           var taskObject = (JSONObject) parser.parse(new FileReader(absolutePath));
           var title = (String) taskObject.get("title");


           var template = templateRepository.findByFilePath(absolutePath);
           if (template==null)
           {
               template = new TaskTemplate(title, absolutePath);

           }
           else {
               template.setTitle(title);
               template.setFilePath(absolutePath);;

           }
            templateRepository.save(template);

        }
    } catch (ParseException | IOException ex) {
        throw new RuntimeException(ex);
    }
        return getTemplates();
    }
    public TaskTemplate getTemplate(long id)
    {
        return templateRepository.findById(id).get();
    }
    public void addTemplate(String templateName, String templateConfig) throws RuntimeException, IOException {
        var path = "./tasks/"+templateName;
        var file = new File(path);
        if (file.exists() && file.isFile())
            throw  new RuntimeException("Задача уже существует");
        Files.writeString(Paths.get(path), templateConfig);
        updateRepository();
    }
    public void deleteTemplate(long id)
    {
        var template = templateRepository.findById(id).orElseThrow();
        var path = template.getFilePath();
        var config = new File(path);
        if (config.exists())
            config.delete();
        templateRepository.delete(template);
    }
    public JSONObject readTemplate(long id) throws RuntimeException {
        var template = templateRepository.findById(id).get();
        var path = template.getFilePath();
        if (Files.exists(Paths.get(path)))
        {
            try {
                var parseConfig = new JSONParser().parse(new FileReader(path));
                return (JSONObject) parseConfig;
            } catch (IOException | ParseException e) {
                throw new RuntimeException(e);
            }
        }
        throw new RuntimeException("Файла задачи не существует");
    }


    public void editTemplate(long id, String configBody) throws RuntimeException {
        var template = templateRepository.findById(id).get();
        var filePath = template.getFilePath();
        var file = new File(filePath);
        if (file.exists() && file.isFile())
        {
            try {
                Files.writeString(Paths.get(filePath), configBody);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }
}




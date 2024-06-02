package com.example.demo.service;

import com.example.demo.entity.task.TaskTemplate;
import com.example.demo.repository.TemplateRepository;
import lombok.AllArgsConstructor;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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
    }



package com.example.demo.repository;

import com.example.demo.entity.task.TaskTemplate;
import org.springframework.data.repository.CrudRepository;

public interface TemplateRepository extends CrudRepository<TaskTemplate, Long> {
    TaskTemplate findByFilePath(String filePath);
}

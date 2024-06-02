package com.example.demo.service;

import com.example.demo.entity.task.TaskType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class ImageService {
    private HashMap<TaskType, String> taskTypeImages;
    @Autowired
    public ImageService()
    {
        taskTypeImages = new HashMap<>();
        taskTypeImages.put(TaskType.Gitlab, "fd88v6jeil9qgbpqb170");
        taskTypeImages.put(TaskType.GrafanaPlusZabbix, "fd89ucnrh5d15hhet1js");
    }
    public String getImageByTaskType(TaskType type)
    {
        return taskTypeImages.get(type);
    }
}

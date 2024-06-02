package com.example.demo.entity.task;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class TaskInfo {
    @Id
    @GeneratedValue
    private long id;
    private String name;
    private String title;
    private String value;
    @Column(name = "task_id")
    private long taskId;

    public TaskInfo(String name, String title, String value, long taskId) {
        this.name = name;
        this.title = title;
        this.value = value;
        this.taskId = taskId;
    }


}

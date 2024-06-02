package com.example.demo.entity.task;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class TaskTemplate {
    @Id
    @GeneratedValue
    private long id;
    private String title;
    private String filePath;

    public TaskTemplate(String title, String filePath) {
        this.title = title;
        this.filePath = filePath;

    }


}

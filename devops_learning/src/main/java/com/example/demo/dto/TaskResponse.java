package com.example.demo.dto;

import com.example.demo.entity.task.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class TaskResponse {
    private long id;
    private Status status;
}

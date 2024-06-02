package com.example.demo.dto;

import com.example.demo.entity.task.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class TaskDetailResponse {
    private Status status;
    private List<String> virtualMachinesIpAddresses;
}

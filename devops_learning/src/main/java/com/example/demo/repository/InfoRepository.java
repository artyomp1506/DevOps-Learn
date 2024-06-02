package com.example.demo.repository;

import com.example.demo.entity.task.TaskInfo;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface InfoRepository extends CrudRepository<TaskInfo, Long> {
    List<TaskInfo> findByTaskId(long taskId);
}

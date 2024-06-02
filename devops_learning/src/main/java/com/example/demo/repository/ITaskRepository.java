package com.example.demo.repository;

import com.example.demo.entity.task.Task;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface ITaskRepository extends CrudRepository<Task,Long> {
    List<Task> findAllByUserId(long userId);
}

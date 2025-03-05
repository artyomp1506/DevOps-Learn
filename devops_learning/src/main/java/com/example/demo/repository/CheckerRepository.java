package com.example.demo.repository;

import com.example.demo.entity.check_results.Check;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CheckerRepository extends CrudRepository<Check, Long> {
    List<Check> getByTaskId(long taskId);
}

package com.example.demo.repository;

import com.example.demo.entity.check_results.IResult;
import com.example.demo.entity.check_results.Result;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface IResultRepository extends CrudRepository<Result, Long> {
    List<Result> getResultByTaskId(long taskId);
    List<Result> getResultsByCheckId(long checkId);
}

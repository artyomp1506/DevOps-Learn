package com.example.demo.service;

import com.example.demo.entity.check_results.Result;
import com.example.demo.repository.IResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResultService {
    private IResultRepository resultRepository;
    @Autowired
    public ResultService(IResultRepository resultRepository) {
        this.resultRepository = resultRepository;
    }
   public List<Result> getByCheckId(long checkId) {
        return resultRepository.getResultsByCheckId(checkId);
   }
   public List<Result> getResultByTaskId(long taskId) {
        return resultRepository.getResultByTaskId(taskId);
   }

    }


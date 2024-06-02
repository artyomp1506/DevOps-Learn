package com.example.demo.repository;

import com.example.demo.entity.GitlabTaskInfo;
import org.springframework.data.repository.CrudRepository;

public interface GitlabInfoRepository extends CrudRepository<GitlabTaskInfo, Long> {
    GitlabTaskInfo getByTaskId(long id);
}

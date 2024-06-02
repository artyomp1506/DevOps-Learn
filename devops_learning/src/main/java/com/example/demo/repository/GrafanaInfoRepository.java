package com.example.demo.repository;

import com.example.demo.entity.GrafanaInfo;
import org.springframework.data.repository.CrudRepository;

public interface GrafanaInfoRepository extends CrudRepository<GrafanaInfo, Long> {
    GrafanaInfo getByTaskId(long taskId);
}

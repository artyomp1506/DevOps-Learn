package com.example.demo.repository;

import com.example.demo.entity.ApiEntity;
import org.springframework.data.repository.CrudRepository;

public interface ApiTaskRepository extends CrudRepository<ApiEntity, Long> {
}

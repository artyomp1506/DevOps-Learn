package com.example.demo.repository;

import com.example.demo.entity.AnsiblePlaybookEntity;
import org.springframework.data.repository.CrudRepository;

public interface AnsibleRepository extends CrudRepository<AnsiblePlaybookEntity, Long> {
}

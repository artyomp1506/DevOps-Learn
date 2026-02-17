package com.example.demo.repository;

import com.example.demo.entity.DockerTaskEntity;
import org.springframework.data.repository.CrudRepository;

public interface IDockerRepository extends CrudRepository<DockerTaskEntity, Long> {
}

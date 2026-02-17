package com.example.demo.repository;

import com.example.demo.entity.TerraformConfiguration;
import org.springframework.data.repository.CrudRepository;

public interface ITerraformRepository extends CrudRepository<TerraformConfiguration, Long> {

}

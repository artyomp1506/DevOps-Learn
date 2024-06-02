package com.example.demo.repository;

import com.example.demo.entity.check_results.Check;
import org.springframework.data.repository.CrudRepository;

public interface CheckerRepository extends CrudRepository<Check, Long> {
}

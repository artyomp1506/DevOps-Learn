package com.example.demo.repository;

import com.example.demo.entity.task.VirtualMachine;
import org.springframework.data.repository.CrudRepository;

public interface VirtualMachineRepository extends CrudRepository<VirtualMachine, Long> {
}

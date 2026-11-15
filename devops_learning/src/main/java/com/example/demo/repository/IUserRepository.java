package com.example.demo.repository;

import com.example.demo.entity.user.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IUserRepository extends CrudRepository<User, Long> {
    User findByUsername(String username);
    boolean existsByUsername(String username);
    List<User> findAll();
    Optional<User> findById(Long id);
}
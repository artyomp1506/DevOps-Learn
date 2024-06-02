package com.example.demo.entity.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "my_user")
public class User {
    @Id
    @GeneratedValue
    private Long id;
   @Column(unique = true)
    private String username;
    private String password;
    private Role role;
}


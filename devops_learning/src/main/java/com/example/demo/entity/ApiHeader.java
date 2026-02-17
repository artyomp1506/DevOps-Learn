package com.example.demo.checker;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Entity
@AllArgsConstructor
@Getter
public class ApiHeader {
    private long id;
    private String title;
    private String key;
    private String value;
    private boolean inUserInput;
}

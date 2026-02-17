package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ApiHeader {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String title;
    private String key;
    @Column(length = 2000)
    private String value;
    private boolean inUserInput;
    public ApiHeader(String title, String key, String value, boolean inUserInput) {
        this.title = title;
        this.key = key;
        this.value = value;
        this.inUserInput = inUserInput;
    }


}

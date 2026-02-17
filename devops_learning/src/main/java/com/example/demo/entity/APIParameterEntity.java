package com.example.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class APIParameterEntity {
    @Id
    @GeneratedValue()
    private long id;
    private String title;
    private String key;
    private String value;
    private boolean inUserInput;

    public APIParameterEntity(String key, String value, String title, boolean inUserInput) {
        this.key = key;
        this.value = value;
        this.inUserInput = inUserInput;
        this.title = title;
    }




}

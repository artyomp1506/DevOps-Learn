package com.example.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class ApiResponseCheck {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String key;
    private String value;
    private ApiCheckerType type;
    private float score;
    private String failMessage;

    public ApiResponseCheck(String key, String value, ApiCheckerType type, float score, String failMessage) {
        this.key = key;
        this.value = value;
        this.type = type;
        this.score = score;
        this.failMessage = failMessage;
    }


}

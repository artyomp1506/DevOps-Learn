package com.example.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
@AllArgsConstructor
@Getter
@Entity
public class ApiEntity {
    private String name;
    private String method;
    private String url;
    @OneToMany()
    private List<ApiHeader> headers;
    @OneToMany()
    private List<ApiResponseCheck> checks;

}

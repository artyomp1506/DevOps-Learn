package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class ApiEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String name;
    private String method;
    private String url;
    private String body;
    @OneToMany()
    private List<ApiHeader> headers;
    @OneToMany()
    private List<ApiResponseCheck> checks;
    @OneToMany()
    private List<APIParameterEntity> parameters;

    public ApiEntity(String name, String method, String url, String body, List<ApiHeader> headers, List<ApiResponseCheck> checks, List<APIParameterEntity> parameters) {
        this.name = name;
        this.method = method;
        this.url = url;
        this.body = body;
        this.headers = headers;
        this.checks = checks;
        this.parameters = parameters;
    }
}

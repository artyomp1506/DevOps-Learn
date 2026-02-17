package com.example.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class TerraformConfiguration {
    @Id
    @GeneratedValue
    public int id;
    public String name;
    public String filePath;
    public  TerraformConfiguration(String name, String filePath) {
        this.name = name;
        this.filePath = filePath;
    }
    public TerraformConfiguration() {

    }
}

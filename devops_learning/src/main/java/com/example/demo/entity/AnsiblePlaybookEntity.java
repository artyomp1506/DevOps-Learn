package com.example.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class AnsiblePlaybookEntity {
    @Id
    @GeneratedValue()
    public long id;

    public AnsiblePlaybookEntity(String archivePath, String mainRoleName, List<String> requirements, String ansibleUser) {
        this.archivePath = archivePath;
        this.mainRoleName = mainRoleName;
        this.requirements = requirements;
        this.ansibleUser = ansibleUser;
    }

    public String name;
    public String archivePath;
    public String mainRoleName;
    public List<String> requirements;
    private String ansibleUser;
    private String hostFileName;
}

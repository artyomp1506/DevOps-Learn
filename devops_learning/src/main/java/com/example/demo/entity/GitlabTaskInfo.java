package com.example.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

@Entity
@NoArgsConstructor
@Getter
@AllArgsConstructor
public class GitlabTaskInfo {
    @Id
    @GeneratedValue
    private long id;
    private String gitlabDomain;
    private boolean httpsEnabled;
    private String projectName;
    private String branchName;
    private long taskId;
    @Autowired
    public GitlabTaskInfo(String gitlabDomain, boolean httpsEnabled, String projectName, String branchName, long taskId) {
        this.gitlabDomain = gitlabDomain;
        this.httpsEnabled = httpsEnabled;
        this.projectName = projectName;
        this.branchName = branchName;
        this.taskId = taskId;
    }
}

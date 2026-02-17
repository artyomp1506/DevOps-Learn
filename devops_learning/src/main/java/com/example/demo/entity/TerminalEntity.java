package com.example.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;

@Entity
@AllArgsConstructor
public class TerminalEntity {
    @Id
    @GeneratedValue
    private long id;
    private long taskId;
    private String[] sshKeyPaths;
    private String link;
}

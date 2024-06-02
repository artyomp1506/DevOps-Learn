package com.example.demo.entity.task;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Getter
@NoArgsConstructor

public class VirtualMachine {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String serviceId;

    public void setExternalIp(String externalIp) {
        this.externalIp = externalIp;
    }

    private String externalIp;
    private String internalIp;
    @Column(name = "task_id")
    private long task_id;

    public VirtualMachine(String serviceId, String externalIp, String internalIp, long taskId) {
        this.serviceId = serviceId;
        this.externalIp = externalIp;
        this.internalIp = internalIp;
        task_id = taskId;
    }
}

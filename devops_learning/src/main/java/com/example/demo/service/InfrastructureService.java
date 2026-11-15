package com.example.demo.service;


import com.example.demo.dto.InfrastructureServiceDto;

import java.io.IOException;

public class InfrastructureService {
    private AnsibleService ansibleService;
    private TerraformService terraformService;
    private TaskService taskService;
    public InfrastructureServiceDto run(long id) {
        var task = taskService.getById(id);
        new Thread(() -> {
            for (var config: task.getAnsibleChecks()) {
                try {
                    ansibleService.runPlaybook(config.id, id);

                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
                ).start();
        //todo: Возврат
    }
}

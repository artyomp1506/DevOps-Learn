package com.example.demo.service;

import com.example.demo.entity.check_results.Check;
import com.example.demo.entity.check_results.Result;
import com.example.demo.entity.check_results.State;
import com.example.demo.entity.task.Task;
import com.example.demo.repository.AnsibleRepository;
import com.example.demo.repository.CheckerRepository;
import com.example.demo.repository.IResultRepository;
import com.example.demo.repository.ITaskRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class AnsibleCheckService {
    private ITaskRepository taskRepository;
    private CheckerRepository checkerRepository;
    private IResultRepository resultRepository;
    private AnsibleService ansibleService;

    public AnsibleCheckService(ITaskRepository taskRepository, CheckerRepository checkerRepository, IResultRepository resultRepository, AnsibleService ansibleService) {
        this.taskRepository = taskRepository;
        this.checkerRepository = checkerRepository;
        this.resultRepository = resultRepository;
        this.ansibleService = ansibleService;
    }

    public Check checkTask(long checkId, long taskId) throws IOException, InterruptedException {
        var task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NoSuchElementException("Задача не найдена с id: " + taskId));
        var check = new Check(checkId);
        checkerRepository.save(check);
        List<Result> results = new ArrayList<>();
        new Thread(() -> {
            String output;
            try {
                output = ansibleService.runPlaybook(checkId, taskId).getOutput();
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
            for (var line : output.split("\n")) {
                if (line.contains("fail"))
                    results.add(new Result(task, line, State.Wrong, check));
            }
            if (results.isEmpty()) {
                results.add(new Result(task, "Все проверки пройдены", State.Correct, check));
            }
            resultRepository.saveAll(results);
        }).start();
        return check;
    }
}
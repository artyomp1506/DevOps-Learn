package com.example.demo.service;

import com.example.demo.entity.check_results.Check;
import com.example.demo.entity.check_results.Result;
import com.example.demo.entity.check_results.State;
import com.example.demo.repository.AnsibleRepository;
import com.example.demo.repository.CheckerRepository;
import com.example.demo.repository.IResultRepository;
import com.example.demo.repository.ITaskRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class AnsibleCheckService {
    private ITaskRepository taskRepository;
    private CheckerRepository checkerRepository;
    private IResultRepository resultRepository;
    private AnsibleService ansibleService;
    public Check checkTask(long id, long taskId) {
    var check = new Check(id);
    var task = taskRepository.findById(id).get();
    checkerRepository.save(check);
    List<Result> results = new ArrayList<>();
    new Thread(()-> {
        String output = null;
        try {
            output = ansibleService.runPlaybook(id, taskId).getOutput();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
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
        ;
    });
    return check;
    }
}

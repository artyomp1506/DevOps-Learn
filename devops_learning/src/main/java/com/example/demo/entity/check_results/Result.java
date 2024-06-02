package com.example.demo.entity.check_results;

import com.example.demo.entity.task.Task;
import jakarta.persistence.*;
import org.springframework.beans.factory.annotation.Autowired;

@Entity
public class Result implements IResult{
    @Id
    @GeneratedValue
    private long id;

    public long getId() {
        return id;
    }

    @ManyToOne
    private Task task;
    @Column(length = 1000)
    private String description;
    private State state;
    @ManyToOne
    private Check check;
@Autowired
    public Result(Task task, String description, State state, Check check) {
        this.task = task;
        this.description = description;
        this.state = state;
        this.check = check;
}
    private Result()
    {

    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }




    public Check getCheck() {
        return check;
    }

    public void setCheck(Check check) {
        this.check = check;
    }
}

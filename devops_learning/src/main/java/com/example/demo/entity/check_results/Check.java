package com.example.demo.entity.check_results;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;

@Entity
@Getter
@Table(name = "task_check")
public class Check {
    @Id
    @GeneratedValue
    private long id;
    private final Date date;
    @Column(columnDefinition = " bigint default 0")
    public  long taskId;
    public Check(long taskId)
    {
        date = Calendar.getInstance().getTime();
        this.taskId = taskId;
    }
    private Check()
    {
        date = new Date();
    }

}

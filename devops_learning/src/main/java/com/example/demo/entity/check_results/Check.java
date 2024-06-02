package com.example.demo.entity.check_results;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
    public Check()
    {
        date = Calendar.getInstance().getTime();
    }

}

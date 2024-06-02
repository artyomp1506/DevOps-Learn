package com.example.demo.dto;

import com.example.demo.entity.check_results.State;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ResultResponseDto {
    private long id;
    private String description;
    private State state;
    private long checkId;
}

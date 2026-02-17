package com.example.demo.service;

import com.example.demo.entity.AnsiblePlaybookEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class AnsibleServiceOutput
{
    private AnsiblePlaybookEntity entity;
    private String output;
}

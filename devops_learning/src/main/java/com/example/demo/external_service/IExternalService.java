package com.example.demo.external_service;

import com.example.demo.entity.check_results.IResult;

public interface IExternalService  {
    void check();
    IResult getResult();

}

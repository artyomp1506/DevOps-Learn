package com.example.demo.external_service;

import com.example.demo.entity.check_results.IResult;
import org.apache.commons.lang3.NotImplementedException;

public class GitlabService implements IExternalService {
    @Override
    public IResult getResult() {
    throw new NotImplementedException();
    }

    @Override
    public void check() {
    throw new NotImplementedException();
    }
}

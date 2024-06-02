package com.example.demo.checker;


import com.example.demo.entity.check_results.Result;

import java.util.List;

public interface IChecker<T extends ICheckerData> {
    List<Result> check(T data);
}

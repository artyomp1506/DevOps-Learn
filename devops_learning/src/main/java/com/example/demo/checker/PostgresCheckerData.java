package com.example.demo.checker;

import java.util.List;

public class PostgresCheckerData implements IPostgresCheckerData {


    private List<String> queries;
    private List<String> data;

    public PostgresCheckerData(List<String> queries, List<String> data) {
        this.queries = queries;
        this.data = data;
    }
    public List<String> getQueries() {
        return queries;
    }

    public List<String> getData() {
        return data;
    }
}

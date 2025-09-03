package com.chinmay.bfh.http;

public class SubmitRequest {
    private String finalQuery;

    public SubmitRequest(String finalQuery) {
        this.finalQuery = finalQuery;
    }

    public String getFinalQuery() {
        return finalQuery;
    }

    public void setFinalQuery(String finalQuery) {
        this.finalQuery = finalQuery;
    }
}

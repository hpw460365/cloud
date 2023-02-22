package com.example.demo.controller.bean;


import com.fasterxml.jackson.annotation.JsonInclude;

public class RequestInfo {

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String username;
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

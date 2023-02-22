package com.example.demo.controller;

import org.springframework.web.bind.annotation.*;

@RestController
public class DemoController {


    @PostMapping("/getusername")
    public String getName(@RequestBody String body){
        System.out.println(body);
        return "username";
    }

}

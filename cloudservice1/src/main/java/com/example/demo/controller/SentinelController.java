package com.example.demo.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("sentinel")
public class SentinelController {

    @SentinelResource(value = "sayHello", blockHandler = "sayHelloExceptionHandler")
    @GetMapping("/hello")
    public String hello(@RequestParam("name") String name) {
        return "hello";
    }


    @SentinelResource(value = "circuitBreaker", fallback = "circuitBreakerFallback", blockHandler = "sayHelloExceptionHandler")
    @GetMapping("/circuitBreaker")
    public String circuitBreaker(@RequestParam("name") String name) {
        return "circuitBreaker";
    }


    public String circuitBreakerFallback(String name) {
        return "服务异常，熔断降级, 请稍后重试!";
    }


    public String sayHelloExceptionHandler(String name, BlockException ex) {
        return "访问过快，限流降级, 请稍后重试!";
    }


}
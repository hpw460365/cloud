package com.example.demo.controller;

import ch.qos.logback.classic.pattern.MessageConverter;
import com.alibaba.fastjson.JSONObject;
import com.example.demo.controller.bean.RequestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;


@Controller
public class DemoController {

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/getuserinfo")
    @ResponseBody
    public String getName() {

        RequestInfo info = new RequestInfo();
        info.setPassword("password");
        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_XML);
        HttpEntity<RequestInfo> entity = new HttpEntity<RequestInfo>(info, header);
        String username = restTemplate.postForObject("http://cloud-service-2/getusername", entity, String.class);
        return username;
    }

    @GetMapping("/gettestname")
    @ResponseBody
    public String getTestName(){
        return "testNam16";
    }


    @GetMapping("/testsentinel")
    @ResponseBody
    public String testsentinel(){
        return "testsentinel";
    }

}

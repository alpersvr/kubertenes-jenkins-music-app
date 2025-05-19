package com.example.simpleapp;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/")
    public String sayHello() {
        return "Merhaba! Spring Boot uygulamam Kubernetes uzerinde calisiyor!";
    }
} 
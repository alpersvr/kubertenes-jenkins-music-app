package com.example.simpleapp;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HelloController {

    @GetMapping("/")
    public String sayHello(Model model) {
        model.addAttribute("message", "Merhaba! Spring Boot uygulamam Kubernetes üzerinde çalışıyor!");
        return "index";
    }
}
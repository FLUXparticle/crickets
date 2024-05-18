package com.example.crickets.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/app")
public class AppController {

    @GetMapping(value = "/{path:[^.]*}")
    public String redirectApp() {
        return "forward:/app/index.html";
    }

}

package com.example.crickets.controller;

import org.springframework.stereotype.*;
import org.springframework.ui.*;
import org.springframework.web.bind.annotation.*;

@Controller
public class ProfileController {

    @GetMapping("/")
    public String getProfile(Model model) {
        model.addAttribute("username", "Unbekannt");
        model.addAttribute("subscriberCount", "unbekannt");
        return "profile";
    }

}

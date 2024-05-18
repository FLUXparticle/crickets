package com.example.crickets.controller;

import com.example.crickets.data.*;
import com.example.crickets.service.*;
import org.springframework.security.core.*;
import org.springframework.stereotype.*;
import org.springframework.ui.*;
import org.springframework.web.bind.annotation.*;

@Controller
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/")
    public String getProfile(Authentication authentication, Model model) {
        String username = authentication.getName();

        User user = profileService.getUser(null, username);
        long subscriberCount = profileService.getSubscriberCount(user.getId());

        model.addAttribute("username", user.getUsername());
        model.addAttribute("subscriberCount", subscriberCount);

        return "profile";
    }

    @PostMapping("/subscribe")
    public String subscribe(Authentication authentication, @RequestParam String server, @RequestParam String creatorName) {
        String subscriberName = authentication.getName();
        profileService.subscribe(server, creatorName, subscriberName);

        return "redirect:/";
    }

}

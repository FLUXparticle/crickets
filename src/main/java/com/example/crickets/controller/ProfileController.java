package com.example.crickets.controller;

import com.example.crickets.data.*;
import com.example.crickets.service.*;
import org.springframework.security.core.*;
import org.springframework.stereotype.*;
import org.springframework.ui.*;
import org.springframework.validation.*;
import org.springframework.validation.annotation.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.*;

import java.util.*;

@Controller
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @ModelAttribute
    public void addAttributes(Authentication authentication, Model model) {
        String username = authentication.getName();

        User user = profileService.getUser(null, username);
        long subscriberCount = profileService.getSubscriberCount(user.getId());

        model.addAttribute("username", user.getUsername());
        model.addAttribute("subscriberCount", subscriberCount);
    }

    @GetMapping("/")
    public String getProfile(Authentication authentication, Model model) {
        model.addAttribute("subscribeForm", new SubscribeForm());

        System.out.println("model = " + model.asMap());

        return "profile";
    }

    @PostMapping("/subscribe")
    public String subscribe(
            Authentication authentication,
            @Validated SubscribeForm subscribeForm,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        System.out.println("subscribeForm = " + subscribeForm);
        System.out.println("model = " + model.asMap());

        if (result.hasErrors()) {
            List<String> errors = result.getAllErrors().stream()
                    .map(ObjectError::getDefaultMessage)
                    .toList();
            model.addAttribute("errors", errors);
            return "profile";
        }

        String subscriberName = authentication.getName();
        String server = subscribeForm.getServer();
        String creatorName = subscribeForm.getCreatorName();
        if (!profileService.subscribe(server, creatorName, subscriberName)) {
            model.addAttribute("errors", List.of(String.format("Creator '%s' existiert nicht.", creatorName)));
            return "profile";
        }

        redirectAttributes.addFlashAttribute("successes", String.format("User '%s' erfolgreich abonniert.", creatorName));
        return "redirect:/";
    }

}

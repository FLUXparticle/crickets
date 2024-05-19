package com.example.crickets.controller;

import com.example.crickets.data.*;
import com.example.crickets.service.*;
import org.springframework.http.*;
import org.springframework.security.core.*;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.*;

import java.util.*;

@RestController
@RequestMapping("/api")
public class TimelineController {

    private final TimelineService timelineService;

    public TimelineController(TimelineService timelineService) {
        this.timelineService = timelineService;
    }

    @ResponseBody
    @GetMapping("/username")
    public Map<String, String> username(Authentication authentication) {
        String username = authentication.getName();
        return Map.of("username", username);
    }

    @GetMapping(value = "/posts", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Post> showTimeline(Authentication authentication) {
        String subscriberName = authentication.getName();
        return timelineService.getTimelinePostUpdates(subscriberName);
    }

    @PostMapping("/post")
    public void createPost(Authentication authentication, @RequestBody Map<String, String> payload) {
        // Erstelle einen neuen Post
        String content = payload.get("content");
        String creatorName = authentication.getName();
        timelineService.createPost(creatorName, content);
    }

    @GetMapping(value = "/search", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Post> subscribeUser(String query) {
        return timelineService.searchPosts(query);
    }

}

package com.example.crickets.repository;

import com.example.crickets.data.*;
import org.springframework.data.mongodb.repository.*;
import reactor.core.publisher.*;

public interface PostReactiveRepository extends ReactiveMongoRepository<Post, String> {
    Flux<Post> findByContentContains(String query);
}

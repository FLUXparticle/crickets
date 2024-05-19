package com.example.crickets.repository;

import com.example.crickets.data.*;
import org.springframework.data.mongodb.core.*;
import org.springframework.stereotype.*;
import reactor.core.publisher.*;

import java.util.*;

import static org.springframework.data.mongodb.core.query.Criteria.*;

@Repository
public class PostUpdateRepository {

    private final ReactiveMongoTemplate reactiveMongoTemplate;

    public PostUpdateRepository(ReactiveMongoTemplate reactiveMongoTemplate) {
        this.reactiveMongoTemplate = reactiveMongoTemplate;
    }

    public Flux<Post> streamByCreatorIn(List<User> creators) {
        return reactiveMongoTemplate.changeStream(Post.class)
                .filter(where("creator").in(creators))
                .listen()
                .mapNotNull(ChangeStreamEvent::getBody);
    }

}

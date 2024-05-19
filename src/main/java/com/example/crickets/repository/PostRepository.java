package com.example.crickets.repository;

import com.example.crickets.data.*;
import org.springframework.data.mongodb.repository.*;
import org.springframework.stereotype.*;

import java.util.*;

@Repository
public interface PostRepository extends MongoRepository<Post, String> {
    List<Post> findByCreatorInOrderByCreatedAtDesc(List<User> creators);
}

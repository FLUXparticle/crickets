package com.example.crickets.repository;

import com.example.crickets.data.*;
import org.springframework.data.mongodb.repository.*;

import java.util.*;

public interface UserRepository extends MongoRepository<User, String> {
    User findByServerAndUsername(String server, String username);
    List<User> findByUsernameIn(List<String> usernames);
}

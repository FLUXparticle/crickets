package com.example.crickets.repository;

import com.example.crickets.data.*;
import org.springframework.data.mongodb.repository.*;

import java.util.*;

public interface SubscriptionRepository extends MongoRepository<Subscription, String> {
    long countByCreatorId(String creatorId);
    List<Subscription> findBySubscriber(User subscriber);
}

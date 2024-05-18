package com.example.crickets.repository;

import com.example.crickets.data.*;
import org.springframework.data.mongodb.repository.*;

public interface SubscriptionRepository extends MongoRepository<Subscription, String> {
    long countByCreatorId(String creatorId);
}

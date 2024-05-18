package com.example.crickets.service;

import com.example.crickets.data.*;
import com.example.crickets.repository.*;
import org.springframework.stereotype.*;

@Service
public class ProfileService {

    private final UserRepository userRepository;

    private final SubscriptionRepository subscriptionRepository;

    public ProfileService(UserRepository userRepository, SubscriptionRepository subscriptionRepository) {
        this.userRepository = userRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    public User getUser(String username) {
        return getUser(null, username);
    }

    public User getUser(String server, String username) {
        User user = userRepository.findByServerAndUsername(server, username);

        if (user == null) {
            user = new User(null, username);
            userRepository.save(user);
        }

        return user;
    }

    public long getSubscriberCount(String userId) {
        return subscriptionRepository.countByCreatorId(userId);
    }

    public void subscribe(String creatorName, String subscriberName) {
        User creator = getUser(null , creatorName);
        User subscriber = getUser(null, subscriberName);

        Subscription subscription = new Subscription(creator, subscriber);
        subscriptionRepository.save(subscription);
    }

}

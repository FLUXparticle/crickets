package com.example.crickets.data;

import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.*;

@Document(collection = "subscriptions")
public class Subscription {
    @Id
    private String id;
    @DBRef
    private User creator;
    @DBRef
    private User subscriber;

    // Konstruktoren, Getter und Setter
    public Subscription() {
        // empty
    }

    public Subscription(User creator, User subscriber) {
        this.creator = creator;
        this.subscriber = subscriber;
    }

    @Override
    public String toString() {
        return "Subscription{" +
                ", creator=" + creator +
                ", subscriber=" + subscriber +
                '}';
    }

    public User getSubscriber() {
        return subscriber;
    }

    public User getCreator() {
        return creator;
    }

}

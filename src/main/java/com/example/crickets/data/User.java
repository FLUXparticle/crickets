package com.example.crickets.data;

import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.*;

import java.io.*;

@Document(collection = "users")
public class User implements Serializable {
    @Id
    private String id;
    private String server;
    private String username;

    // Konstruktoren, Getter und Setter
    public User() {
        // empty
    }

    public User(String server, String username) {
        this.server = server;
        this.username = username;
    }

    public String getId() {
        return id;
    }

    public String getServer() {
        return server;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public String toString() {
        return "User{" +
                "server='" + server + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}

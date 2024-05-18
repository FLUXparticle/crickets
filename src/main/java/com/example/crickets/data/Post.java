package com.example.crickets.data;

import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.*;

import java.io.*;
import java.util.*;

@Document(collection = "posts")
public class Post implements Serializable {

    @Id
    private String id;
    private String content;
    @DBRef
    private User creator;
    private Date createdAt;

    // Konstruktoren, Getter und Setter
    public Post() {
        // empty
    }

    public Post(String content, User creator, Date createdAt) {
        this.content = content;
        this.creator = creator;
        this.createdAt = createdAt;
    }

    public String getContent() {
        return content;
    }

    public User getCreator() {
        return creator;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "Post{" +
                "content='" + content + '\'' +
                ", creator=" + creator +
                ", createdAt=" + createdAt +
                '}';
    }

}

package com.example.crickets.controller;

import jakarta.validation.constraints.*;

public class SubscribeForm {

    private String server;

    @NotBlank(message = "Username darf nicht leer sein")
    private String creatorName;

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    @Override
    public String toString() {
        return "SubscribeForm{" +
                "server='" + server + '\'' +
                ", creatorName='" + creatorName + '\'' +
                '}';
    }

}

package com.campuschat.model;

/**
 * Simple user model.
 *
 * Passwords are stored as plain text only for this MVP.
 * A production system should use password hashing.
 */
public class User {
    private final String username;
    private final String password;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}

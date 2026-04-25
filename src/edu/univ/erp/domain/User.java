package edu.univ.erp.domain;

public class User {
    private int userId;
    private String username;
    private String role;
    private String passwordHash;

    public User(int userId, String username, String role, String passwordHash) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.passwordHash = passwordHash;
    }

    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
    public String getPasswordHash() { return passwordHash; }
}
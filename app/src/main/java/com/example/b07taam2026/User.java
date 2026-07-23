package com.example.b07taam2026;

public class User {

    private String email;
    private String username;
    private String role;

    // user constructors
    public User() {}

    public User(String email, String username, String role) {
        this.email = email;
        this.username = username;
        this.role = role;
    }

    // standard getters and setters
    public String getEmail() { 
        return email; 
    }
    public void setEmail(String email) { 
        this.email = email; 
    }
    public String getUsername() { 
        return username; 
    }
    public void setUsername(String username) { 
        this.username = username; 
    }
    public String getRole() { 
        return role; 
    }
    public void setRole(String role) { 
        this.role = role; 
    }
    public boolean isAdmin() {
        return "admin".equals(role);
    }
}

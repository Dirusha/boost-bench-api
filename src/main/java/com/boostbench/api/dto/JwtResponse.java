package com.boostbench.api.dto;

import java.util.Set;

public class JwtResponse {
    private String token;
    private Long id;
    private String username;
    private Set<String> roles;
    private Set<String> permissions; // New field for permissions

    // Constructor
    public JwtResponse(String token, Long id, String username, Set<String> roles, Set<String> permissions) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.roles = roles;
        this.permissions = permissions;
    }

    // Getters and setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<String> permissions) {
        this.permissions = permissions;
    }
}
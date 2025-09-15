package com.boostbench.api.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.Set;

public class UpdateUserDto {
    @NotBlank
    private String username;
    private String password;
    private Set<Long> roleIds;

    // Getters and setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<Long> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(Set<Long> roleIds) {
        this.roleIds = roleIds;
    }
}
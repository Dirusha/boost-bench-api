package com.boostbench.api.dto;

import java.util.Set;

public class UpdateRolesDto {
    private Set<Long> roleIds;

    // Getters and setters
    public Set<Long> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(Set<Long> roleIds) {
        this.roleIds = roleIds;
    }
}
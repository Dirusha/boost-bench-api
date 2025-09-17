package com.boostbench.api.controller;

import com.boostbench.api.dto.ApiResponse;
import com.boostbench.api.dto.RoleDTO;
import com.boostbench.api.entity.Role;
import com.boostbench.api.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ResponseEntity<ApiResponse<List<Role>>> getAllRoles() {
        List<Role> roles = roleService.getAllRoles();
        return ResponseEntity.ok(new ApiResponse<>("Roles retrieved successfully", roles));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ResponseEntity<ApiResponse<Role>> getRoleById(@PathVariable Long id) {
        Role role = roleService.getRoleById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        return ResponseEntity.ok(new ApiResponse<>("Role retrieved successfully", role));
    }

    // Create role using DTO (recommended approach)
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ResponseEntity<ApiResponse<Role>> createRole(@Valid @RequestBody RoleDTO roleDTO) {
        Role createdRole = roleService.createRole(roleDTO);
        return ResponseEntity.ok(new ApiResponse<>("Role created successfully", createdRole));
    }

    // Create role using entity (for backward compatibility)
    @PostMapping("/entity")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ResponseEntity<ApiResponse<Role>> createRoleEntity(@Valid @RequestBody Role role) {
        Role createdRole = roleService.createRole(role);
        return ResponseEntity.ok(new ApiResponse<>("Role created successfully", createdRole));
    }

    // Update role using DTO (recommended approach)
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ResponseEntity<ApiResponse<Role>> updateRole(@PathVariable Long id, @Valid @RequestBody RoleDTO roleDTO) {
        Role updatedRole = roleService.updateRole(id, roleDTO);
        return ResponseEntity.ok(new ApiResponse<>("Role updated successfully", updatedRole));
    }

    // Update role using entity (for backward compatibility)
    @PutMapping("/{id}/entity")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ResponseEntity<ApiResponse<Role>> updateRoleEntity(@PathVariable Long id, @Valid @RequestBody Role role) {
        Role updatedRole = roleService.updateRole(id, role);
        return ResponseEntity.ok(new ApiResponse<>("Role updated successfully", updatedRole));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ResponseEntity.ok(new ApiResponse<>("Role deleted successfully", null));
    }

    // Individual permission management endpoints
    @PostMapping("/{roleId}/permissions/{permissionId}")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ResponseEntity<ApiResponse<Role>> addPermissionToRole(@PathVariable Long roleId, @PathVariable Long permissionId) {
        Role updatedRole = roleService.addPermissionToRole(roleId, permissionId);
        return ResponseEntity.ok(new ApiResponse<>("Permission added to role successfully", updatedRole));
    }

    @DeleteMapping("/{roleId}/permissions/{permissionId}")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ResponseEntity<ApiResponse<Role>> removePermissionFromRole(@PathVariable Long roleId, @PathVariable Long permissionId) {
        Role updatedRole = roleService.removePermissionFromRole(roleId, permissionId);
        return ResponseEntity.ok(new ApiResponse<>("Permission removed from role successfully", updatedRole));
    }
}
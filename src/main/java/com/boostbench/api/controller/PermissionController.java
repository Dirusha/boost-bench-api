package com.boostbench.api.controller;

import com.boostbench.api.dto.ApiResponse;
import com.boostbench.api.entity.Permission;
import com.boostbench.api.service.PermissionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PERMISSION_MANAGE')")
    public ResponseEntity<ApiResponse<List<Permission>>> getAllPermissions() {
        List<Permission> permissions = permissionService.getAllPermissions();
        return ResponseEntity.ok(new ApiResponse<>("Permissions retrieved successfully", permissions));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSION_MANAGE')")
    public ResponseEntity<ApiResponse<Permission>> getPermissionById(@PathVariable Long id) {
        Permission permission = permissionService.getPermissionById(id)
                .orElseThrow(() -> new RuntimeException("Permission not found"));
        return ResponseEntity.ok(new ApiResponse<>("Permission retrieved successfully", permission));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PERMISSION_MANAGE')")
    public ResponseEntity<ApiResponse<Permission>> createPermission(@Valid @RequestBody Permission permission) {
        Permission createdPermission = permissionService.createPermission(permission);
        return ResponseEntity.ok(new ApiResponse<>("Permission created successfully", createdPermission));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSION_MANAGE')")
    public ResponseEntity<ApiResponse<Permission>> updatePermission(@PathVariable Long id, @Valid @RequestBody Permission permission) {
        Permission updatedPermission = permissionService.updatePermission(id, permission);
        return ResponseEntity.ok(new ApiResponse<>("Permission updated successfully", updatedPermission));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSION_MANAGE')")
    public ResponseEntity<ApiResponse<Void>> deletePermission(@PathVariable Long id) {
        permissionService.deletePermission(id);
        return ResponseEntity.ok(new ApiResponse<>("Permission deleted successfully", null));
    }
}
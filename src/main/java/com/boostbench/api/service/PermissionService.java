package com.boostbench.api.service;

import com.boostbench.api.entity.Permission;
import com.boostbench.api.repository.PermissionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PermissionService {

    private final PermissionRepository permissionRepository;

    public PermissionService(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    public List<Permission> getAllPermissions() {
        return permissionRepository.findAll();
    }

    public Optional<Permission> getPermissionById(Long id) {
        return permissionRepository.findById(id);
    }

    public Permission createPermission(Permission permission) {
        if (permissionRepository.findByName(permission.getName()).isPresent()) {
            throw new RuntimeException("Permission already exists");
        }
        return permissionRepository.save(permission);
    }

    public Permission updatePermission(Long id, Permission updatedPermission) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Permission not found"));
        if (!permission.getName().equals(updatedPermission.getName()) &&
                permissionRepository.findByName(updatedPermission.getName()).isPresent()) {
            throw new RuntimeException("Permission name already exists");
        }
        permission.setName(updatedPermission.getName());
        return permissionRepository.save(permission);
    }

    public void deletePermission(Long id) {
        if (!permissionRepository.existsById(id)) {
            throw new RuntimeException("Permission not found");
        }
        permissionRepository.deleteById(id);
    }
}
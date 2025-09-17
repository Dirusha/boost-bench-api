package com.boostbench.api.service;

import com.boostbench.api.dto.RoleDTO;
import com.boostbench.api.entity.Permission;
import com.boostbench.api.entity.Role;
import com.boostbench.api.repository.PermissionRepository;
import com.boostbench.api.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public RoleService(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public Optional<Role> getRoleById(Long id) {
        return roleRepository.findById(id);
    }

    // Original method for backward compatibility
    public Role createRole(Role role) {
        if (roleRepository.findByName(role.getName()).isPresent()) {
            throw new RuntimeException("Role already exists");
        }

        if (role.getPermissions() != null && !role.getPermissions().isEmpty()) {
            Set<Permission> validatedPermissions = validateAndFetchPermissions(role.getPermissions());
            role.setPermissions(validatedPermissions);
        } else {
            role.setPermissions(new HashSet<>());
        }

        return roleRepository.save(role);
    }

    // New method using DTO for cleaner permission handling
    public Role createRole(RoleDTO roleDTO) {
        if (roleRepository.findByName(roleDTO.getName()).isPresent()) {
            throw new RuntimeException("Role already exists");
        }

        Role role = new Role();
        role.setName(roleDTO.getName());

        // Handle permissions by IDs
        if (roleDTO.getPermissionIds() != null && !roleDTO.getPermissionIds().isEmpty()) {
            Set<Permission> permissions = fetchPermissionsByIds(roleDTO.getPermissionIds());
            role.setPermissions(permissions);
        } else {
            role.setPermissions(new HashSet<>());
        }

        return roleRepository.save(role);
    }

    // Original method for backward compatibility
    public Role updateRole(Long id, Role updatedRole) {
        Role existingRole = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        if (!existingRole.getName().equals(updatedRole.getName()) &&
                roleRepository.findByName(updatedRole.getName()).isPresent()) {
            throw new RuntimeException("Role name already exists");
        }

        existingRole.setName(updatedRole.getName());

        if (updatedRole.getPermissions() != null) {
            Set<Permission> validatedPermissions = validateAndFetchPermissions(updatedRole.getPermissions());
            existingRole.setPermissions(validatedPermissions);
        }

        return roleRepository.save(existingRole);
    }

    // New method using DTO for cleaner permission handling
    public Role updateRole(Long id, RoleDTO roleDTO) {
        Role existingRole = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        if (!existingRole.getName().equals(roleDTO.getName()) &&
                roleRepository.findByName(roleDTO.getName()).isPresent()) {
            throw new RuntimeException("Role name already exists");
        }

        existingRole.setName(roleDTO.getName());

        // Replace all existing permissions with new ones
        if (roleDTO.getPermissionIds() != null) {
            Set<Permission> permissions = fetchPermissionsByIds(roleDTO.getPermissionIds());
            existingRole.setPermissions(permissions);
        } else {
            // If no permissions provided, clear all existing permissions
            existingRole.setPermissions(new HashSet<>());
        }

        return roleRepository.save(existingRole);
    }

    public void deleteRole(Long id) {
        if (!roleRepository.existsById(id)) {
            throw new RuntimeException("Role not found");
        }
        roleRepository.deleteById(id);
    }

    public Role addPermissionToRole(Long roleId, Long permissionId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new RuntimeException("Permission not found"));

        role.getPermissions().add(permission);
        return roleRepository.save(role);
    }

    public Role removePermissionFromRole(Long roleId, Long permissionId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new RuntimeException("Permission not found"));

        role.getPermissions().remove(permission);
        return roleRepository.save(role);
    }

    /**
     * Fetch permissions by their IDs
     */
    private Set<Permission> fetchPermissionsByIds(List<Long> permissionIds) {
        Set<Permission> permissions = new HashSet<>();

        for (Long permissionId : permissionIds) {
            Permission permission = permissionRepository.findById(permissionId)
                    .orElseThrow(() -> new RuntimeException("Permission not found with ID: " + permissionId));
            permissions.add(permission);
        }

        return permissions;
    }

    /**
     * Validates that all provided permissions exist in the database and returns the managed entities
     */
    private Set<Permission> validateAndFetchPermissions(Set<Permission> permissions) {
        Set<Permission> validatedPermissions = new HashSet<>();

        for (Permission permission : permissions) {
            Permission managedPermission;

            if (permission.getId() != null) {
                managedPermission = permissionRepository.findById(permission.getId())
                        .orElseThrow(() -> new RuntimeException("Permission not found with ID: " + permission.getId()));
            } else if (permission.getName() != null) {
                managedPermission = permissionRepository.findByName(permission.getName())
                        .orElseThrow(() -> new RuntimeException("Permission not found with name: " + permission.getName()));
            } else {
                throw new RuntimeException("Permission must have either ID or name specified");
            }

            validatedPermissions.add(managedPermission);
        }

        return validatedPermissions;
    }
}
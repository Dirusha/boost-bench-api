package com.boostbench.api.service;

import com.boostbench.api.entity.Permission;
import com.boostbench.api.entity.Role;
import com.boostbench.api.repository.PermissionRepository;
import com.boostbench.api.repository.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
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

    public Role createRole(Role role) {
        if (roleRepository.findByName(role.getName()).isPresent()) {
            throw new RuntimeException("Role already exists");
        }
        return roleRepository.save(role);
    }

    public Role updateRole(Long id, Role updatedRole) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        if (!role.getName().equals(updatedRole.getName()) &&
                roleRepository.findByName(updatedRole.getName()).isPresent()) {
            throw new RuntimeException("Role name already exists");
        }
        role.setName(updatedRole.getName());
        // Optionally update permissions if provided
        if (updatedRole.getPermissions() != null) {
            Set<Permission> permissions = new HashSet<>();
            for (Permission p : updatedRole.getPermissions()) {
                permissions.add(permissionRepository.findById(p.getId())
                        .orElseThrow(() -> new RuntimeException("Permission not found: " + p.getId())));
            }
            role.setPermissions(permissions);
        }
        return roleRepository.save(role);
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
}
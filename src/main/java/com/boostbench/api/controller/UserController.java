package com.boostbench.api.controller;

import com.boostbench.api.dto.ApiResponse;
import com.boostbench.api.dto.CreateUserDto;
import com.boostbench.api.dto.UpdateRolesDto;
import com.boostbench.api.dto.UpdateUserDto;
import com.boostbench.api.entity.User;
import com.boostbench.api.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(new ApiResponse<>("Users retrieved successfully", users));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        return ResponseEntity.ok(new ApiResponse<>("User retrieved successfully", user));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('USER_CREATE')")
    public ResponseEntity<ApiResponse<User>> createUser(@Valid @RequestBody CreateUserDto createUserDto) {
        User user = userService.createUser(createUserDto);
        return ResponseEntity.ok(new ApiResponse<>("User created successfully", user));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    public ResponseEntity<ApiResponse<User>> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserDto updateUserDto) {
        User updatedUser = userService.updateUser(id, updateUserDto);
        return ResponseEntity.ok(new ApiResponse<>("User updated successfully", updatedUser));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(new ApiResponse<>("User deleted successfully", null));
    }

    @PutMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    public ResponseEntity<ApiResponse<User>> updateUserRoles(@PathVariable Long id, @Valid @RequestBody UpdateRolesDto updateRolesDto) {
        User user = userService.updateUserRoles(id, updateRolesDto.getRoleIds());
        return ResponseEntity.ok(new ApiResponse<>("User roles updated successfully", user));
    }
}
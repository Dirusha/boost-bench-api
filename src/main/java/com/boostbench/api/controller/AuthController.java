package com.boostbench.api.controller;

import com.boostbench.api.dto.ApiResponse;
import com.boostbench.api.dto.JwtResponse;
import com.boostbench.api.dto.LoginDto;
import com.boostbench.api.dto.RegisterDto;
import com.boostbench.api.entity.User;
import com.boostbench.api.service.UserService;
import com.boostbench.api.util.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody LoginDto loginDto) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword()));
        final User userDetails = (User) userService.loadUserByUsername(loginDto.getUsername());
        final String token = jwtUtil.generateToken(userDetails);

        Set<String> roles = userDetails.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.toSet());

        Set<String> permissions = userDetails.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(permission -> permission.getName())
                .collect(Collectors.toSet());

        JwtResponse jwtResponse = new JwtResponse(token, userDetails.getId(), userDetails.getUsername(), roles, permissions);
        return ResponseEntity.ok(jwtResponse);
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@RequestBody RegisterDto registerDto) {
        try {
            userService.registerUser(registerDto);
            return ResponseEntity.ok(new ApiResponse<>("User registered successfully", null));
        } catch (RuntimeException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @PostMapping("/register/admin")
    public ResponseEntity<ApiResponse<Void>> registerAdmin(@RequestBody RegisterDto registerDto) {
        try {
            userService.registerAdminUser(registerDto);
            return ResponseEntity.ok(new ApiResponse<>("Admin user registered successfully", null));
        } catch (RuntimeException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
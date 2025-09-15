package com.boostbench.api.service;

import com.boostbench.api.dto.CreateUserDto;
import com.boostbench.api.dto.RegisterDto;
import com.boostbench.api.dto.UpdateUserDto;
import com.boostbench.api.entity.Role;
import com.boostbench.api.entity.User;
import com.boostbench.api.repository.RoleRepository;
import com.boostbench.api.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public void registerUser(RegisterDto registerDto) {
        if (userRepository.findByUsername(registerDto.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        User user = new User();
        user.setUsername(registerDto.getUsername());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        user.setFullName(registerDto.getFullName());
        user.setWorkEmail(registerDto.getWorkEmail());
        user.setPhoneNumber(registerDto.getPhoneNumber());
        user.setAddress(registerDto.getAddress());
        user.setCity(registerDto.getCity());
        user.setCountry(registerDto.getCountry());
        user.setZipCode(registerDto.getZipCode());
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Role not found"));
        user.setRoles(new HashSet<>() {{ add(userRole); }});
        userRepository.save(user);
    }

    public User registerAdminUser(RegisterDto registerDto) {
        if (userRepository.findByUsername(registerDto.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        User user = new User();
        user.setUsername(registerDto.getUsername());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        user.setFullName(registerDto.getFullName());
        user.setWorkEmail(registerDto.getWorkEmail());
        user.setPhoneNumber(registerDto.getPhoneNumber());
        user.setAddress(registerDto.getAddress());
        user.setCity(registerDto.getCity());
        user.setCountry(registerDto.getCountry());
        user.setZipCode(registerDto.getZipCode());
        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseThrow(() -> new RuntimeException("Role not found"));
        user.setRoles(new HashSet<>() {{ add(adminRole); }});
        return userRepository.save(user);
    }

    public User createUser(CreateUserDto createUserDto) {
        if (userRepository.findByUsername(createUserDto.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        User user = new User();
        user.setUsername(createUserDto.getUsername());
        user.setPassword(passwordEncoder.encode(createUserDto.getPassword()));
        user.setFullName(createUserDto.getFullName());
        user.setWorkEmail(createUserDto.getWorkEmail());
        user.setPhoneNumber(createUserDto.getPhoneNumber());
        user.setAddress(createUserDto.getAddress());
        user.setCity(createUserDto.getCity());
        user.setCountry(createUserDto.getCountry());
        user.setZipCode(createUserDto.getZipCode());
        Set<Role> roles = createUserDto.getRoleIds().stream()
                .map(id -> roleRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Role not found: " + id)))
                .collect(Collectors.toSet());
        user.setRoles(roles);
        return userRepository.save(user);
    }

    public User updateUser(Long id, UpdateUserDto updateUserDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (updateUserDto.getUsername() != null && !user.getUsername().equals(updateUserDto.getUsername())) {
            if (userRepository.findByUsername(updateUserDto.getUsername()).isPresent()) {
                throw new RuntimeException("Username already exists");
            }
            user.setUsername(updateUserDto.getUsername());
        }
        if (updateUserDto.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(updateUserDto.getPassword()));
        }
        if (updateUserDto.getRoleIds() != null) {
            Set<Role> roles = updateUserDto.getRoleIds().stream()
                    .map(roleId -> roleRepository.findById(roleId)
                            .orElseThrow(() -> new RuntimeException("Role not found: " + roleId)))
                    .collect(Collectors.toSet());
            user.setRoles(roles);
        }
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found");
        }
        userRepository.deleteById(id);
    }

    public User updateUserRoles(Long userId, Set<Long> roleIds) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Set<Role> roles = roleIds.stream()
                .map(id -> roleRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Role not found: " + id)))
                .collect(Collectors.toSet());
        user.setRoles(roles);
        return userRepository.save(user);
    }
}
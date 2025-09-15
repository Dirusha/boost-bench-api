package com.boostbench.api;

import com.boostbench.api.entity.Permission;
import com.boostbench.api.entity.Role;
import com.boostbench.api.entity.User;
import com.boostbench.api.repository.PermissionRepository;
import com.boostbench.api.repository.RoleRepository;
import com.boostbench.api.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Set;

@SpringBootApplication
public class ApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiApplication.class, args);
	}

	@Bean
	CommandLineRunner initData(RoleRepository roleRepository, PermissionRepository permissionRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
		return args -> {
			if (roleRepository.findByName("USER").isPresent()) {
				return;
			}

			// Create permissions
			Permission userRead = permissionRepository.save(new Permission("USER_READ"));
			Permission userCreate = permissionRepository.save(new Permission("USER_CREATE"));
			Permission userUpdate = permissionRepository.save(new Permission("USER_UPDATE"));
			Permission roleManage = permissionRepository.save(new Permission("ROLE_MANAGE"));
			Permission permissionManage = permissionRepository.save(new Permission("PERMISSION_MANAGE"));

			Permission productRead = permissionRepository.save(new Permission("PRODUCT_READ"));
			Permission productCreate = permissionRepository.save(new Permission("PRODUCT_CREATE"));
			Permission productUpdate = permissionRepository.save(new Permission("PRODUCT_UPDATE"));
			Permission productDelete = permissionRepository.save(new Permission("PRODUCT_DELETE"));

			Permission categoryRead = permissionRepository.save(new Permission("CATEGORY_READ"));
			Permission categoryCreate = permissionRepository.save(new Permission("CATEGORY_CREATE"));

			Permission tagRead = permissionRepository.save(new Permission("TAG_READ"));
			Permission tagCreate = permissionRepository.save(new Permission("TAG_CREATE"));

			// USER role with basic permission
			Set<Permission> userPermissions = new HashSet<>() {{
				add(userRead);
				add(productRead);
				add(categoryRead);
				add(tagRead);
			}};
			Role userRole = roleRepository.save(new Role("USER", userPermissions));

			// ADMIN role with all permissions
			Set<Permission> adminPermissions = new HashSet<>() {{
				add(userRead);
				add(userCreate);
				add(userUpdate);
				add(roleManage);
				add(permissionManage);
				add(productRead);
				add(productCreate);
				add(productUpdate);
				add(productDelete);
				add(categoryRead);
				add(categoryCreate);
				add(tagRead);
				add(tagCreate);
			}};
			Role adminRole = roleRepository.save(new Role("ADMIN", adminPermissions));

			// Create admin user
			Set<Role> adminRoles = new HashSet<>() {{
				add(adminRole);
			}};
			User admin = new User();
			admin.setUsername("admin");
			admin.setPassword(passwordEncoder.encode("password"));
			admin.setRoles(adminRoles);
			userRepository.save(admin);
		};
	}
}
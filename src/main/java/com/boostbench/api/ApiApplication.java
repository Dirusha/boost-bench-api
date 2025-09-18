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
				return; // Skip initialization if USER role already exists
			}

			// Create all permissions
			Permission userRead = permissionRepository.save(new Permission("USER_READ"));
			Permission userCreate = permissionRepository.save(new Permission("USER_CREATE"));
			Permission userUpdate = permissionRepository.save(new Permission("USER_UPDATE"));
			Permission userDelete = permissionRepository.save(new Permission("USER_DELETE"));

			Permission roleManage = permissionRepository.save(new Permission("ROLE_MANAGE"));

			Permission permissionManage = permissionRepository.save(new Permission("PERMISSION_MANAGE"));
			Permission productRead = permissionRepository.save(new Permission("PRODUCT_READ"));
			Permission productCreate = permissionRepository.save(new Permission("PRODUCT_CREATE"));
			Permission productUpdate = permissionRepository.save(new Permission("PRODUCT_UPDATE"));
			Permission productDelete = permissionRepository.save(new Permission("PRODUCT_DELETE"));
			Permission categoryRead = permissionRepository.save(new Permission("CATEGORY_READ"));
			Permission categoryCreate = permissionRepository.save(new Permission("CATEGORY_CREATE"));
			Permission categoryUpdate = permissionRepository.save(new Permission("CATEGORY_UPDATE"));
			Permission categoryDelete = permissionRepository.save(new Permission("CATEGORY_DELETE"));
			Permission tagRead = permissionRepository.save(new Permission("TAG_READ"));
			Permission tagCreate = permissionRepository.save(new Permission("TAG_CREATE"));
			Permission tagUpdate = permissionRepository.save(new Permission("TAG_UPDATE"));
			Permission tagDelete = permissionRepository.save(new Permission("TAG_DELETE"));
			Permission cartRead = permissionRepository.save(new Permission("CART_READ"));
			Permission cartModify = permissionRepository.save(new Permission("CART_MODIFY"));
			Permission orderCreate = permissionRepository.save(new Permission("ORDER_CREATE"));
			Permission orderReadOwn = permissionRepository.save(new Permission("ORDER_READ_OWN"));
			Permission orderRead = permissionRepository.save(new Permission("ORDER_READ"));
			Permission orderReadAll = permissionRepository.save(new Permission("ORDER_READ_ALL"));
			Permission orderStatusUpdate = permissionRepository.save(new Permission("ORDER_STATUS_UPDATE"));
			Permission orderPay = permissionRepository.save(new Permission("ORDER_PAY"));

			// USER role with read-only and basic operation permissions
			Set<Permission> userPermissions = new HashSet<>() {{
				add(userRead);
				add(productRead);
				add(categoryRead);
				add(tagRead);
				add(cartRead);
				add(cartModify);
				add(orderCreate);
				add(orderReadOwn);
				add(orderPay);
			}};
			Role userRole = roleRepository.save(new Role("USER", userPermissions));

			// ADMIN role with all permissions
			Set<Permission> adminPermissions = new HashSet<>() {{
				add(userRead);
				add(userCreate);
				add(userUpdate);
				add(userDelete);
				add(roleManage);
				add(permissionManage);
				add(productRead);
				add(productCreate);
				add(productUpdate);
				add(productDelete);
				add(categoryRead);
				add(categoryCreate);
				add(categoryUpdate);
				add(categoryDelete);
				add(tagRead);
				add(tagCreate);
				add(tagUpdate);
				add(tagDelete);
				add(cartRead);
				add(cartModify);
				add(orderCreate);
				add(orderReadOwn);
				add(orderRead);
				add(orderReadAll);
				add(orderStatusUpdate);
				add(orderPay);
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
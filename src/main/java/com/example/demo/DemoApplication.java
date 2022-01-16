package com.example.demo;

import com.example.demo.auth.Role;
import com.example.demo.auth.User;
import com.example.demo.auth.UserService;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Bean
	CommandLineRunner run(UserService userService) {
		return args -> {
			// userService.saveUser(new User(null, "test", "1234", null));

			// userService.saveRole(new Role(null, "ROLE_USER"));
			// userService.saveRole(new Role(null, "ROLE_ADMIN"));
			
			// userService.addRoleToUser("test", "ROLE_USER");
			// userService.addRoleToUser("admin", "ROLE_ADMIN");
		};
	}
}

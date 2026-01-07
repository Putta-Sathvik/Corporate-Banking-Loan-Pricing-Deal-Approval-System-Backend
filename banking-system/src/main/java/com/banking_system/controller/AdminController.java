package com.banking_system.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.banking_system.model.dto.CreateUserRequest;
import com.banking_system.model.dto.UpdateUserStatusRequest;
import com.banking_system.model.dto.UserResponse;
import com.banking_system.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin")
@Validated
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

	private final UserService userService;

	public AdminController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping("/users")
	public ResponseEntity<List<UserResponse>> getAllUsers() {
		List<UserResponse> users = userService.getAllUsers();
		return ResponseEntity.ok(users);
	}

	@PostMapping("/users")
	public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
		UserResponse created = userService.createUser(request);
		return ResponseEntity
				.created(URI.create("/api/admin/users/" + created.id()))
				.body(created);
	}

	@PutMapping("/users/{id}/status")
	public ResponseEntity<UserResponse> updateUserStatus(
			@PathVariable String id,
			@Valid @RequestBody UpdateUserStatusRequest request) {
		UserResponse updated = userService.updateUserStatus(id, request);
		return ResponseEntity.ok(updated);
	}
}

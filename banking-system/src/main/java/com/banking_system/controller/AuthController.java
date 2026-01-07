package com.banking_system.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.banking_system.model.User;
import com.banking_system.model.dto.LoginRequest;
import com.banking_system.model.dto.LoginResponse;
import com.banking_system.model.dto.UserResponse;
import com.banking_system.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
@Validated
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/auth/login")
	public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
		LoginResponse response = authService.login(request);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/users/me")
	public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal User user) {
		UserResponse response = new UserResponse(
				user.getId().toHexString(),
				user.getEmail(),
				user.getRole(),
				user.isActive()
		);
		return ResponseEntity.ok(response);
	}
}

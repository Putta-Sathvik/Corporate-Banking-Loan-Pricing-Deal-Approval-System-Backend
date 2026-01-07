package com.banking_system.service;

import java.time.Instant;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.banking_system.config.JwtProperties;
import com.banking_system.exception.InvalidCredentialsException;
import com.banking_system.model.User;
import com.banking_system.model.dto.LoginRequest;
import com.banking_system.model.dto.LoginResponse;
import com.banking_system.model.dto.UserResponse;
import com.banking_system.repository.UserRepository;
import com.banking_system.security.JwtService;

@Service
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final JwtProperties jwtProperties;

	public AuthService(
			UserRepository userRepository,
			PasswordEncoder passwordEncoder,
			JwtService jwtService,
			JwtProperties jwtProperties) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
		this.jwtProperties = jwtProperties;
	}

	public LoginResponse login(LoginRequest request) {
		String email = request.email().trim().toLowerCase(java.util.Locale.ROOT);
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

		if (!user.isActive()) {
			throw new InvalidCredentialsException("Account is not active");
		}

		if (!passwordEncoder.matches(request.password(), user.getPassword())) {
			throw new InvalidCredentialsException("Invalid email or password");
		}

		String token = jwtService.generateToken(user);
		Instant expiresAt = Instant.now().plusSeconds(jwtProperties.expirationMinutes() * 60L);

		UserResponse userResponse = new UserResponse(
				user.getId().toHexString(),
				user.getEmail(),
				user.getRole(),
				user.isActive()
		);

		return new LoginResponse(token, "Bearer", expiresAt, userResponse);
	}
}

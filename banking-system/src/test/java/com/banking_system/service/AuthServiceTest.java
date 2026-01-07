package com.banking_system.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.banking_system.config.JwtProperties;
import com.banking_system.exception.InvalidCredentialsException;
import com.banking_system.model.User;
import com.banking_system.model.UserRole;
import com.banking_system.model.dto.LoginRequest;
import com.banking_system.model.dto.LoginResponse;
import com.banking_system.repository.UserRepository;
import com.banking_system.security.JwtService;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	UserRepository userRepository;

	@Mock
	PasswordEncoder passwordEncoder;

	@Mock
	JwtService jwtService;

	@Mock
	JwtProperties jwtProperties;

	@InjectMocks
	AuthService authService;

	@Test
	void login_success_returnsTokenAndUser() {
		User user = new User(
				new ObjectId(),
				"user@bank.com",
				"hashed",
				UserRole.USER,
				true,
				Instant.now(),
				Instant.now());

		when(userRepository.findByEmail("user@bank.com")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("password123", "hashed")).thenReturn(true);
		when(jwtService.generateToken(user)).thenReturn("fake-jwt-token");
		when(jwtProperties.expirationMinutes()).thenReturn(60);

		LoginRequest request = new LoginRequest("user@bank.com", "password123");
		LoginResponse response = authService.login(request);

		assertThat(response.accessToken()).isEqualTo("fake-jwt-token");
		assertThat(response.tokenType()).isEqualTo("Bearer");
		assertThat(response.user().email()).isEqualTo("user@bank.com");
		assertThat(response.user().role()).isEqualTo(UserRole.USER);
	}

	@Test
	void login_invalidEmail_throwsInvalidCredentials() {
		when(userRepository.findByEmail("unknown@bank.com")).thenReturn(Optional.empty());

		LoginRequest request = new LoginRequest("unknown@bank.com", "password123");

		assertThatThrownBy(() -> authService.login(request))
				.isInstanceOf(InvalidCredentialsException.class)
				.hasMessageContaining("Invalid email or password");
	}

	@Test
	void login_wrongPassword_throwsInvalidCredentials() {
		User user = new User(
				new ObjectId(),
				"user@bank.com",
				"hashed",
				UserRole.USER,
				true,
				Instant.now(),
				Instant.now());

		when(userRepository.findByEmail("user@bank.com")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("wrongpassword", "hashed")).thenReturn(false);

		LoginRequest request = new LoginRequest("user@bank.com", "wrongpassword");

		assertThatThrownBy(() -> authService.login(request))
				.isInstanceOf(InvalidCredentialsException.class)
				.hasMessageContaining("Invalid email or password");
	}

	@Test
	void login_inactiveUser_throwsInvalidCredentials() {
		User user = new User(
				new ObjectId(),
				"user@bank.com",
				"hashed",
				UserRole.USER,
				false,
				Instant.now(),
				Instant.now());

		when(userRepository.findByEmail("user@bank.com")).thenReturn(Optional.of(user));

		LoginRequest request = new LoginRequest("user@bank.com", "password123");

		assertThatThrownBy(() -> authService.login(request))
				.isInstanceOf(InvalidCredentialsException.class)
				.hasMessageContaining("not active");
	}
}

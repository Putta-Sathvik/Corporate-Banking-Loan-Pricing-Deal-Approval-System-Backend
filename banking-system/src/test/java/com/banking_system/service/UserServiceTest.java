package com.banking_system.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.banking_system.exception.UserAlreadyExistsException;
import com.banking_system.exception.UserNotFoundException;
import com.banking_system.model.User;
import com.banking_system.model.UserRole;
import com.banking_system.model.dto.CreateUserRequest;
import com.banking_system.model.dto.UpdateUserStatusRequest;
import com.banking_system.model.dto.UserResponse;
import com.banking_system.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@Mock
	UserRepository userRepository;

	@Mock
	PasswordEncoder passwordEncoder;

	@InjectMocks
	UserService userService;

	@Test
	void getAllUsers_returnsUserList() {
		User u1 = new User(new ObjectId(), "user1@bank.com", "hash1", UserRole.USER, true, Instant.now(), Instant.now());
		User u2 = new User(new ObjectId(), "admin@bank.com", "hash2", UserRole.ADMIN, true, Instant.now(), Instant.now());

		when(userRepository.findAll()).thenReturn(List.of(u1, u2));

		List<UserResponse> result = userService.getAllUsers();

		assertThat(result).hasSize(2);
		assertThat(result.get(0).email()).isEqualTo("user1@bank.com");
		assertThat(result.get(1).email()).isEqualTo("admin@bank.com");
	}

	@Test
	void createUser_success_returnsUserResponse() {
		when(userRepository.existsByEmail("newuser@bank.com")).thenReturn(false);
		when(passwordEncoder.encode("pass123")).thenReturn("hashed");
		when(userRepository.save(any(User.class))).thenAnswer(inv -> {
			User u = inv.getArgument(0);
			u.setId(new ObjectId());
			return u;
		});

		CreateUserRequest request = new CreateUserRequest("newuser@bank.com", "pass123", UserRole.USER);
		UserResponse response = userService.createUser(request);

		assertThat(response.email()).isEqualTo("newuser@bank.com");
		assertThat(response.role()).isEqualTo(UserRole.USER);
		assertThat(response.active()).isTrue();
	}

	@Test
	void createUser_duplicateEmail_throwsUserAlreadyExists() {
		when(userRepository.existsByEmail("duplicate@bank.com")).thenReturn(true);

		CreateUserRequest request = new CreateUserRequest("duplicate@bank.com", "pass", UserRole.USER);

		assertThatThrownBy(() -> userService.createUser(request))
				.isInstanceOf(UserAlreadyExistsException.class);
	}

	@Test
	void updateUserStatus_success_updatesActive() {
		ObjectId id = new ObjectId();
		User user = new User(id, "user@bank.com", "hash", UserRole.USER, true, Instant.now(), Instant.now());

		when(userRepository.findById(id)).thenReturn(Optional.of(user));
		when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

		UpdateUserStatusRequest request = new UpdateUserStatusRequest(false);
		UserResponse response = userService.updateUserStatus(id.toHexString(), request);

		assertThat(response.active()).isFalse();
	}

	@Test
	void updateUserStatus_invalidId_throwsUserNotFound() {
		UpdateUserStatusRequest request = new UpdateUserStatusRequest(false);

		assertThatThrownBy(() -> userService.updateUserStatus("invalid-id", request))
				.isInstanceOf(UserNotFoundException.class);
	}

	@Test
	void updateUserStatus_userNotFound_throwsUserNotFound() {
		ObjectId id = new ObjectId();
		when(userRepository.findById(id)).thenReturn(Optional.empty());

		UpdateUserStatusRequest request = new UpdateUserStatusRequest(false);

		assertThatThrownBy(() -> userService.updateUserStatus(id.toHexString(), request))
				.isInstanceOf(UserNotFoundException.class);
	}
}

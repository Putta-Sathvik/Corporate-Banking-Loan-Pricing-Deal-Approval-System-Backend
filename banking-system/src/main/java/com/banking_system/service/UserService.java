package com.banking_system.service;

import java.time.Instant;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.banking_system.exception.UserAlreadyExistsException;
import com.banking_system.exception.UserNotFoundException;
import com.banking_system.model.User;
import com.banking_system.model.dto.CreateUserRequest;
import com.banking_system.model.dto.UpdateUserStatusRequest;
import com.banking_system.model.dto.UserResponse;
import com.banking_system.repository.UserRepository;

@Service
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	public List<UserResponse> getAllUsers() {
		return userRepository.findAll().stream()
				.map(this::toResponse)
				.toList();
	}

	public UserResponse createUser(CreateUserRequest request) {
		String email = request.email().trim().toLowerCase(java.util.Locale.ROOT);

		if (userRepository.existsByEmail(email)) {
			throw new UserAlreadyExistsException(email);
		}

		Instant now = Instant.now();
		User user = new User(
				null,
				email,
				passwordEncoder.encode(request.password()),
				request.role(),
				true,
				now,
				now
		);

		User saved = userRepository.save(user);
		return toResponse(saved);
	}

	public UserResponse updateUserStatus(String userId, UpdateUserStatusRequest request) {
		ObjectId objectId;
		try {
			objectId = new ObjectId(userId);
		} catch (IllegalArgumentException e) {
			throw new UserNotFoundException(userId);
		}

		User user = userRepository.findById(objectId)
				.orElseThrow(() -> new UserNotFoundException(userId));

		user.setActive(request.active());
		user.setUpdatedAt(Instant.now());

		User saved = userRepository.save(user);
		return toResponse(saved);
	}

	private UserResponse toResponse(User user) {
		return new UserResponse(
				user.getId().toHexString(),
				user.getEmail(),
				user.getRole(),
				user.isActive()
		);
	}
}

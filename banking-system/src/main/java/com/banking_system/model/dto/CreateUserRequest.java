package com.banking_system.model.dto;

import com.banking_system.model.UserRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateUserRequest(
		@NotBlank(message = "email is required")
		@Email(message = "email must be valid")
		String email,

		@NotBlank(message = "password is required")
		String password,

		@NotNull(message = "role is required")
		UserRole role
) {
}

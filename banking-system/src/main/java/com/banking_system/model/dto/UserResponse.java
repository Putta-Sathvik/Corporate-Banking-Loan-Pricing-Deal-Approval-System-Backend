package com.banking_system.model.dto;

import com.banking_system.model.UserRole;

public record UserResponse(
		String id,
		String email,
		UserRole role,
		boolean active
) {
}

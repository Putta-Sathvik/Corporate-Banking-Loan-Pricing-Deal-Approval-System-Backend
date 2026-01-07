package com.banking_system.model.dto;

import jakarta.validation.constraints.NotBlank;

public record ChangeStatusRequest(
		@NotBlank(message = "Status is required")
		String status,
		
		String comments
) {
}

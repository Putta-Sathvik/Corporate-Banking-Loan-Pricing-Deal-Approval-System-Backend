package com.banking_system.model.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateAccountRequest(
		@NotBlank(message = "holderName is required")
		String holderName
) {
}

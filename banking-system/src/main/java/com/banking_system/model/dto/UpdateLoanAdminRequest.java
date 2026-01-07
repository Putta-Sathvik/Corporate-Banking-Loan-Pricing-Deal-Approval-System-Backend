package com.banking_system.model.dto;

import jakarta.validation.constraints.Positive;

public record UpdateLoanAdminRequest(
		@Positive(message = "sanctionedAmount must be positive")
		Double sanctionedAmount,

		@Positive(message = "approvedInterestRate must be positive")
		Double approvedInterestRate
) {
}

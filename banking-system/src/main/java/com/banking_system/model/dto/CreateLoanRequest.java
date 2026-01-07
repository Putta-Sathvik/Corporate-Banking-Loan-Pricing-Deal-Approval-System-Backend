package com.banking_system.model.dto;

import com.banking_system.model.Loan.Financials;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateLoanRequest(
		@NotBlank(message = "clientName is required")
		String clientName,

		@NotBlank(message = "loanType is required")
		String loanType,

		@NotNull(message = "requestedAmount is required")
		@Positive(message = "requestedAmount must be positive")
		Double requestedAmount,

		@NotNull(message = "proposedInterestRate is required")
		@Positive(message = "proposedInterestRate must be positive")
		Double proposedInterestRate,

		@NotNull(message = "tenureMonths is required")
		@Positive(message = "tenureMonths must be positive")
		Integer tenureMonths,

		Financials financials
) {
}

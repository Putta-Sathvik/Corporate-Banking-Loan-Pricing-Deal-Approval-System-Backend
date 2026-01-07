package com.banking_system.model.dto;

import com.banking_system.model.Loan.Financials;

import jakarta.validation.constraints.Positive;

public record UpdateLoanRequest(
		String clientName,
		String loanType,

		@Positive(message = "requestedAmount must be positive")
		Double requestedAmount,

		@Positive(message = "proposedInterestRate must be positive")
		Double proposedInterestRate,

		@Positive(message = "tenureMonths must be positive")
		Integer tenureMonths,

		Financials financials
) {
}

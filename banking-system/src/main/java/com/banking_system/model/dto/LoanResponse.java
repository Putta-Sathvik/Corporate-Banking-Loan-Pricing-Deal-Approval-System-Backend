package com.banking_system.model.dto;

import java.time.Instant;

import com.banking_system.model.Loan.Financials;
import com.banking_system.model.LoanStatus;

public record LoanResponse(
		String id,
		String clientName,
		String loanType,
		Double requestedAmount,
		Double proposedInterestRate,
		Integer tenureMonths,
		Financials financials,
		LoanStatus status,
		Double sanctionedAmount,
		Double approvedInterestRate,
		String createdBy,
		String updatedBy,
		String approvedBy,
		Instant approvedAt,
		Instant createdAt,
		Instant updatedAt
) {
}

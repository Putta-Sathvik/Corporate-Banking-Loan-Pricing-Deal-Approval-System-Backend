package com.banking_system.model.dto;

public record PricingRequest(
		Double requestedAmount,
		Double proposedInterestRate,
		Integer tenureMonths,
		String rating
) {
}

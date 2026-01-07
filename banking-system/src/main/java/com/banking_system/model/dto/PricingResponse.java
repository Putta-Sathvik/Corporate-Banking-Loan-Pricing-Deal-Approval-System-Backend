package com.banking_system.model.dto;

public record PricingResponse(
		Double recommendedRate,
		Double emi,
		Double totalInterest,
		String riskCategory
) {
}

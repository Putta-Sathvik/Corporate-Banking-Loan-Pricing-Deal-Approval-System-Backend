package com.banking_system.service;

import org.springframework.stereotype.Service;

import com.banking_system.model.dto.PricingRequest;
import com.banking_system.model.dto.PricingResponse;

@Service
public class PricingService {

	/**
	 * Simple pricing calculation based on rating and tenure
	 * Formula: 
	 * - Base rate adjustment by rating (A=0%, B=+0.5%, C=+1.0%, D=+1.5%)
	 * - Risk category based on rating
	 * - EMI = P * r * (1+r)^n / ((1+r)^n - 1)
	 */
	public PricingResponse calculatePricing(PricingRequest request) {
		double baseRate = request.proposedInterestRate();
		String rating = request.rating() != null ? request.rating().toUpperCase() : "C";

		// Adjust rate based on rating
		double rateAdjustment = switch (rating) {
			case "A" -> 0.0;
			case "B" -> 0.5;
			case "C" -> 1.0;
			default -> 1.5;
		};

		double recommendedRate = baseRate + rateAdjustment;
		String riskCategory = switch (rating) {
			case "A" -> "LOW";
			case "B" -> "MEDIUM";
			case "C" -> "HIGH";
			default -> "VERY_HIGH";
		};

		// Calculate EMI (Equated Monthly Installment)
		double principal = request.requestedAmount();
		double monthlyRate = recommendedRate / 100 / 12;
		int n = request.tenureMonths();

		double emi = 0.0;
		double totalInterest = 0.0;

		if (monthlyRate > 0) {
			double powerTerm = Math.pow(1 + monthlyRate, n);
			emi = principal * monthlyRate * powerTerm / (powerTerm - 1);
			totalInterest = (emi * n) - principal;
		} else {
			// Zero interest case
			emi = principal / n;
		}

		return new PricingResponse(
				Math.round(recommendedRate * 100.0) / 100.0,
				Math.round(emi * 100.0) / 100.0,
				Math.round(totalInterest * 100.0) / 100.0,
				riskCategory
		);
	}
}

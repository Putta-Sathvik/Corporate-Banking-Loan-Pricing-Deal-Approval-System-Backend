package com.banking_system.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.banking_system.model.dto.PricingRequest;
import com.banking_system.model.dto.PricingResponse;

class PricingServiceTest {

	private final PricingService pricingService = new PricingService();

	@Test
	void calculatePricing_ratingA_returnsLowRisk() {
		PricingRequest request = new PricingRequest(50000000.0, 11.5, 36, "A");

		PricingResponse response = pricingService.calculatePricing(request);

		assertThat(response.recommendedRate()).isEqualTo(11.5); // No adjustment for A
		assertThat(response.riskCategory()).isEqualTo("LOW");
		assertThat(response.emi()).isGreaterThan(0);
		assertThat(response.totalInterest()).isGreaterThan(0);
	}

	@Test
	void calculatePricing_ratingB_addsFiftyBasisPoints() {
		PricingRequest request = new PricingRequest(50000000.0, 11.5, 36, "B");

		PricingResponse response = pricingService.calculatePricing(request);

		assertThat(response.recommendedRate()).isEqualTo(12.0); // +0.5% for B
		assertThat(response.riskCategory()).isEqualTo("MEDIUM");
	}

	@Test
	void calculatePricing_ratingC_addsOnePercent() {
		PricingRequest request = new PricingRequest(50000000.0, 11.5, 36, "C");

		PricingResponse response = pricingService.calculatePricing(request);

		assertThat(response.recommendedRate()).isEqualTo(12.5); // +1.0% for C
		assertThat(response.riskCategory()).isEqualTo("HIGH");
	}

	@Test
	void calculatePricing_ratingD_addsOneFiftyBasisPoints() {
		PricingRequest request = new PricingRequest(50000000.0, 11.5, 36, "D");

		PricingResponse response = pricingService.calculatePricing(request);

		assertThat(response.recommendedRate()).isEqualTo(13.0); // +1.5% for D
		assertThat(response.riskCategory()).isEqualTo("VERY_HIGH");
	}

	@Test
	void calculatePricing_emiCalculation_isCorrect() {
		// Simplified test: 10,000 principal, 12% annual (1% monthly), 12 months
		PricingRequest request = new PricingRequest(10000.0, 12.0, 12, "A");

		PricingResponse response = pricingService.calculatePricing(request);

		// EMI formula: P * r * (1+r)^n / ((1+r)^n - 1)
		// Expected EMI ~888.49
		assertThat(response.emi()).isBetween(880.0, 900.0);
		assertThat(response.totalInterest()).isBetween(600.0, 700.0);
	}
}

package com.banking_system.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class AmountRequest {
	@NotNull(message = "amount is required")
	@Positive(message = "amount must be positive")
	private Double amount;

	public AmountRequest() {
	}

	public AmountRequest(Double amount) {
		this.amount = amount;
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}
}

package com.banking_system.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class TransferRequest {
	@NotBlank(message = "sourceAccount is required")
	private String sourceAccount;

	@NotBlank(message = "destinationAccount is required")
	private String destinationAccount;

	@NotNull(message = "amount is required")
	@Positive(message = "amount must be positive")
	private Double amount;

	public TransferRequest() {
	}

	public TransferRequest(String sourceAccount, String destinationAccount, Double amount) {
		this.sourceAccount = sourceAccount;
		this.destinationAccount = destinationAccount;
		this.amount = amount;
	}

	public String getSourceAccount() {
		return sourceAccount;
	}

	public void setSourceAccount(String sourceAccount) {
		this.sourceAccount = sourceAccount;
	}

	public String getDestinationAccount() {
		return destinationAccount;
	}

	public void setDestinationAccount(String destinationAccount) {
		this.destinationAccount = destinationAccount;
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}
}

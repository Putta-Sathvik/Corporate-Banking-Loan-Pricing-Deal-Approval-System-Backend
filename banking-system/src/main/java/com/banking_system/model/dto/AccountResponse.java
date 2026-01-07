package com.banking_system.model.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.banking_system.model.AccountStatus;

public record AccountResponse(
		String accountNumber,
		String holderName,
		BigDecimal balance,
		AccountStatus status,
		Instant createdAt
) {
}

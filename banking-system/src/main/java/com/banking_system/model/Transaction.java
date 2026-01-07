package com.banking_system.model;

import java.time.Instant;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Document(collection = "transactions")
public class Transaction {
	@Id
	private ObjectId id;

	@Indexed(unique = true)
	@NotBlank(message = "transactionId is required")
	private String transactionId;

	@NotNull(message = "type is required")
	private TransactionType type;

	@NotNull(message = "amount is required")
	@Positive(message = "amount must be positive")
	private Double amount;

	@NotNull(message = "timestamp is required")
	private Instant timestamp;

	@NotNull(message = "status is required")
	private TransactionStatus status;

	private String sourceAccount;
	private String destinationAccount;

	public Transaction() {
	}

	public Transaction(
			ObjectId id,
			String transactionId,
			TransactionType type,
			Double amount,
			Instant timestamp,
			TransactionStatus status,
			String sourceAccount,
			String destinationAccount) {
		this.id = id;
		this.transactionId = transactionId;
		this.type = type;
		this.amount = amount;
		this.timestamp = timestamp;
		this.status = status;
		this.sourceAccount = sourceAccount;
		this.destinationAccount = destinationAccount;
	}

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public TransactionType getType() {
		return type;
	}

	public void setType(TransactionType type) {
		this.type = type;
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	public Instant getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Instant timestamp) {
		this.timestamp = timestamp;
	}

	public TransactionStatus getStatus() {
		return status;
	}

	public void setStatus(TransactionStatus status) {
		this.status = status;
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
}

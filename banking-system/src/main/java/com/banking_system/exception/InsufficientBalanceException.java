package com.banking_system.exception;

public class InsufficientBalanceException extends RuntimeException {
	public InsufficientBalanceException() {
		super("Insufficient balance");
	}

	public InsufficientBalanceException(String message) {
		super(message);
	}
}

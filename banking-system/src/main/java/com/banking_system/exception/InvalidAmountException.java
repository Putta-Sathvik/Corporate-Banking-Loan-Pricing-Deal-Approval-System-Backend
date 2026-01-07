package com.banking_system.exception;

public class InvalidAmountException extends RuntimeException {
	public InvalidAmountException() {
		super("Invalid amount");
	}

	public InvalidAmountException(String message) {
		super(message);
	}
}

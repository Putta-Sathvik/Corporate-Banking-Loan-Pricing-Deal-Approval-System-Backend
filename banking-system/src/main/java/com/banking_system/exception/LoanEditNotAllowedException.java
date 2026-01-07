package com.banking_system.exception;

public class LoanEditNotAllowedException extends RuntimeException {
	public LoanEditNotAllowedException(String message) {
		super(message);
	}
}

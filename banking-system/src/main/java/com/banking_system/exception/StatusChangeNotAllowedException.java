package com.banking_system.exception;

public class StatusChangeNotAllowedException extends RuntimeException {
	public StatusChangeNotAllowedException(String message) {
		super(message);
	}
}

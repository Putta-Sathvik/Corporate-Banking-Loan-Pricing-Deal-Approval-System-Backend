package com.banking_system.service;

import java.util.List;

import com.banking_system.model.Account;
import com.banking_system.model.Transaction;

public interface AccountService {
	Account createAccount(String holderName);

	Account getByAccountNumber(String accountNumber);

	Account deposit(String accountNumber, Double amount);

	Account withdraw(String accountNumber, Double amount);

	Account transfer(String sourceAccountNumber, String destinationAccountNumber, Double amount);

	List<Transaction> getTransactions(String accountNumber);
}

package com.banking_system.controller;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.banking_system.model.Account;
import com.banking_system.model.Transaction;
import com.banking_system.model.dto.AccountResponse;
import com.banking_system.model.dto.CreateAccountRequest;
import com.banking_system.model.dto.AmountRequest;
import com.banking_system.model.dto.TransferRequest;
import com.banking_system.service.AccountService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/accounts")
@Validated
public class AccountController {
	private static final Logger log = LoggerFactory.getLogger(AccountController.class);

	private final AccountService accountService;

	public AccountController(AccountService accountService) {
		this.accountService = accountService;
	}

	@PostMapping
	public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest request) {
		Account created = accountService.createAccount(request.holderName());
		log.info("API create account {}", created.getAccountNumber());
		return ResponseEntity
				.created(URI.create("/api/accounts/" + created.getAccountNumber()))
				.body(toResponse(created));
	}

	@GetMapping("/{accountNumber}")
	public ResponseEntity<AccountResponse> getAccount(@PathVariable String accountNumber) {
		Account account = accountService.getByAccountNumber(accountNumber);
		return ResponseEntity.ok(toResponse(account));
	}

	@PutMapping("/{accountNumber}/deposit")
	public ResponseEntity<AccountResponse> deposit(@PathVariable String accountNumber, @Valid @RequestBody AmountRequest request) {
		Account updated = accountService.deposit(accountNumber, request.getAmount());
		return ResponseEntity.ok(toResponse(updated));
	}

	@PutMapping("/{accountNumber}/withdraw")
	public ResponseEntity<AccountResponse> withdraw(@PathVariable String accountNumber, @Valid @RequestBody AmountRequest request) {
		Account updated = accountService.withdraw(accountNumber, request.getAmount());
		return ResponseEntity.ok(toResponse(updated));
	}

	@PostMapping("/transfer")
	public ResponseEntity<AccountResponse> transfer(@Valid @RequestBody TransferRequest request) {
		Account updatedSource = accountService.transfer(request.getSourceAccount(), request.getDestinationAccount(), request.getAmount());
		return ResponseEntity.ok(toResponse(updatedSource));
	}

	@GetMapping("/{accountNumber}/transactions")
	public ResponseEntity<java.util.List<Transaction>> getTransactions(@PathVariable String accountNumber) {
		java.util.List<Transaction> txns = accountService.getTransactions(accountNumber);
		return ResponseEntity.ok(txns);
	}

	private static AccountResponse toResponse(Account account) {
		return new AccountResponse(
				account.getAccountNumber(),
				account.getHolderName(),
				account.getBalance(),
				account.getStatus(),
				account.getCreatedAt());
	}
}

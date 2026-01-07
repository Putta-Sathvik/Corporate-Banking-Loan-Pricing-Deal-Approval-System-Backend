package com.banking_system.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.banking_system.exception.InsufficientBalanceException;
import com.banking_system.model.Account;
import com.banking_system.model.AccountStatus;
import com.banking_system.repository.AccountRepository;
import com.banking_system.repository.TransactionRepository;

@ExtendWith(MockitoExtension.class)
class AccountServiceWithdrawTest {
	@Mock
	AccountRepository accountRepository;

	@Mock
	TransactionRepository transactionRepository;

	@InjectMocks
	AccountServiceImpl accountService;

	@Test
	void withdraw_success_updatesBalance_andSavesTransaction() {
		Account existing = new Account(
				null,
				"JOH0002",
				"John Doe",
				new BigDecimal("1000.0"),
				AccountStatus.ACTIVE,
				null);

		when(accountRepository.findByAccountNumber("JOH0002")).thenReturn(Optional.of(existing));
		when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0, Account.class));

		Account updated = accountService.withdraw("JOH0002", 400d);

		assertThat(updated.getBalance()).isEqualByComparingTo(new BigDecimal("600.0"));
		verify(transactionRepository, times(1)).save(any());
		verify(accountRepository, times(1)).save(any());
	}

	@Test
	void withdraw_insufficient_recordsFailedTransaction_andThrows() {
		Account existing = new Account(
				null,
				"JOH0003",
				"John Doe",
				new BigDecimal("200.0"),
				AccountStatus.ACTIVE,
				null);

		when(accountRepository.findByAccountNumber("JOH0003")).thenReturn(Optional.of(existing));

		assertThatThrownBy(() -> accountService.withdraw("JOH0003", 400d))
				.isInstanceOf(InsufficientBalanceException.class);
		verify(transactionRepository, times(1)).save(any());
		verify(accountRepository, times(0)).save(any());
	}
}

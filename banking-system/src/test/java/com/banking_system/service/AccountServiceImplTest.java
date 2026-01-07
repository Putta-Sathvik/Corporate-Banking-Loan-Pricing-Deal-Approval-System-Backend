package com.banking_system.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

import com.banking_system.model.Account;
import com.banking_system.model.AccountStatus;
import com.banking_system.repository.AccountRepository;
import com.banking_system.repository.TransactionRepository;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {
	@Mock
	AccountRepository accountRepository;

	@Mock
	TransactionRepository transactionRepository;

	@InjectMocks
	AccountServiceImpl accountService;

	@Test
	void deposit_increasesBalance_andSavesTransaction() {
		Account existing = new Account(
				null,
				"JOH0001",
				"John Doe",
				BigDecimal.ZERO,
				AccountStatus.ACTIVE,
				null);

		when(accountRepository.findByAccountNumber("JOH0001")).thenReturn(Optional.of(existing));
		when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0, Account.class));

		Account updated = accountService.deposit("JOH0001", 1000d);

		assertThat(updated.getBalance()).isEqualByComparingTo(new BigDecimal("1000.0"));
		verify(transactionRepository, times(1)).save(any());
		verify(accountRepository, times(1)).save(any());
	}
}

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
class AccountServiceTransferTest {
	@Mock
	AccountRepository accountRepository;

	@Mock
	TransactionRepository transactionRepository;

	@InjectMocks
	AccountServiceImpl accountService;

	@Test
	void transfer_success_updatesBothBalances_andSavesTransaction() {
		Account source = new Account(
				null,
				"SRC0001",
				"Source",
				new BigDecimal("2000.0"),
				AccountStatus.ACTIVE,
				null);
		Account dest = new Account(
				null,
				"DST0001",
				"Dest",
				new BigDecimal("500.0"),
				AccountStatus.ACTIVE,
				null);

		when(accountRepository.findByAccountNumber("SRC0001")).thenReturn(Optional.of(source));
		when(accountRepository.findByAccountNumber("DST0001")).thenReturn(Optional.of(dest));
		when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0, Account.class));

		Account updatedSource = accountService.transfer("SRC0001", "DST0001", 1000d);

		assertThat(updatedSource.getBalance()).isEqualByComparingTo(new BigDecimal("1000.0"));
		assertThat(dest.getBalance()).isEqualByComparingTo(new BigDecimal("1500.0"));
		verify(transactionRepository, times(1)).save(any());
	}

	@Test
	void transfer_insufficient_recordsFailedTransaction_andThrows() {
		Account source = new Account(
				null,
				"SRC0002",
				"Source",
				new BigDecimal("500.0"),
				AccountStatus.ACTIVE,
				null);
		Account dest = new Account(
				null,
				"DST0002",
				"Dest",
				new BigDecimal("500.0"),
				AccountStatus.ACTIVE,
				null);

		when(accountRepository.findByAccountNumber("SRC0002")).thenReturn(Optional.of(source));
		when(accountRepository.findByAccountNumber("DST0002")).thenReturn(Optional.of(dest));

		assertThatThrownBy(() -> accountService.transfer("SRC0002", "DST0002", 1000d))
				.isInstanceOf(InsufficientBalanceException.class);
		verify(transactionRepository, times(1)).save(any());
	}
}

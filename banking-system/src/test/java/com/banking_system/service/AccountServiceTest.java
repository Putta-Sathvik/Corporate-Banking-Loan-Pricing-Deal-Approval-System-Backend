package com.banking_system.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.banking_system.exception.AccountNotFoundException;
import com.banking_system.model.Account;
import com.banking_system.repository.AccountRepository;
import com.banking_system.repository.TransactionRepository;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
	@Mock
	AccountRepository accountRepository;

	@Mock
	TransactionRepository transactionRepository;

	@InjectMocks
	AccountServiceImpl accountService;

	@Test
	void createAccount_generatesAccountNumber_andInitializesBalance() {
		when(accountRepository.existsByAccountNumber(any())).thenReturn(false);
		when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0, Account.class));

		Account created = accountService.createAccount("John Doe");

		assertThat(created.getAccountNumber()).startsWith("JOH");
		assertThat(created.getAccountNumber()).hasSize(7);
		assertThat(created.getHolderName()).isEqualTo("John Doe");
		assertThat(created.getBalance()).isNotNull();
		assertThat(created.getCreatedAt()).isNotNull();
	}

	@Test
	void getByAccountNumber_throwsWhenMissing() {
		when(accountRepository.findByAccountNumber("MISSING1234")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> accountService.getByAccountNumber("MISSING1234"))
				.isInstanceOf(AccountNotFoundException.class);
	}
}

package com.banking_system.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.banking_system.model.Account;
import com.banking_system.model.AccountStatus;
import com.banking_system.model.Transaction;
import com.banking_system.model.TransactionStatus;
import com.banking_system.model.TransactionType;
import com.banking_system.repository.AccountRepository;
import com.banking_system.repository.TransactionRepository;

@ExtendWith(MockitoExtension.class)
class AccountServiceGetTransactionsTest {
	@Mock
	AccountRepository accountRepository;

	@Mock
	TransactionRepository transactionRepository;

	@InjectMocks
	AccountServiceImpl accountService;

	@Test
	void getTransactions_returnsBothSourceAndDestination_sortedDesc() {
		String acct = "ACC0001";
		Account existing = new Account(null, acct, "Holder", java.math.BigDecimal.ZERO, AccountStatus.ACTIVE, Instant.now());
		when(accountRepository.findByAccountNumber(acct)).thenReturn(Optional.of(existing));

		Transaction t1 = new Transaction(null, "TXN-1", TransactionType.DEPOSIT, 100.0, Instant.parse("2025-12-31T10:00:00Z"), TransactionStatus.SUCCESS, null, acct);
		Transaction t2 = new Transaction(null, "TXN-2", TransactionType.TRANSFER, 50.0, Instant.parse("2025-12-31T12:00:00Z"), TransactionStatus.SUCCESS, acct, "OTHER");
		when(transactionRepository.findBySourceAccountOrDestinationAccount(acct, acct)).thenReturn(java.util.Arrays.asList(t1, t2));

		List<Transaction> result = accountService.getTransactions(acct);

		assertThat(result).hasSize(2);
		assertThat(result.get(0).getTransactionId()).isEqualTo("TXN-2"); // latest first
		assertThat(result.get(1).getTransactionId()).isEqualTo("TXN-1");
		verify(transactionRepository, times(1)).findBySourceAccountOrDestinationAccount(acct, acct);
	}
}

package com.banking_system.service;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Comparator;
import java.util.Locale;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.banking_system.exception.AccountNotFoundException;
import com.banking_system.exception.InvalidAmountException;
import com.banking_system.exception.InsufficientBalanceException;
import com.banking_system.model.Account;
import com.banking_system.model.AccountStatus;
import com.banking_system.model.Transaction;
import com.banking_system.model.TransactionStatus;
import com.banking_system.model.TransactionType;
import com.banking_system.repository.AccountRepository;
import com.banking_system.repository.TransactionRepository;

@Service
public class AccountServiceImpl implements AccountService {
	private static final Logger log = LoggerFactory.getLogger(AccountServiceImpl.class);
	private static final int MAX_GENERATION_ATTEMPTS = 25;

	private final AccountRepository accountRepository;
	private final TransactionRepository transactionRepository;
	private final SecureRandom random = new SecureRandom();

	public AccountServiceImpl(AccountRepository accountRepository, TransactionRepository transactionRepository) {
		this.accountRepository = accountRepository;
		this.transactionRepository = transactionRepository;
	}

	@Override
	public Account createAccount(String holderName) {
		String normalizedName = holderName == null ? null : holderName.trim();
		String accountNumber = generateUniqueAccountNumber(normalizedName);

		Account account = new Account(
				null,
				accountNumber,
				normalizedName,
				BigDecimal.ZERO,
				AccountStatus.ACTIVE,
				Instant.now());

		Account saved = accountRepository.save(account);
		log.info("Created account {} for holder {}", saved.getAccountNumber(), saved.getHolderName());
		return saved;
	}

	@Override
	public Account getByAccountNumber(String accountNumber) {
		return accountRepository
				.findByAccountNumber(accountNumber)
				.orElseThrow(() -> new AccountNotFoundException(accountNumber));
	}

	@Override
	public Account deposit(String accountNumber, Double amount) {
		if (amount == null || amount.doubleValue() <= 0d) {
			throw new InvalidAmountException("Amount must be positive");
		}

		Account account = accountRepository
				.findByAccountNumber(accountNumber)
				.orElseThrow(() -> new AccountNotFoundException(accountNumber));

		BigDecimal newBalance = account.getBalance().add(BigDecimal.valueOf(amount));
		account.setBalance(newBalance);

		Account saved = accountRepository.save(account);

		Instant now = Instant.now();
		String txId = "TXN-" + now.toEpochMilli();
		Transaction txn = new Transaction(
				null,
				txId,
				TransactionType.DEPOSIT,
				amount,
				now,
				TransactionStatus.SUCCESS,
				null,
				accountNumber);
		transactionRepository.save(txn);

		log.info("Deposited {} to account {}. New balance: {}", amount, accountNumber, newBalance);
		return saved;
	}

	@Override
	public Account withdraw(String accountNumber, Double amount) {
		if (amount == null || amount.doubleValue() <= 0d) {
			throw new InvalidAmountException("Amount must be positive");
		}

		Account account = accountRepository
				.findByAccountNumber(accountNumber)
				.orElseThrow(() -> new AccountNotFoundException(accountNumber));

		BigDecimal withdrawAmount = BigDecimal.valueOf(amount);
		if (account.getBalance().compareTo(withdrawAmount) < 0) {
			Instant now = Instant.now();
			String txId = "TXN-" + now.toEpochMilli();
			Transaction failedTxn = new Transaction(
					null,
					txId,
					TransactionType.WITHDRAW,
					amount,
					now,
					TransactionStatus.FAILED,
					accountNumber,
					null);
			transactionRepository.save(failedTxn);
			throw new InsufficientBalanceException("Insufficient balance for withdrawal");
		}

		BigDecimal newBalance = account.getBalance().subtract(withdrawAmount);
		account.setBalance(newBalance);

		Account saved = accountRepository.save(account);

		Instant now = Instant.now();
		String txId = "TXN-" + now.toEpochMilli();
		Transaction successTxn = new Transaction(
				null,
				txId,
				TransactionType.WITHDRAW,
				amount,
				now,
				TransactionStatus.SUCCESS,
				accountNumber,
				null);
		transactionRepository.save(successTxn);

		log.info("Withdrew {} from account {}. New balance: {}", amount, accountNumber, newBalance);
		return saved;
	}

	@Override
	public Account transfer(String sourceAccountNumber, String destinationAccountNumber, Double amount) {
		if (amount == null || amount.doubleValue() <= 0d) {
			throw new InvalidAmountException("Amount must be positive");
		}
		if (sourceAccountNumber == null || destinationAccountNumber == null || sourceAccountNumber.equals(destinationAccountNumber)) {
			throw new InvalidAmountException("Source and destination accounts must differ");
		}

		Account source = accountRepository
				.findByAccountNumber(sourceAccountNumber)
				.orElseThrow(() -> new AccountNotFoundException(sourceAccountNumber));
		Account destination = accountRepository
				.findByAccountNumber(destinationAccountNumber)
				.orElseThrow(() -> new AccountNotFoundException(destinationAccountNumber));

		BigDecimal amountBD = BigDecimal.valueOf(amount);
		if (source.getBalance().compareTo(amountBD) < 0) {
			Instant now = Instant.now();
			String txId = "TXN-" + now.toEpochMilli();
			Transaction failedTxn = new Transaction(
					null,
					txId,
					TransactionType.TRANSFER,
					amount,
					now,
					TransactionStatus.FAILED,
					sourceAccountNumber,
					destinationAccountNumber);
			transactionRepository.save(failedTxn);
			throw new InsufficientBalanceException("Insufficient balance for transfer");
		}

		BigDecimal newSourceBal = source.getBalance().subtract(amountBD);
		BigDecimal newDestBal = destination.getBalance().add(amountBD);
		source.setBalance(newSourceBal);
		destination.setBalance(newDestBal);

		Account savedSource = accountRepository.save(source);
		accountRepository.save(destination);

		Instant now = Instant.now();
		String txId = "TXN-" + now.toEpochMilli();
		Transaction successTxn = new Transaction(
				null,
				txId,
				TransactionType.TRANSFER,
				amount,
				now,
				TransactionStatus.SUCCESS,
				sourceAccountNumber,
				destinationAccountNumber);
		transactionRepository.save(successTxn);

		log.info("Transferred {} from {} to {}. New source balance: {}, destination balance: {}",
				amount, sourceAccountNumber, destinationAccountNumber, newSourceBal, newDestBal);
		return savedSource;
	}

	@Override
	public List<Transaction> getTransactions(String accountNumber) {
		accountRepository
				.findByAccountNumber(accountNumber)
				.orElseThrow(() -> new AccountNotFoundException(accountNumber));

		List<Transaction> txns = transactionRepository
				.findBySourceAccountOrDestinationAccount(accountNumber, accountNumber);
		txns.sort(Comparator.comparing(Transaction::getTimestamp).reversed());
		return txns;
	}

	private String generateUniqueAccountNumber(String holderName) {
		String prefix = buildPrefix(holderName);
		for (int attempt = 0; attempt < MAX_GENERATION_ATTEMPTS; attempt++) {
			String suffix = String.format(Locale.ROOT, "%04d", random.nextInt(10_000));
			String candidate = prefix + suffix;
			if (!accountRepository.existsByAccountNumber(candidate)) {
				return candidate;
			}
		}
		throw new IllegalStateException("Failed to generate a unique account number");
	}

	private static String buildPrefix(String holderName) {
		if (!StringUtils.hasText(holderName)) {
			return "ACC";
		}

		String lettersOnly = holderName.replaceAll("[^A-Za-z]", "");
		if (!StringUtils.hasText(lettersOnly)) {
			return "ACC";
		}

		String upper = lettersOnly.toUpperCase(Locale.ROOT);
		return upper.length() >= 3 ? upper.substring(0, 3) : String.format(Locale.ROOT, "%-3s", upper).replace(' ', 'X');
	}
}

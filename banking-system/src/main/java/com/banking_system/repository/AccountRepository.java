package com.banking_system.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.banking_system.model.Account;

public interface AccountRepository extends MongoRepository<Account, String> {
	Optional<Account> findByAccountNumber(String accountNumber);

	boolean existsByAccountNumber(String accountNumber);
}

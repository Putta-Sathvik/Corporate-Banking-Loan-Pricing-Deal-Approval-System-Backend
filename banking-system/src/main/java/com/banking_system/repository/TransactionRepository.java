package com.banking_system.repository;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.banking_system.model.Transaction;

public interface TransactionRepository extends MongoRepository<Transaction, ObjectId> {
	List<Transaction> findBySourceAccount(String sourceAccount);

	List<Transaction> findByDestinationAccount(String destinationAccount);

	List<Transaction> findBySourceAccountOrDestinationAccount(String sourceAccount, String destinationAccount);
}

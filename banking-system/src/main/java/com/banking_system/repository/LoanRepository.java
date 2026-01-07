package com.banking_system.repository;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.banking_system.model.Loan;
import com.banking_system.model.LoanStatus;

public interface LoanRepository extends MongoRepository<Loan, ObjectId> {
	List<Loan> findByDeletedFalse();
	
	List<Loan> findByCreatedBy(ObjectId createdBy);
	
	Page<Loan> findByDeleted(boolean deleted, Pageable pageable);
	
	Page<Loan> findByDeletedAndStatus(boolean deleted, LoanStatus status, Pageable pageable);
	
	Page<Loan> findByDeletedAndClientNameContainingIgnoreCase(boolean deleted, String clientName, Pageable pageable);
	
	Page<Loan> findByDeletedAndLoanType(boolean deleted, String loanType, Pageable pageable);
	
	Page<Loan> findByDeletedAndStatusAndClientNameContainingIgnoreCase(
			boolean deleted, LoanStatus status, String clientName, Pageable pageable);
	
	Page<Loan> findByDeletedAndStatusAndLoanType(
			boolean deleted, LoanStatus status, String loanType, Pageable pageable);
	
	Page<Loan> findByDeletedAndClientNameContainingIgnoreCaseAndLoanType(
			boolean deleted, String clientName, String loanType, Pageable pageable);
	
	Page<Loan> findByDeletedAndStatusAndClientNameContainingIgnoreCaseAndLoanType(
			boolean deleted, LoanStatus status, String clientName, String loanType, Pageable pageable);
}

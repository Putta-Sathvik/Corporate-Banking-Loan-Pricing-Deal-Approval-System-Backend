package com.banking_system.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.banking_system.exception.LoanNotFoundException;
import com.banking_system.model.Loan;
import com.banking_system.model.LoanStatus;
import com.banking_system.model.User;
import com.banking_system.model.UserRole;
import com.banking_system.model.dto.LoanResponse;
import com.banking_system.repository.LoanRepository;

@ExtendWith(MockitoExtension.class)
class LoanServiceDeleteTest {

	@Mock
	private LoanRepository loanRepository;

	@Mock
	private PricingService pricingService;

	@InjectMocks
	private LoanService loanService;

	@Test
	void deleteLoan_existingLoan_softDeletesSuccessfully() {
		ObjectId loanId = new ObjectId();
		ObjectId userId = new ObjectId();
		User user = new User(userId, "user@test.com", "hash", UserRole.USER, true, Instant.now(), Instant.now());
		
		Loan loan = new Loan(
				loanId, "Client", "TermLoan", 10000.0, 10.0, 12, null,
				LoanStatus.DRAFT, null, null, userId, userId, null, null,
				Instant.now(), Instant.now(), false, null
		);

		when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));
		when(loanRepository.save(any(Loan.class))).thenAnswer(inv -> {
			Loan savedLoan = inv.getArgument(0);
			assertThat(savedLoan.isDeleted()).isTrue();
			assertThat(savedLoan.getDeletedAt()).isNotNull();
			assertThat(savedLoan.getActions()).isNotEmpty();
			return savedLoan;
		});

		LoanResponse response = loanService.deleteLoan(loanId.toHexString(), user);

		assertThat(response).isNotNull();
		verify(loanRepository).save(any(Loan.class));
	}

	@Test
	void deleteLoan_alreadyDeleted_throwsNotFoundException() {
		ObjectId loanId = new ObjectId();
		User user = new User(new ObjectId(), "user@test.com", "hash", UserRole.USER, true, Instant.now(), Instant.now());
		
		Loan loan = new Loan(
				loanId, "Client", "TermLoan", 10000.0, 10.0, 12, null,
				LoanStatus.DRAFT, null, null, new ObjectId(), new ObjectId(), null, null,
				Instant.now(), Instant.now(), true, Instant.now()
		);

		when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));

		assertThatThrownBy(() -> loanService.deleteLoan(loanId.toHexString(), user))
				.isInstanceOf(LoanNotFoundException.class);
	}

	@Test
	void deleteLoan_nonExistentLoan_throwsNotFoundException() {
		ObjectId loanId = new ObjectId();
		User user = new User(new ObjectId(), "user@test.com", "hash", UserRole.USER, true, Instant.now(), Instant.now());

		when(loanRepository.findById(loanId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> loanService.deleteLoan(loanId.toHexString(), user))
				.isInstanceOf(LoanNotFoundException.class);
	}

	@Test
	void getAllLoans_includeDeletedFalse_returnsOnlyActiveLoans() {
		Loan activeLoan = new Loan(
				new ObjectId(), "Client1", "TermLoan", 10000.0, 10.0, 12, null,
				LoanStatus.DRAFT, null, null, new ObjectId(), new ObjectId(), null, null,
				Instant.now(), Instant.now(), false, null
		);
		
		Loan deletedLoan = new Loan(
				new ObjectId(), "Client2", "TermLoan", 20000.0, 11.0, 24, null,
				LoanStatus.DRAFT, null, null, new ObjectId(), new ObjectId(), null, null,
				Instant.now(), Instant.now(), true, Instant.now()
		);

		when(loanRepository.findByDeletedFalse()).thenReturn(List.of(activeLoan));

		List<LoanResponse> loans = loanService.getAllLoans(false);

		assertThat(loans).hasSize(1);
		assertThat(loans.get(0).clientName()).isEqualTo("Client1");
	}

	@Test
	void getAllLoans_includeDeletedTrue_returnsAllLoans() {
		Loan activeLoan = new Loan(
				new ObjectId(), "Client1", "TermLoan", 10000.0, 10.0, 12, null,
				LoanStatus.DRAFT, null, null, new ObjectId(), new ObjectId(), null, null,
				Instant.now(), Instant.now(), false, null
		);
		
		Loan deletedLoan = new Loan(
				new ObjectId(), "Client2", "TermLoan", 20000.0, 11.0, 24, null,
				LoanStatus.DRAFT, null, null, new ObjectId(), new ObjectId(), null, null,
				Instant.now(), Instant.now(), true, Instant.now()
		);

		when(loanRepository.findAll()).thenReturn(List.of(activeLoan, deletedLoan));

		List<LoanResponse> loans = loanService.getAllLoans(true);

		assertThat(loans).hasSize(2);
	}

	@Test
	void getAllLoans_defaultParameter_returnsOnlyActiveLoans() {
		Loan activeLoan = new Loan(
				new ObjectId(), "Client1", "TermLoan", 10000.0, 10.0, 12, null,
				LoanStatus.DRAFT, null, null, new ObjectId(), new ObjectId(), null, null,
				Instant.now(), Instant.now(), false, null
		);

		when(loanRepository.findByDeletedFalse()).thenReturn(List.of(activeLoan));

		List<LoanResponse> loans = loanService.getAllLoans();

		assertThat(loans).hasSize(1);
		verify(loanRepository).findByDeletedFalse();
	}
}

package com.banking_system.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.banking_system.exception.LoanEditNotAllowedException;
import com.banking_system.exception.LoanNotFoundException;
import com.banking_system.exception.StatusChangeNotAllowedException;
import com.banking_system.model.Loan;
import com.banking_system.model.LoanStatus;
import com.banking_system.model.User;
import com.banking_system.model.UserRole;
import com.banking_system.model.dto.ChangeStatusRequest;
import com.banking_system.model.dto.LoanResponse;
import com.banking_system.repository.LoanRepository;

@ExtendWith(MockitoExtension.class)
class LoanServiceStatusTest {

	@Mock
	private LoanRepository loanRepository;

	@Mock
	private PricingService pricingService;

	@InjectMocks
	private LoanService loanService;

	@Test
	void changeStatus_draftToSubmitted_userRole_success() {
		ObjectId loanId = new ObjectId();
		ObjectId userId = new ObjectId();
		User user = new User(userId, "user@test.com", "hash", UserRole.USER, true, Instant.now(), Instant.now());
		
		Loan loan = new Loan(
				loanId, "Client", "TermLoan", 10000.0, 10.0, 12, null,
				LoanStatus.DRAFT, null, null, userId, userId, null, null,
				Instant.now(), Instant.now(), false, null
		);

		when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));
		when(loanRepository.save(any(Loan.class))).thenAnswer(inv -> inv.getArgument(0));

		ChangeStatusRequest request = new ChangeStatusRequest("SUBMITTED", "Ready for review");
		LoanResponse response = loanService.changeStatus(loanId.toHexString(), request, user);

		assertThat(response.status()).isEqualTo(LoanStatus.SUBMITTED);
		verify(loanRepository).save(any(Loan.class));
	}

	@Test
	void changeStatus_submittedToUnderReview_adminRole_success() {
		ObjectId loanId = new ObjectId();
		ObjectId adminId = new ObjectId();
		User admin = new User(adminId, "admin@test.com", "hash", UserRole.ADMIN, true, Instant.now(), Instant.now());
		
		Loan loan = new Loan(
				loanId, "Client", "TermLoan", 10000.0, 10.0, 12, null,
				LoanStatus.SUBMITTED, null, null, new ObjectId(), adminId, null, null,
				Instant.now(), Instant.now(), false, null
		);

		when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));
		when(loanRepository.save(any(Loan.class))).thenAnswer(inv -> inv.getArgument(0));

		ChangeStatusRequest request = new ChangeStatusRequest("UNDER_REVIEW", "Starting review");
		LoanResponse response = loanService.changeStatus(loanId.toHexString(), request, admin);

		assertThat(response.status()).isEqualTo(LoanStatus.UNDER_REVIEW);
	}

	@Test
	void changeStatus_underReviewToApproved_adminRole_setsApprovalMetadata() {
		ObjectId loanId = new ObjectId();
		ObjectId adminId = new ObjectId();
		User admin = new User(adminId, "admin@test.com", "hash", UserRole.ADMIN, true, Instant.now(), Instant.now());
		
		Loan loan = new Loan(
				loanId, "Client", "TermLoan", 10000.0, 10.0, 12, null,
				LoanStatus.UNDER_REVIEW, null, null, new ObjectId(), adminId, null, null,
				Instant.now(), Instant.now(), false, null
		);

		when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));
		when(loanRepository.save(any(Loan.class))).thenAnswer(inv -> inv.getArgument(0));

		ChangeStatusRequest request = new ChangeStatusRequest("APPROVED", "Loan approved");
		LoanResponse response = loanService.changeStatus(loanId.toHexString(), request, admin);

		assertThat(response.status()).isEqualTo(LoanStatus.APPROVED);
		assertThat(response.approvedBy()).isEqualTo(adminId.toHexString());
		assertThat(response.approvedAt()).isNotNull();
	}

	@Test
	void changeStatus_underReviewToRejected_adminRole_success() {
		ObjectId loanId = new ObjectId();
		ObjectId adminId = new ObjectId();
		User admin = new User(adminId, "admin@test.com", "hash", UserRole.ADMIN, true, Instant.now(), Instant.now());
		
		Loan loan = new Loan(
				loanId, "Client", "TermLoan", 10000.0, 10.0, 12, null,
				LoanStatus.UNDER_REVIEW, null, null, new ObjectId(), adminId, null, null,
				Instant.now(), Instant.now(), false, null
		);

		when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));
		when(loanRepository.save(any(Loan.class))).thenAnswer(inv -> inv.getArgument(0));

		ChangeStatusRequest request = new ChangeStatusRequest("REJECTED", "Does not meet criteria");
		LoanResponse response = loanService.changeStatus(loanId.toHexString(), request, admin);

		assertThat(response.status()).isEqualTo(LoanStatus.REJECTED);
	}

	@Test
	void changeStatus_invalidTransition_throwsException() {
		ObjectId loanId = new ObjectId();
		User admin = new User(new ObjectId(), "admin@test.com", "hash", UserRole.ADMIN, true, Instant.now(), Instant.now());
		
		Loan loan = new Loan(
				loanId, "Client", "TermLoan", 10000.0, 10.0, 12, null,
				LoanStatus.DRAFT, null, null, new ObjectId(), new ObjectId(), null, null,
				Instant.now(), Instant.now(), false, null
		);

		when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));

		ChangeStatusRequest request = new ChangeStatusRequest("APPROVED", "Skip steps");

		assertThatThrownBy(() -> loanService.changeStatus(loanId.toHexString(), request, admin))
				.isInstanceOf(StatusChangeNotAllowedException.class)
				.hasMessageContaining("Cannot transition");
	}

	@Test
	void changeStatus_userTriesToReview_throwsException() {
		ObjectId loanId = new ObjectId();
		User user = new User(new ObjectId(), "user@test.com", "hash", UserRole.USER, true, Instant.now(), Instant.now());
		
		Loan loan = new Loan(
				loanId, "Client", "TermLoan", 10000.0, 10.0, 12, null,
				LoanStatus.SUBMITTED, null, null, new ObjectId(), new ObjectId(), null, null,
				Instant.now(), Instant.now(), false, null
		);

		when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));

		ChangeStatusRequest request = new ChangeStatusRequest("UNDER_REVIEW", "User attempting review");

		assertThatThrownBy(() -> loanService.changeStatus(loanId.toHexString(), request, user))
				.isInstanceOf(StatusChangeNotAllowedException.class)
				.hasMessageContaining("Users can only submit");
	}

	@Test
	void changeStatus_sameStatus_throwsException() {
		ObjectId loanId = new ObjectId();
		User admin = new User(new ObjectId(), "admin@test.com", "hash", UserRole.ADMIN, true, Instant.now(), Instant.now());
		
		Loan loan = new Loan(
				loanId, "Client", "TermLoan", 10000.0, 10.0, 12, null,
				LoanStatus.SUBMITTED, null, null, new ObjectId(), new ObjectId(), null, null,
				Instant.now(), Instant.now(), false, null
		);

		when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));

		ChangeStatusRequest request = new ChangeStatusRequest("SUBMITTED", "No change");

		assertThatThrownBy(() -> loanService.changeStatus(loanId.toHexString(), request, admin))
				.isInstanceOf(StatusChangeNotAllowedException.class)
				.hasMessageContaining("Cannot transition");
	}

	@Test
	void changeStatus_approvedToSubmitted_throwsException() {
		ObjectId loanId = new ObjectId();
		User admin = new User(new ObjectId(), "admin@test.com", "hash", UserRole.ADMIN, true, Instant.now(), Instant.now());
		
		Loan loan = new Loan(
				loanId, "Client", "TermLoan", 10000.0, 10.0, 12, null,
				LoanStatus.APPROVED, null, null, new ObjectId(), new ObjectId(), null, null,
				Instant.now(), Instant.now(), false, null
		);

		when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));

		ChangeStatusRequest request = new ChangeStatusRequest("SUBMITTED", "Trying to revert");

		assertThatThrownBy(() -> loanService.changeStatus(loanId.toHexString(), request, admin))
				.isInstanceOf(StatusChangeNotAllowedException.class)
				.hasMessageContaining("Cannot transition");
	}

	@Test
	void changeStatus_deletedLoan_throwsNotFoundException() {
		ObjectId loanId = new ObjectId();
		User admin = new User(new ObjectId(), "admin@test.com", "hash", UserRole.ADMIN, true, Instant.now(), Instant.now());
		
		Loan loan = new Loan(
				loanId, "Client", "TermLoan", 10000.0, 10.0, 12, null,
				LoanStatus.DRAFT, null, null, new ObjectId(), new ObjectId(), null, null,
				Instant.now(), Instant.now(), true, Instant.now()
		);

		when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));

		ChangeStatusRequest request = new ChangeStatusRequest("SUBMITTED", "Deleted loan");

		assertThatThrownBy(() -> loanService.changeStatus(loanId.toHexString(), request, admin))
				.isInstanceOf(LoanNotFoundException.class);
	}
}

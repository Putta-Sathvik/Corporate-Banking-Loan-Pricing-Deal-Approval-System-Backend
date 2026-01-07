package com.banking_system.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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

import com.banking_system.exception.LoanEditNotAllowedException;
import com.banking_system.exception.LoanNotFoundException;
import com.banking_system.model.Loan;
import com.banking_system.model.Loan.Financials;
import com.banking_system.model.LoanStatus;
import com.banking_system.model.User;
import com.banking_system.model.UserRole;
import com.banking_system.model.dto.CreateLoanRequest;
import com.banking_system.model.dto.LoanResponse;
import com.banking_system.model.dto.PricingResponse;
import com.banking_system.model.dto.UpdateLoanAdminRequest;
import com.banking_system.model.dto.UpdateLoanRequest;
import com.banking_system.repository.LoanRepository;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

	@Mock
	LoanRepository loanRepository;

	@Mock
	PricingService pricingService;

	@InjectMocks
	LoanService loanService;

	@Test
	void createLoan_success_returnsLoanResponse() {
		User user = new User(new ObjectId(), "user@bank.com", "hash", UserRole.USER, true, Instant.now(), Instant.now());
		Financials financials = new Financials(120000000.0, 14000000.0, "A");
		CreateLoanRequest request = new CreateLoanRequest(
				"OmniTech Pvt Ltd",
				"TermLoan",
				50000000.0,
				11.5,
				36,
				financials
		);

		when(loanRepository.save(any(Loan.class))).thenAnswer(inv -> {
			Loan l = inv.getArgument(0);
			l.setId(new ObjectId());
			return l;
		});

		LoanResponse response = loanService.createLoan(request, user);

		assertThat(response.clientName()).isEqualTo("OmniTech Pvt Ltd");
		assertThat(response.status()).isEqualTo(LoanStatus.DRAFT);
		assertThat(response.requestedAmount()).isEqualTo(50000000.0);
	}

	@Test
	void getLoanById_found_returnsLoan() {
		ObjectId id = new ObjectId();
		Loan loan = new Loan(
				id, "Client", "TermLoan", 10000.0, 10.0, 12, null,
				LoanStatus.DRAFT, null, null, new ObjectId(), new ObjectId(), null, null,
				Instant.now(), Instant.now(), false, null
		);

		when(loanRepository.findById(id)).thenReturn(Optional.of(loan));

		LoanResponse response = loanService.getLoanById(id.toHexString());

		assertThat(response.id()).isEqualTo(id.toHexString());
		assertThat(response.clientName()).isEqualTo("Client");
	}

	@Test
	void getLoanById_notFound_throws() {
		ObjectId id = new ObjectId();
		when(loanRepository.findById(id)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> loanService.getLoanById(id.toHexString()))
				.isInstanceOf(LoanNotFoundException.class);
	}

	@Test
	void getAllLoans_returnsNonDeletedLoans() {
		Loan l1 = new Loan(
				new ObjectId(), "Client1", "WorkingCapital", 5000.0, 9.0, 6, null,
				LoanStatus.DRAFT, null, null, new ObjectId(), new ObjectId(), null, null,
				Instant.now(), Instant.now(), false, null
		);
		Loan l2 = new Loan(
				new ObjectId(), "Client2", "Overdraft", 8000.0, 8.5, 12, null,
				LoanStatus.SUBMITTED, null, null, new ObjectId(), new ObjectId(), null, null,
				Instant.now(), Instant.now(), false, null
		);

		when(loanRepository.findByDeletedFalse()).thenReturn(List.of(l1, l2));

		List<LoanResponse> loans = loanService.getAllLoans();

		assertThat(loans).hasSize(2);
		assertThat(loans.get(0).clientName()).isEqualTo("Client1");
		assertThat(loans.get(1).clientName()).isEqualTo("Client2");
	}

	@Test
	void updateLoan_userDraftStatus_success() {
		ObjectId id = new ObjectId();
		User user = new User(new ObjectId(), "user@bank.com", "hash", UserRole.USER, true, Instant.now(), Instant.now());
		Loan loan = new Loan(
				id, "OldClient", "TermLoan", 10000.0, 10.0, 12, null,
				LoanStatus.DRAFT, null, null, user.getId(), user.getId(), null, null,
				Instant.now(), Instant.now(), false, null
		);

		when(loanRepository.findById(id)).thenReturn(Optional.of(loan));
		when(loanRepository.save(any(Loan.class))).thenAnswer(inv -> inv.getArgument(0));

		UpdateLoanRequest request = new UpdateLoanRequest("NewClient", null, null, null, null, null);
		LoanResponse response = loanService.updateLoan(id.toHexString(), request, user);

		assertThat(response.clientName()).isEqualTo("NewClient");
	}

	@Test
	void updateLoan_userNonDraftStatus_throws() {
		ObjectId id = new ObjectId();
		User user = new User(new ObjectId(), "user@bank.com", "hash", UserRole.USER, true, Instant.now(), Instant.now());
		Loan loan = new Loan(
				id, "Client", "TermLoan", 10000.0, 10.0, 12, null,
				LoanStatus.SUBMITTED, null, null, user.getId(), user.getId(), null, null,
				Instant.now(), Instant.now(), false, null
		);

		when(loanRepository.findById(id)).thenReturn(Optional.of(loan));

		UpdateLoanRequest request = new UpdateLoanRequest("NewClient", null, null, null, null, null);

		assertThatThrownBy(() -> loanService.updateLoan(id.toHexString(), request, user))
				.isInstanceOf(LoanEditNotAllowedException.class)
				.hasMessageContaining("DRAFT");
	}

	@Test
	void updateLoanAdmin_adminUser_updatesSensitiveFields() {
		ObjectId id = new ObjectId();
		User admin = new User(new ObjectId(), "admin@bank.com", "hash", UserRole.ADMIN, true, Instant.now(), Instant.now());
		Loan loan = new Loan(
				id, "Client", "TermLoan", 10000.0, 10.0, 12, null,
				LoanStatus.SUBMITTED, null, null, admin.getId(), admin.getId(), null, null,
				Instant.now(), Instant.now(), false, null
		);

		when(loanRepository.findById(id)).thenReturn(Optional.of(loan));
		when(loanRepository.save(any(Loan.class))).thenAnswer(inv -> inv.getArgument(0));

		UpdateLoanAdminRequest request = new UpdateLoanAdminRequest(9000.0, 10.5);
		LoanResponse response = loanService.updateLoanAdmin(id.toHexString(), request, admin);

		assertThat(response.sanctionedAmount()).isEqualTo(9000.0);
		assertThat(response.approvedInterestRate()).isEqualTo(10.5);
	}

	@Test
	void calculatePricing_existingLoan_returnsPricing() {
		ObjectId id = new ObjectId();
		Financials financials = new Financials(120000000.0, 14000000.0, "A");
		Loan loan = new Loan(
				id, "Client", "TermLoan", 50000000.0, 11.5, 36, financials,
				LoanStatus.DRAFT, null, null, new ObjectId(), new ObjectId(), null, null,
				Instant.now(), Instant.now(), false, null
		);

		when(loanRepository.findById(id)).thenReturn(Optional.of(loan));
		when(pricingService.calculatePricing(any())).thenReturn(
				new PricingResponse(11.5, 1650000.0, 9400000.0, "LOW")
		);

		PricingResponse response = loanService.calculatePricing(id.toHexString());

		assertThat(response.recommendedRate()).isEqualTo(11.5);
		assertThat(response.riskCategory()).isEqualTo("LOW");
	}
}

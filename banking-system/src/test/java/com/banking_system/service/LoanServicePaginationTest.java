package com.banking_system.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.banking_system.model.Loan;
import com.banking_system.model.LoanStatus;
import com.banking_system.model.dto.LoanResponse;
import com.banking_system.repository.LoanRepository;

@ExtendWith(MockitoExtension.class)
class LoanServicePaginationTest {

	@Mock
	private LoanRepository loanRepository;

	@Mock
	private PricingService pricingService;

	@InjectMocks
	private LoanService loanService;

	@Test
	void getAllLoans_withPagination_returnsPagedResults() {
		Loan loan1 = new Loan(
				new ObjectId(), "Client1", "TermLoan", 10000.0, 10.0, 12, null,
				LoanStatus.DRAFT, null, null, new ObjectId(), new ObjectId(), null, null,
				Instant.now(), Instant.now(), false, null
		);
		Loan loan2 = new Loan(
				new ObjectId(), "Client2", "WorkingCapital", 20000.0, 11.0, 24, null,
				LoanStatus.SUBMITTED, null, null, new ObjectId(), new ObjectId(), null, null,
				Instant.now(), Instant.now(), false, null
		);

		Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
		Page<Loan> loanPage = new PageImpl<>(List.of(loan1, loan2), pageable, 2);

		when(loanRepository.findByDeleted(eq(false), any(Pageable.class))).thenReturn(loanPage);

		Page<LoanResponse> result = loanService.getAllLoans(false, null, null, null, pageable);

		assertThat(result.getContent()).hasSize(2);
		assertThat(result.getTotalElements()).isEqualTo(2);
		assertThat(result.getNumber()).isEqualTo(0);
		verify(loanRepository).findByDeleted(eq(false), any(Pageable.class));
	}

	@Test
	void getAllLoans_withStatusFilter_returnsFilteredResults() {
		Loan loan = new Loan(
				new ObjectId(), "Client1", "TermLoan", 10000.0, 10.0, 12, null,
				LoanStatus.APPROVED, null, null, new ObjectId(), new ObjectId(), null, null,
				Instant.now(), Instant.now(), false, null
		);

		Pageable pageable = PageRequest.of(0, 10);
		Page<Loan> loanPage = new PageImpl<>(List.of(loan), pageable, 1);

		when(loanRepository.findByDeletedAndStatus(eq(false), eq(LoanStatus.APPROVED), any(Pageable.class)))
				.thenReturn(loanPage);

		Page<LoanResponse> result = loanService.getAllLoans(false, "APPROVED", null, null, pageable);

		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().get(0).status()).isEqualTo(LoanStatus.APPROVED);
		verify(loanRepository).findByDeletedAndStatus(eq(false), eq(LoanStatus.APPROVED), any(Pageable.class));
	}

	@Test
	void getAllLoans_withClientNameFilter_returnsFilteredResults() {
		Loan loan = new Loan(
				new ObjectId(), "ABC Corp", "TermLoan", 10000.0, 10.0, 12, null,
				LoanStatus.DRAFT, null, null, new ObjectId(), new ObjectId(), null, null,
				Instant.now(), Instant.now(), false, null
		);

		Pageable pageable = PageRequest.of(0, 10);
		Page<Loan> loanPage = new PageImpl<>(List.of(loan), pageable, 1);

		when(loanRepository.findByDeletedAndClientNameContainingIgnoreCase(
				eq(false), eq("ABC"), any(Pageable.class))).thenReturn(loanPage);

		Page<LoanResponse> result = loanService.getAllLoans(false, null, "ABC", null, pageable);

		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().get(0).clientName()).isEqualTo("ABC Corp");
	}

	@Test
	void getAllLoans_withLoanTypeFilter_returnsFilteredResults() {
		Loan loan = new Loan(
				new ObjectId(), "Client1", "WorkingCapital", 10000.0, 10.0, 12, null,
				LoanStatus.DRAFT, null, null, new ObjectId(), new ObjectId(), null, null,
				Instant.now(), Instant.now(), false, null
		);

		Pageable pageable = PageRequest.of(0, 10);
		Page<Loan> loanPage = new PageImpl<>(List.of(loan), pageable, 1);

		when(loanRepository.findByDeletedAndLoanType(eq(false), eq("WorkingCapital"), any(Pageable.class)))
				.thenReturn(loanPage);

		Page<LoanResponse> result = loanService.getAllLoans(false, null, null, "WorkingCapital", pageable);

		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().get(0).loanType()).isEqualTo("WorkingCapital");
	}

	@Test
	void getAllLoans_withMultipleFilters_returnsFilteredResults() {
		Loan loan = new Loan(
				new ObjectId(), "XYZ Corp", "TermLoan", 10000.0, 10.0, 12, null,
				LoanStatus.SUBMITTED, null, null, new ObjectId(), new ObjectId(), null, null,
				Instant.now(), Instant.now(), false, null
		);

		Pageable pageable = PageRequest.of(0, 10);
		Page<Loan> loanPage = new PageImpl<>(List.of(loan), pageable, 1);

		when(loanRepository.findByDeletedAndStatusAndClientNameContainingIgnoreCaseAndLoanType(
				eq(false), eq(LoanStatus.SUBMITTED), eq("XYZ"), eq("TermLoan"), any(Pageable.class)))
				.thenReturn(loanPage);

		Page<LoanResponse> result = loanService.getAllLoans(
				false, "SUBMITTED", "XYZ", "TermLoan", pageable);

		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().get(0).clientName()).isEqualTo("XYZ Corp");
		assertThat(result.getContent().get(0).status()).isEqualTo(LoanStatus.SUBMITTED);
		assertThat(result.getContent().get(0).loanType()).isEqualTo("TermLoan");
	}

	@Test
	void getAllLoans_withInvalidStatus_ignoresFilter() {
		Loan loan = new Loan(
				new ObjectId(), "Client1", "TermLoan", 10000.0, 10.0, 12, null,
				LoanStatus.DRAFT, null, null, new ObjectId(), new ObjectId(), null, null,
				Instant.now(), Instant.now(), false, null
		);

		Pageable pageable = PageRequest.of(0, 10);
		Page<Loan> loanPage = new PageImpl<>(List.of(loan), pageable, 1);

		when(loanRepository.findByDeleted(eq(false), any(Pageable.class))).thenReturn(loanPage);

		Page<LoanResponse> result = loanService.getAllLoans(false, "INVALID_STATUS", null, null, pageable);

		assertThat(result.getContent()).hasSize(1);
		verify(loanRepository).findByDeleted(eq(false), any(Pageable.class));
	}

	@Test
	void getAllLoans_withIncludeDeletedTrue_returnsDeletedLoans() {
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

		Pageable pageable = PageRequest.of(0, 10);
		Page<Loan> loanPage = new PageImpl<>(List.of(activeLoan, deletedLoan), pageable, 2);

		when(loanRepository.findByDeleted(eq(true), any(Pageable.class))).thenReturn(loanPage);

		Page<LoanResponse> result = loanService.getAllLoans(true, null, null, null, pageable);

		assertThat(result.getContent()).hasSize(2);
		verify(loanRepository).findByDeleted(eq(true), any(Pageable.class));
	}

	@Test
	void getAllLoans_secondPage_returnsCorrectPage() {
		Loan loan = new Loan(
				new ObjectId(), "Client3", "TermLoan", 10000.0, 10.0, 12, null,
				LoanStatus.DRAFT, null, null, new ObjectId(), new ObjectId(), null, null,
				Instant.now(), Instant.now(), false, null
		);

		Pageable pageable = PageRequest.of(1, 10);
		Page<Loan> loanPage = new PageImpl<>(List.of(loan), pageable, 11);

		when(loanRepository.findByDeleted(eq(false), any(Pageable.class))).thenReturn(loanPage);

		Page<LoanResponse> result = loanService.getAllLoans(false, null, null, null, pageable);

		assertThat(result.getNumber()).isEqualTo(1);
		assertThat(result.getTotalElements()).isEqualTo(11);
		assertThat(result.getTotalPages()).isEqualTo(2);
	}
}

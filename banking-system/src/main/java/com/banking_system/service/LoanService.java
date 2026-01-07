package com.banking_system.service;

import java.time.Instant;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.banking_system.exception.LoanEditNotAllowedException;
import com.banking_system.exception.LoanNotFoundException;
import com.banking_system.exception.StatusChangeNotAllowedException;
import com.banking_system.model.Loan;
import com.banking_system.model.LoanStatus;
import com.banking_system.model.User;
import com.banking_system.model.UserRole;
import com.banking_system.model.dto.ChangeStatusRequest;
import com.banking_system.model.dto.CreateLoanRequest;
import com.banking_system.model.dto.LoanResponse;
import com.banking_system.model.dto.PricingRequest;
import com.banking_system.model.dto.PricingResponse;
import com.banking_system.model.dto.UpdateLoanAdminRequest;
import com.banking_system.model.dto.UpdateLoanRequest;
import com.banking_system.repository.LoanRepository;

@Service
public class LoanService {

	private final LoanRepository loanRepository;
	private final PricingService pricingService;

	public LoanService(LoanRepository loanRepository, PricingService pricingService) {
		this.loanRepository = loanRepository;
		this.pricingService = pricingService;
	}

	public LoanResponse createLoan(CreateLoanRequest request, User currentUser) {
		Instant now = Instant.now();
		Loan loan = new Loan(
				null,
				request.clientName(),
				request.loanType(),
				request.requestedAmount(),
				request.proposedInterestRate(),
				request.tenureMonths(),
				request.financials(),
				LoanStatus.DRAFT,
				null,
				null,
				currentUser.getId(),
				currentUser.getId(),
				null,
				null,
				now,
				now,
				false,
				null
		);

		Loan saved = loanRepository.save(loan);
		return toResponse(saved);
	}

	public LoanResponse getLoanById(String loanId) {
		ObjectId objectId = parseObjectId(loanId);
		Loan loan = loanRepository.findById(objectId)
				.orElseThrow(() -> new LoanNotFoundException(loanId));

		if (loan.isDeleted()) {
			throw new LoanNotFoundException(loanId);
		}

		return toResponse(loan);
	}

	public List<LoanResponse> getAllLoans() {
		return getAllLoans(false);
	}

	public List<LoanResponse> getAllLoans(boolean includeDeleted) {
		if (includeDeleted) {
			return loanRepository.findAll().stream()
					.map(this::toResponse)
					.toList();
		}
		return loanRepository.findByDeletedFalse().stream()
				.map(this::toResponse)
				.toList();
	}

	public Page<LoanResponse> getAllLoans(
			boolean includeDeleted,
			String status,
			String clientName,
			String loanType,
			Pageable pageable) {
		
		LoanStatus loanStatus = null;
		if (status != null && !status.isBlank()) {
			try {
				loanStatus = LoanStatus.valueOf(status.toUpperCase());
			} catch (IllegalArgumentException e) {
				// Invalid status, ignore filter
			}
		}

		boolean hasStatus = loanStatus != null;
		boolean hasClientName = clientName != null && !clientName.isBlank();
		boolean hasLoanType = loanType != null && !loanType.isBlank();

		Page<Loan> loanPage;

		if (hasStatus && hasClientName && hasLoanType) {
			loanPage = loanRepository.findByDeletedAndStatusAndClientNameContainingIgnoreCaseAndLoanType(
					includeDeleted, loanStatus, clientName, loanType, pageable);
		} else if (hasStatus && hasClientName) {
			loanPage = loanRepository.findByDeletedAndStatusAndClientNameContainingIgnoreCase(
					includeDeleted, loanStatus, clientName, pageable);
		} else if (hasStatus && hasLoanType) {
			loanPage = loanRepository.findByDeletedAndStatusAndLoanType(
					includeDeleted, loanStatus, loanType, pageable);
		} else if (hasClientName && hasLoanType) {
			loanPage = loanRepository.findByDeletedAndClientNameContainingIgnoreCaseAndLoanType(
					includeDeleted, clientName, loanType, pageable);
		} else if (hasStatus) {
			loanPage = loanRepository.findByDeletedAndStatus(includeDeleted, loanStatus, pageable);
		} else if (hasClientName) {
			loanPage = loanRepository.findByDeletedAndClientNameContainingIgnoreCase(
					includeDeleted, clientName, pageable);
		} else if (hasLoanType) {
			loanPage = loanRepository.findByDeletedAndLoanType(includeDeleted, loanType, pageable);
		} else {
			loanPage = loanRepository.findByDeleted(includeDeleted, pageable);
		}

		return loanPage.map(this::toResponse);
	}

	public LoanResponse deleteLoan(String loanId, User currentUser) {
		ObjectId objectId = parseObjectId(loanId);
		Loan loan = loanRepository.findById(objectId)
				.orElseThrow(() -> new LoanNotFoundException(loanId));

		if (loan.isDeleted()) {
			throw new LoanNotFoundException(loanId);
		}

		Instant now = Instant.now();
		loan.setDeleted(true);
		loan.setDeletedAt(now);
		loan.setUpdatedBy(currentUser.getId());
		loan.setUpdatedAt(now);

		// Add audit trail action
		Loan.LoanAction action = new Loan.LoanAction(
				currentUser.getId(),
				"DELETED",
				"Loan soft deleted",
				now
		);
		loan.addAction(action);

		Loan savedLoan = loanRepository.save(loan);
		return toResponse(savedLoan);
	}

	public LoanResponse updateLoan(String loanId, UpdateLoanRequest request, User currentUser) {
		ObjectId objectId = parseObjectId(loanId);
		Loan loan = loanRepository.findById(objectId)
				.orElseThrow(() -> new LoanNotFoundException(loanId));

		if (loan.isDeleted()) {
			throw new LoanNotFoundException(loanId);
		}

		// B1: USER can only edit DRAFT loans
		if (currentUser.getRole() == UserRole.USER && loan.getStatus() != LoanStatus.DRAFT) {
			throw new LoanEditNotAllowedException("USER can only edit loans in DRAFT status");
		}

		// Update non-sensitive fields
		if (request.clientName() != null) {
			loan.setClientName(request.clientName());
		}
		if (request.loanType() != null) {
			loan.setLoanType(request.loanType());
		}
		if (request.requestedAmount() != null) {
			loan.setRequestedAmount(request.requestedAmount());
		}
		if (request.proposedInterestRate() != null) {
			loan.setProposedInterestRate(request.proposedInterestRate());
		}
		if (request.tenureMonths() != null) {
			loan.setTenureMonths(request.tenureMonths());
		}
		if (request.financials() != null) {
			loan.setFinancials(request.financials());
		}

		loan.setUpdatedBy(currentUser.getId());
		loan.setUpdatedAt(Instant.now());

		Loan saved = loanRepository.save(loan);
		return toResponse(saved);
	}

	public LoanResponse updateLoanAdmin(String loanId, UpdateLoanAdminRequest request, User currentUser) {
		ObjectId objectId = parseObjectId(loanId);
		Loan loan = loanRepository.findById(objectId)
				.orElseThrow(() -> new LoanNotFoundException(loanId));

		if (loan.isDeleted()) {
			throw new LoanNotFoundException(loanId);
		}

		// ADMIN can update sensitive fields anytime
		if (request.sanctionedAmount() != null) {
			loan.setSanctionedAmount(request.sanctionedAmount());
		}
		if (request.approvedInterestRate() != null) {
			loan.setApprovedInterestRate(request.approvedInterestRate());
		}

		loan.setUpdatedBy(currentUser.getId());
		loan.setUpdatedAt(Instant.now());

		Loan saved = loanRepository.save(loan);
		return toResponse(saved);
	}

	public PricingResponse calculatePricing(String loanId) {
		ObjectId objectId = parseObjectId(loanId);
		Loan loan = loanRepository.findById(objectId)
				.orElseThrow(() -> new LoanNotFoundException(loanId));

		if (loan.isDeleted()) {
			throw new LoanNotFoundException(loanId);
		}

		String rating = loan.getFinancials() != null ? loan.getFinancials().getRating() : "C";
		PricingRequest request = new PricingRequest(
				loan.getRequestedAmount(),
				loan.getProposedInterestRate(),
				loan.getTenureMonths(),
				rating
		);

		return pricingService.calculatePricing(request);
	}

	public LoanResponse changeStatus(String loanId, ChangeStatusRequest request, User currentUser) {
		ObjectId objectId = parseObjectId(loanId);
		Loan loan = loanRepository.findById(objectId)
				.orElseThrow(() -> new LoanNotFoundException(loanId));

		if (loan.isDeleted()) {
			throw new LoanNotFoundException(loanId);
		}

		LoanStatus newStatus;
		try {
			newStatus = LoanStatus.valueOf(request.status());
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Invalid status: " + request.status());
		}

		// Validate transition is allowed
		if (!isTransitionAllowed(loan.getStatus(), newStatus)) {
			throw new StatusChangeNotAllowedException(
					"Cannot transition from " + loan.getStatus() + " to " + newStatus);
		}

		// Role-based restrictions
		if (currentUser.getRole() == UserRole.USER) {
			// USER can only submit DRAFT loans
			if (loan.getStatus() != LoanStatus.DRAFT || newStatus != LoanStatus.SUBMITTED) {
				throw new StatusChangeNotAllowedException(
						"Users can only submit loans from DRAFT status");
			}
		}
		// ADMIN can perform all valid transitions

		// Update status and audit trail
		Instant now = Instant.now();
		loan.setStatus(newStatus);
		loan.setUpdatedBy(currentUser.getId());
		loan.setUpdatedAt(now);

		// Add audit trail action
		Loan.LoanAction action = new Loan.LoanAction(
				currentUser.getId(),
				"STATUS_CHANGE: " + newStatus,
				request.comments(),
				now
		);
		loan.addAction(action);

		// If approved, set approval metadata
		if (newStatus == LoanStatus.APPROVED) {
			loan.setApprovedBy(currentUser.getId());
			loan.setApprovedAt(now);
		}

		Loan savedLoan = loanRepository.save(loan);
		return toResponse(savedLoan);
	}

	private boolean isTransitionAllowed(LoanStatus currentStatus, LoanStatus newStatus) {
		if (currentStatus == newStatus) {
			return false; // No transition to same status
		}

		return switch (currentStatus) {
			case DRAFT -> newStatus == LoanStatus.SUBMITTED;
			case SUBMITTED -> newStatus == LoanStatus.UNDER_REVIEW;
			case UNDER_REVIEW -> newStatus == LoanStatus.APPROVED || newStatus == LoanStatus.REJECTED;
			case APPROVED, REJECTED -> false; // Terminal states
		};
	}

	private LoanResponse toResponse(Loan loan) {
		return new LoanResponse(
				loan.getId().toHexString(),
				loan.getClientName(),
				loan.getLoanType(),
				loan.getRequestedAmount(),
				loan.getProposedInterestRate(),
				loan.getTenureMonths(),
				loan.getFinancials(),
				loan.getStatus(),
				loan.getSanctionedAmount(),
				loan.getApprovedInterestRate(),
				loan.getCreatedBy() != null ? loan.getCreatedBy().toHexString() : null,
				loan.getUpdatedBy() != null ? loan.getUpdatedBy().toHexString() : null,
				loan.getApprovedBy() != null ? loan.getApprovedBy().toHexString() : null,
				loan.getApprovedAt(),
				loan.getCreatedAt(),
				loan.getUpdatedAt()
		);
	}

	private ObjectId parseObjectId(String id) {
		try {
			return new ObjectId(id);
		} catch (IllegalArgumentException e) {
			throw new LoanNotFoundException(id);
		}
	}
}

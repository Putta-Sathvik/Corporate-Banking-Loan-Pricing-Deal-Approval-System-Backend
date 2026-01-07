package com.banking_system.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.banking_system.model.User;
import com.banking_system.model.dto.ChangeStatusRequest;
import com.banking_system.model.dto.CreateLoanRequest;
import com.banking_system.model.dto.LoanResponse;
import com.banking_system.model.dto.PricingResponse;
import com.banking_system.model.dto.UpdateLoanAdminRequest;
import com.banking_system.model.dto.UpdateLoanRequest;
import com.banking_system.service.LoanService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/loans")
@Validated
public class LoanController {

	private final LoanService loanService;

	public LoanController(LoanService loanService) {
		this.loanService = loanService;
	}

	@PostMapping
	public ResponseEntity<LoanResponse> createLoan(
			@Valid @RequestBody CreateLoanRequest request,
			@AuthenticationPrincipal User currentUser) {
		LoanResponse created = loanService.createLoan(request, currentUser);
		return ResponseEntity
				.created(URI.create("/api/loans/" + created.id()))
				.body(created);
	}

	@GetMapping("/{id}")
	public ResponseEntity<LoanResponse> getLoan(@PathVariable String id) {
		LoanResponse loan = loanService.getLoanById(id);
		return ResponseEntity.ok(loan);
	}

	@GetMapping
	@PreAuthorize("hasRole('ADMIN') or !#includeDeleted")
	public ResponseEntity<List<LoanResponse>> getAllLoans(
			@RequestParam(defaultValue = "false") boolean includeDeleted) {
		List<LoanResponse> loans = loanService.getAllLoans(includeDeleted);
		return ResponseEntity.ok(loans);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<LoanResponse> deleteLoan(
			@PathVariable String id,
			@AuthenticationPrincipal User currentUser) {
		LoanResponse deleted = loanService.deleteLoan(id, currentUser);
		return ResponseEntity.ok(deleted);
	}

	@GetMapping("/paginated")
	@PreAuthorize("hasRole('ADMIN') or !#includeDeleted")
	public ResponseEntity<Page<LoanResponse>> getAllLoansPaginated(
			@RequestParam(defaultValue = "false") boolean includeDeleted,
			@RequestParam(required = false) String status,
			@RequestParam(required = false) String clientName,
			@RequestParam(required = false) String loanType,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "createdAt") String sortBy,
			@RequestParam(defaultValue = "DESC") String sortDirection) {
		
		Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") 
				? Sort.Direction.ASC 
				: Sort.Direction.DESC;
		Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
		
		Page<LoanResponse> loans = loanService.getAllLoans(
				includeDeleted, status, clientName, loanType, pageable);
		return ResponseEntity.ok(loans);
	}

	@PutMapping("/{id}")
	public ResponseEntity<LoanResponse> updateLoan(
			@PathVariable String id,
			@Valid @RequestBody UpdateLoanRequest request,
			@AuthenticationPrincipal User currentUser) {
		LoanResponse updated = loanService.updateLoan(id, request, currentUser);
		return ResponseEntity.ok(updated);
	}

	@PutMapping("/{id}/admin")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<LoanResponse> updateLoanAdmin(
			@PathVariable String id,
			@Valid @RequestBody UpdateLoanAdminRequest request,
			@AuthenticationPrincipal User currentUser) {
		LoanResponse updated = loanService.updateLoanAdmin(id, request, currentUser);
		return ResponseEntity.ok(updated);
	}

	@PatchMapping("/{id}/status")
	public ResponseEntity<LoanResponse> changeStatus(
			@PathVariable String id,
			@Valid @RequestBody ChangeStatusRequest request,
			@AuthenticationPrincipal User currentUser) {
		LoanResponse updated = loanService.changeStatus(id, request, currentUser);
		return ResponseEntity.ok(updated);
	}

	@GetMapping("/{id}/pricing")
	public ResponseEntity<PricingResponse> calculatePricing(@PathVariable String id) {
		PricingResponse pricing = loanService.calculatePricing(id);
		return ResponseEntity.ok(pricing);
	}
}

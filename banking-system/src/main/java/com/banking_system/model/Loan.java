package com.banking_system.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "loans")
public class Loan {
	@Id
	private ObjectId id;

	private String clientName;
	private String loanType;
	private Double requestedAmount;
	private Double proposedInterestRate;
	private Integer tenureMonths;

	// Embedded financials
	private Financials financials;

	private LoanStatus status;

	// Sensitive fields (admin-only, set in B2)
	private Double sanctionedAmount;
	private Double approvedInterestRate;

	// Audit fields
	private ObjectId createdBy;
	private ObjectId updatedBy;
	private ObjectId approvedBy;
	private Instant approvedAt;

	private Instant createdAt;
	private Instant updatedAt;

	// Audit trail for status changes
	private List<LoanAction> actions = new ArrayList<>();

	private boolean deleted;
	private Instant deletedAt;

	public Loan() {
	}

	public Loan(
			ObjectId id,
			String clientName,
			String loanType,
			Double requestedAmount,
			Double proposedInterestRate,
			Integer tenureMonths,
			Financials financials,
			LoanStatus status,
			Double sanctionedAmount,
			Double approvedInterestRate,
			ObjectId createdBy,
			ObjectId updatedBy,
			ObjectId approvedBy,
			Instant approvedAt,
			Instant createdAt,
			Instant updatedAt,
			boolean deleted,
			Instant deletedAt) {
		this.id = id;
		this.clientName = clientName;
		this.loanType = loanType;
		this.requestedAmount = requestedAmount;
		this.proposedInterestRate = proposedInterestRate;
		this.tenureMonths = tenureMonths;
		this.financials = financials;
		this.status = status;
		this.sanctionedAmount = sanctionedAmount;
		this.approvedInterestRate = approvedInterestRate;
		this.createdBy = createdBy;
		this.updatedBy = updatedBy;
		this.approvedBy = approvedBy;
		this.approvedAt = approvedAt;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.deleted = deleted;
		this.deletedAt = deletedAt;
	}

	// Getters and setters
	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	public String getClientName() {
		return clientName;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public String getLoanType() {
		return loanType;
	}

	public void setLoanType(String loanType) {
		this.loanType = loanType;
	}

	public Double getRequestedAmount() {
		return requestedAmount;
	}

	public void setRequestedAmount(Double requestedAmount) {
		this.requestedAmount = requestedAmount;
	}

	public Double getProposedInterestRate() {
		return proposedInterestRate;
	}

	public void setProposedInterestRate(Double proposedInterestRate) {
		this.proposedInterestRate = proposedInterestRate;
	}

	public Integer getTenureMonths() {
		return tenureMonths;
	}

	public void setTenureMonths(Integer tenureMonths) {
		this.tenureMonths = tenureMonths;
	}

	public Financials getFinancials() {
		return financials;
	}

	public void setFinancials(Financials financials) {
		this.financials = financials;
	}

	public LoanStatus getStatus() {
		return status;
	}

	public void setStatus(LoanStatus status) {
		this.status = status;
	}

	public Double getSanctionedAmount() {
		return sanctionedAmount;
	}

	public void setSanctionedAmount(Double sanctionedAmount) {
		this.sanctionedAmount = sanctionedAmount;
	}

	public Double getApprovedInterestRate() {
		return approvedInterestRate;
	}

	public void setApprovedInterestRate(Double approvedInterestRate) {
		this.approvedInterestRate = approvedInterestRate;
	}

	public ObjectId getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(ObjectId createdBy) {
		this.createdBy = createdBy;
	}

	public ObjectId getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(ObjectId updatedBy) {
		this.updatedBy = updatedBy;
	}

	public ObjectId getApprovedBy() {
		return approvedBy;
	}

	public void setApprovedBy(ObjectId approvedBy) {
		this.approvedBy = approvedBy;
	}

	public Instant getApprovedAt() {
		return approvedAt;
	}

	public void setApprovedAt(Instant approvedAt) {
		this.approvedAt = approvedAt;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}

	public List<LoanAction> getActions() {
		return actions;
	}

	public void setActions(List<LoanAction> actions) {
		this.actions = actions;
	}

	public void addAction(LoanAction action) {
		if (this.actions == null) {
			this.actions = new ArrayList<>();
		}
		this.actions.add(action);
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public Instant getDeletedAt() {
		return deletedAt;
	}

	public void setDeletedAt(Instant deletedAt) {
		this.deletedAt = deletedAt;
	}

	// Nested class for financials
	public static class Financials {
		private Double revenue;
		private Double ebitda;
		private String rating;

		public Financials() {
		}

		public Financials(Double revenue, Double ebitda, String rating) {
			this.revenue = revenue;
			this.ebitda = ebitda;
			this.rating = rating;
		}

		public Double getRevenue() {
			return revenue;
		}

		public void setRevenue(Double revenue) {
			this.revenue = revenue;
		}

		public Double getEbitda() {
			return ebitda;
		}

		public void setEbitda(Double ebitda) {
			this.ebitda = ebitda;
		}

		public String getRating() {
			return rating;
		}

		public void setRating(String rating) {
			this.rating = rating;
		}
	}

	// Nested class for audit trail actions
	public static class LoanAction {
		private ObjectId by;
		private String action;
		private String comments;
		private Instant timestamp;

		public LoanAction() {
		}

		public LoanAction(ObjectId by, String action, String comments, Instant timestamp) {
			this.by = by;
			this.action = action;
			this.comments = comments;
			this.timestamp = timestamp;
		}

		public ObjectId getBy() {
			return by;
		}

		public void setBy(ObjectId by) {
			this.by = by;
		}

		public String getAction() {
			return action;
		}

		public void setAction(String action) {
			this.action = action;
		}

		public String getComments() {
			return comments;
		}

		public void setComments(String comments) {
			this.comments = comments;
		}

		public Instant getTimestamp() {
			return timestamp;
		}

		public void setTimestamp(Instant timestamp) {
			this.timestamp = timestamp;
		}
	}
}

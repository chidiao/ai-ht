package com.example.backend.contract;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "contract_payment_plans")
public class ContractPaymentPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long contractId;
    private String paymentStage;

    @Column(precision = 8, scale = 2)
    private BigDecimal plannedRatio;

    @DecimalMin("0.00")
    @Column(precision = 14, scale = 2)
    private BigDecimal plannedAmount;

    @DecimalMin("0.00")
    @Column(precision = 14, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    private LocalDate plannedDate;
    private Boolean paid = false;

    @Column(length = 1000)
    private String paymentCondition;

    private String creator;
    private LocalDateTime createdAt;

    public ContractPaymentPlan() {
    }

    public ContractPaymentPlan(Long contractId, String paymentStage, BigDecimal plannedRatio, BigDecimal plannedAmount,
                               LocalDate plannedDate, String paymentCondition, String creator) {
        this.contractId = contractId;
        this.paymentStage = paymentStage;
        this.plannedRatio = plannedRatio;
        this.plannedAmount = plannedAmount;
        this.plannedDate = plannedDate;
        this.paymentCondition = paymentCondition;
        this.creator = creator;
        this.createdAt = LocalDateTime.now();
        this.paid = false;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getContractId() { return contractId; }
    public void setContractId(Long contractId) { this.contractId = contractId; }
    public String getPaymentStage() { return paymentStage; }
    public void setPaymentStage(String paymentStage) { this.paymentStage = paymentStage; }
    public BigDecimal getPlannedRatio() { return plannedRatio; }
    public void setPlannedRatio(BigDecimal plannedRatio) { this.plannedRatio = plannedRatio; }
    public BigDecimal getPlannedAmount() { return plannedAmount; }
    public void setPlannedAmount(BigDecimal plannedAmount) { this.plannedAmount = plannedAmount; }
    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }
    public LocalDate getPlannedDate() { return plannedDate; }
    public void setPlannedDate(LocalDate plannedDate) { this.plannedDate = plannedDate; }
    public Boolean getPaid() { return paid; }
    public void setPaid(Boolean paid) { this.paid = paid; }
    public String getPaymentCondition() { return paymentCondition; }
    public void setPaymentCondition(String paymentCondition) { this.paymentCondition = paymentCondition; }
    public String getCreator() { return creator; }
    public void setCreator(String creator) { this.creator = creator; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

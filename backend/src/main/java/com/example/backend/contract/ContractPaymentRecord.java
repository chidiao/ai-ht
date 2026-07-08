package com.example.backend.contract;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "contract_payment_records")
public class ContractPaymentRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long contractId;
    private Long paymentPlanId;
    private String paymentStage;

    @NotNull
    @DecimalMin("0.00")
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @NotNull
    @DecimalMin("0.00")
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal paidAmountAfterPayment;

    private LocalDate paymentDate;
    private String invoiceNo;

    @Column(length = 1000)
    private String note;

    private String operator;
    private LocalDateTime createdAt;

    public ContractPaymentRecord() {
    }

    public ContractPaymentRecord(Long contractId, String paymentStage, BigDecimal amount, BigDecimal paidAmountAfterPayment,
                                 LocalDate paymentDate, String invoiceNo, String note, String operator) {
        this.contractId = contractId;
        this.paymentStage = paymentStage;
        this.amount = amount;
        this.paidAmountAfterPayment = paidAmountAfterPayment;
        this.paymentDate = paymentDate;
        this.invoiceNo = invoiceNo;
        this.note = note;
        this.operator = operator;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getContractId() { return contractId; }
    public void setContractId(Long contractId) { this.contractId = contractId; }
    public Long getPaymentPlanId() { return paymentPlanId; }
    public void setPaymentPlanId(Long paymentPlanId) { this.paymentPlanId = paymentPlanId; }
    public String getPaymentStage() { return paymentStage; }
    public void setPaymentStage(String paymentStage) { this.paymentStage = paymentStage; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public BigDecimal getPaidAmountAfterPayment() { return paidAmountAfterPayment; }
    public void setPaidAmountAfterPayment(BigDecimal paidAmountAfterPayment) { this.paidAmountAfterPayment = paidAmountAfterPayment; }
    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }
    public String getInvoiceNo() { return invoiceNo; }
    public void setInvoiceNo(String invoiceNo) { this.invoiceNo = invoiceNo; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

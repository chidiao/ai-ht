package com.example.backend.contract;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "contract_acceptance_records")
public class ContractAcceptanceRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long contractId;
    private LocalDate deliveryDate;
    private LocalDate acceptanceDate;
    private String acceptanceResult;

    @Column(length = 1000)
    private String acceptanceNote;

    @Column(length = 1000)
    private String exceptionNote;

    private String accepter;
    private LocalDateTime createdAt;

    public ContractAcceptanceRecord() {
    }

    public ContractAcceptanceRecord(Long contractId, LocalDate deliveryDate, LocalDate acceptanceDate,
                                    String acceptanceResult, String acceptanceNote, String exceptionNote,
                                    String accepter) {
        this.contractId = contractId;
        this.deliveryDate = deliveryDate;
        this.acceptanceDate = acceptanceDate;
        this.acceptanceResult = acceptanceResult;
        this.acceptanceNote = acceptanceNote;
        this.exceptionNote = exceptionNote;
        this.accepter = accepter;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getContractId() { return contractId; }
    public void setContractId(Long contractId) { this.contractId = contractId; }
    public LocalDate getDeliveryDate() { return deliveryDate; }
    public void setDeliveryDate(LocalDate deliveryDate) { this.deliveryDate = deliveryDate; }
    public LocalDate getAcceptanceDate() { return acceptanceDate; }
    public void setAcceptanceDate(LocalDate acceptanceDate) { this.acceptanceDate = acceptanceDate; }
    public String getAcceptanceResult() { return acceptanceResult; }
    public void setAcceptanceResult(String acceptanceResult) { this.acceptanceResult = acceptanceResult; }
    public String getAcceptanceNote() { return acceptanceNote; }
    public void setAcceptanceNote(String acceptanceNote) { this.acceptanceNote = acceptanceNote; }
    public String getExceptionNote() { return exceptionNote; }
    public void setExceptionNote(String exceptionNote) { this.exceptionNote = exceptionNote; }
    public String getAccepter() { return accepter; }
    public void setAccepter(String accepter) { this.accepter = accepter; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

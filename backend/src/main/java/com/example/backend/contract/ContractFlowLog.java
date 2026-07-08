package com.example.backend.contract;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "contract_flow_logs")
public class ContractFlowLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long contractId;

    @Enumerated(EnumType.STRING)
    private ContractAction action;

    private String operator;

    @Enumerated(EnumType.STRING)
    private ContractStatus fromStatus;

    @Enumerated(EnumType.STRING)
    private ContractStatus toStatus;

    private String comment;
    private LocalDateTime operatedAt;

    public ContractFlowLog() {
    }

    public ContractFlowLog(Long contractId, ContractAction action, String operator, ContractStatus fromStatus, ContractStatus toStatus, String comment) {
        this.contractId = contractId;
        this.action = action;
        this.operator = operator;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.comment = comment;
        this.operatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getContractId() { return contractId; }
    public void setContractId(Long contractId) { this.contractId = contractId; }
    public ContractAction getAction() { return action; }
    public void setAction(ContractAction action) { this.action = action; }
    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }
    public ContractStatus getFromStatus() { return fromStatus; }
    public void setFromStatus(ContractStatus fromStatus) { this.fromStatus = fromStatus; }
    public ContractStatus getToStatus() { return toStatus; }
    public void setToStatus(ContractStatus toStatus) { this.toStatus = toStatus; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public LocalDateTime getOperatedAt() { return operatedAt; }
    public void setOperatedAt(LocalDateTime operatedAt) { this.operatedAt = operatedAt; }
}

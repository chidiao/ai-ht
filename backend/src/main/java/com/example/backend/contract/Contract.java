package com.example.backend.contract;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "contracts")
public class Contract {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String contractNo;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @NotBlank
    @Column(nullable = false)
    private String supplierName;

    @NotBlank
    @Column(nullable = false)
    private String owner;

    private String department;
    private String category;
    private String purchaseContent;
    private String purchaseQuantity;
    private String purchaseMethod;
    private String supplierContact;
    private String supplierPhone;
    private String deliveryLocation;
    private String paymentMethod;
    private String acceptanceCriteria;
    private String signingMethod;
    private String contractSource;

    @NotNull
    @DecimalMin("0.00")
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @NotNull
    @DecimalMin("0.00")
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    private LocalDate signDate;
    private LocalDate effectiveDate;

    @NotNull
    @Column(nullable = false)
    private LocalDate expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContractStatus status = ContractStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApprovalStatus approvalStatus = ApprovalStatus.NOT_SUBMITTED;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SigningStatus signingStatus = SigningStatus.UNSIGNED;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ArchiveStatus archiveStatus = ArchiveStatus.UNARCHIVED;

    @Column(length = 1000)
    private String remark;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) {
            this.status = ContractStatus.DRAFT;
        }
        if (this.paymentStatus == null) {
            this.paymentStatus = PaymentStatus.UNPAID;
        }
        if (this.approvalStatus == null) {
            this.approvalStatus = ApprovalStatus.NOT_SUBMITTED;
        }
        if (this.signingStatus == null) {
            this.signingStatus = SigningStatus.UNSIGNED;
        }
        if (this.archiveStatus == null) {
            this.archiveStatus = ArchiveStatus.UNARCHIVED;
        }
        if (this.paidAmount == null) {
            this.paidAmount = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getContractNo() { return contractNo; }
    public void setContractNo(String contractNo) { this.contractNo = contractNo; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getPurchaseContent() { return purchaseContent; }
    public void setPurchaseContent(String purchaseContent) { this.purchaseContent = purchaseContent; }
    public String getPurchaseQuantity() { return purchaseQuantity; }
    public void setPurchaseQuantity(String purchaseQuantity) { this.purchaseQuantity = purchaseQuantity; }
    public String getPurchaseMethod() { return purchaseMethod; }
    public void setPurchaseMethod(String purchaseMethod) { this.purchaseMethod = purchaseMethod; }
    public String getSupplierContact() { return supplierContact; }
    public void setSupplierContact(String supplierContact) { this.supplierContact = supplierContact; }
    public String getSupplierPhone() { return supplierPhone; }
    public void setSupplierPhone(String supplierPhone) { this.supplierPhone = supplierPhone; }
    public String getDeliveryLocation() { return deliveryLocation; }
    public void setDeliveryLocation(String deliveryLocation) { this.deliveryLocation = deliveryLocation; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getAcceptanceCriteria() { return acceptanceCriteria; }
    public void setAcceptanceCriteria(String acceptanceCriteria) { this.acceptanceCriteria = acceptanceCriteria; }
    public String getSigningMethod() { return signingMethod; }
    public void setSigningMethod(String signingMethod) { this.signingMethod = signingMethod; }
    public String getContractSource() { return contractSource; }
    public void setContractSource(String contractSource) { this.contractSource = contractSource; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }
    public LocalDate getSignDate() { return signDate; }
    public void setSignDate(LocalDate signDate) { this.signDate = signDate; }
    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public ContractStatus getStatus() { return status; }
    public void setStatus(ContractStatus status) { this.status = status; }
    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }
    public ApprovalStatus getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(ApprovalStatus approvalStatus) { this.approvalStatus = approvalStatus; }
    public SigningStatus getSigningStatus() { return signingStatus; }
    public void setSigningStatus(SigningStatus signingStatus) { this.signingStatus = signingStatus; }
    public ArchiveStatus getArchiveStatus() { return archiveStatus; }
    public void setArchiveStatus(ArchiveStatus archiveStatus) { this.archiveStatus = archiveStatus; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

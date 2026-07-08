package com.example.backend.contract.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ContractRequest(
        @NotBlank String contractNo,
        @NotBlank String name,
        @NotBlank String supplierName,
        @NotBlank String owner,
        String department,
        String category,
        String purchaseContent,
        String purchaseQuantity,
        String purchaseMethod,
        String supplierContact,
        String supplierPhone,
        String deliveryLocation,
        String paymentMethod,
        String acceptanceCriteria,
        String signingMethod,
        String contractSource,
        @NotNull @DecimalMin("0.00") BigDecimal amount,
        LocalDate signDate,
        LocalDate effectiveDate,
        @NotNull LocalDate expiryDate,
        String remark
) {
}

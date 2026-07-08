package com.example.backend.contract.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ContractPaymentPlanRequest(
        @NotBlank String paymentStage,
        @DecimalMin("0.00") @DecimalMax("100.00") BigDecimal plannedRatio,
        @NotNull @DecimalMin("0.00") BigDecimal plannedAmount,
        LocalDate plannedDate,
        String paymentCondition,
        @NotBlank String creator
) {
}

package com.example.backend.contract.dto;

import com.example.backend.contract.ContractAction;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ContractActionRequest(
        @NotNull ContractAction action,
        @NotBlank String operator,
        String comment,
        BigDecimal paidAmount,
        Long paymentPlanId,
        String paymentStage,
        LocalDate paymentDate,
        String invoiceNo,
        LocalDate deliveryDate,
        LocalDate acceptanceDate,
        String acceptanceResult,
        String exceptionNote
) {
}

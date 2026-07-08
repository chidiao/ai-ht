package com.example.backend.contract.dto;

import com.example.backend.contract.ContractAction;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ContractActionRequest(
        @NotNull ContractAction action,
        @NotBlank String operator,
        String comment,
        BigDecimal paidAmount
) {
}

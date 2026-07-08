package com.example.backend.contract.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record ContractAcceptanceRequest(
        LocalDate deliveryDate,
        LocalDate acceptanceDate,
        @NotBlank String acceptanceResult,
        String acceptanceNote,
        String exceptionNote,
        @NotBlank String accepter
) {
}

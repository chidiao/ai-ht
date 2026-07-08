package com.example.backend.contract.dto;

import com.example.backend.contract.Contract;

import java.util.List;

public record ContractPageResponse(
        List<Contract> content,
        long totalElements,
        int totalPages,
        int page,
        int size
) {
}

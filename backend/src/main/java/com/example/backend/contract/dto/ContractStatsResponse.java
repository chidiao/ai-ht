package com.example.backend.contract.dto;

public record ContractStatsResponse(
        long total,
        long pendingApproval,
        long executing,
        long pendingPayment,
        long expiringSoon
) {
}

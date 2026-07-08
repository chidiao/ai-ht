package com.example.backend.contract.dto;

import com.example.backend.contract.Contract;
import com.example.backend.contract.ContractFlowLog;

import java.util.List;

public record ContractDetailResponse(
        Contract contract,
        List<ContractFlowLog> logs
) {
}

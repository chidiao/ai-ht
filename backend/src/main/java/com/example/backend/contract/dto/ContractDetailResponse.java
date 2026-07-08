package com.example.backend.contract.dto;

import com.example.backend.contract.Contract;
import com.example.backend.contract.ContractAcceptanceRecord;
import com.example.backend.contract.ContractAttachment;
import com.example.backend.contract.ContractFlowLog;
import com.example.backend.contract.ContractPaymentPlan;
import com.example.backend.contract.ContractPaymentRecord;

import java.util.List;

public record ContractDetailResponse(
        Contract contract,
        List<ContractPaymentPlan> paymentPlans,
        List<ContractPaymentRecord> paymentRecords,
        List<ContractAcceptanceRecord> acceptanceRecords,
        List<ContractAttachment> attachments,
        List<ContractFlowLog> logs
) {
}

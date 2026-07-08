package com.example.backend.contract;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContractPaymentPlanRepository extends JpaRepository<ContractPaymentPlan, Long> {
    List<ContractPaymentPlan> findByContractIdOrderByPlannedDateAscCreatedAtAsc(Long contractId);
}

package com.example.backend.contract;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContractFlowLogRepository extends JpaRepository<ContractFlowLog, Long> {
    List<ContractFlowLog> findByContractIdOrderByOperatedAtAsc(Long contractId);
}

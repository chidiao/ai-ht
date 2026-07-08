package com.example.backend.contract;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContractAcceptanceRecordRepository extends JpaRepository<ContractAcceptanceRecord, Long> {
    List<ContractAcceptanceRecord> findByContractIdOrderByAcceptanceDateAscCreatedAtAsc(Long contractId);
}

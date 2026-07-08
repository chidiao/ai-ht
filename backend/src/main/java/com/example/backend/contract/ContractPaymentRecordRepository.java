package com.example.backend.contract;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContractPaymentRecordRepository extends JpaRepository<ContractPaymentRecord, Long> {
    List<ContractPaymentRecord> findByContractIdOrderByPaymentDateAscCreatedAtAsc(Long contractId);
}

package com.example.backend.contract;

import com.example.backend.contract.dto.ContractActionRequest;
import com.example.backend.contract.dto.ContractAcceptanceRequest;
import com.example.backend.contract.dto.ContractAttachmentRequest;
import com.example.backend.contract.dto.ContractDetailResponse;
import com.example.backend.contract.dto.ContractPageResponse;
import com.example.backend.contract.dto.ContractPaymentPlanRequest;
import com.example.backend.contract.dto.ContractRequest;
import com.example.backend.contract.dto.ContractStatsResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/contracts")
public class ContractController {
    private final ContractService contractService;

    public ContractController(ContractService contractService) {
        this.contractService = contractService;
    }

    @GetMapping
    public ContractPageResponse search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String supplierName,
            @RequestParam(required = false) String owner,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) ContractStatus status,
            @RequestParam(required = false) List<ContractStatus> statuses,
            @RequestParam(required = false) PaymentStatus paymentStatus,
            @RequestParam(required = false) ArchiveStatus archiveStatus,
            @RequestParam(required = false) java.math.BigDecimal amountMin,
            @RequestParam(required = false) java.math.BigDecimal amountMax,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate signStart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate signEnd,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueStart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueEnd,
            @RequestParam(required = false) Boolean expiringSoon,
            @RequestParam(required = false) String quickFilter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return contractService.search(keyword, supplierName, owner, department, category, status, statuses, paymentStatus,
                archiveStatus, amountMin, amountMax, signStart, signEnd, dueStart, dueEnd, expiringSoon, quickFilter, page, size);
    }

    @GetMapping("/{id}")
    public ContractDetailResponse detail(@PathVariable Long id) {
        return contractService.detail(id);
    }

    @PostMapping
    public Contract create(@Valid @RequestBody ContractRequest request) {
        return contractService.create(request);
    }

    @PutMapping("/{id}")
    public Contract update(@PathVariable Long id, @Valid @RequestBody ContractRequest request) {
        return contractService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        contractService.delete(id);
    }

    @PostMapping("/{id}/payment-plans")
    public ContractPaymentPlan addPaymentPlan(@PathVariable Long id, @Valid @RequestBody ContractPaymentPlanRequest request) {
        return contractService.addPaymentPlan(id, request);
    }

    @PostMapping("/{id}/attachments")
    public ContractAttachment addAttachment(@PathVariable Long id, @Valid @RequestBody ContractAttachmentRequest request) {
        return contractService.addAttachment(id, request);
    }

    @PostMapping("/{id}/acceptances")
    public ContractAcceptanceRecord addAcceptance(@PathVariable Long id, @Valid @RequestBody ContractAcceptanceRequest request) {
        return contractService.addAcceptance(id, request);
    }

    @PostMapping("/{id}/action")
    public Contract action(@PathVariable Long id, @Valid @RequestBody ContractActionRequest request) {
        return contractService.action(id, request);
    }

    @GetMapping("/stats")
    public ContractStatsResponse stats() {
        return contractService.stats();
    }
}

package com.example.backend.contract;

import com.example.backend.contract.dto.ContractActionRequest;
import com.example.backend.contract.dto.ContractDetailResponse;
import com.example.backend.contract.dto.ContractRequest;
import com.example.backend.contract.dto.ContractStatsResponse;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class ContractService {
    private final ContractRepository contractRepository;
    private final ContractFlowLogRepository logRepository;
    private final Map<ContractAction, ContractStatus> requiredStatus = new EnumMap<>(ContractAction.class);

    public ContractService(ContractRepository contractRepository, ContractFlowLogRepository logRepository) {
        this.contractRepository = contractRepository;
        this.logRepository = logRepository;
        requiredStatus.put(ContractAction.SUBMIT_SUPPLIER_CONFIRM, ContractStatus.DRAFT);
        requiredStatus.put(ContractAction.SUPPLIER_CONFIRMED, ContractStatus.SUPPLIER_CONFIRMING);
        requiredStatus.put(ContractAction.SUBMIT_APPROVAL, ContractStatus.SUPPLIER_CONFIRMING);
        requiredStatus.put(ContractAction.APPROVE, ContractStatus.PENDING_APPROVAL);
        requiredStatus.put(ContractAction.REJECT, ContractStatus.PENDING_APPROVAL);
        requiredStatus.put(ContractAction.START_EXECUTION, ContractStatus.ACTIVE);
        requiredStatus.put(ContractAction.COMPLETE, ContractStatus.PAYING);
        requiredStatus.put(ContractAction.ARCHIVE, ContractStatus.COMPLETED);
    }

    @Transactional(readOnly = true)
    public List<Contract> search(String keyword, String supplierName, String owner, ContractStatus status,
                                 List<ContractStatus> statuses, PaymentStatus paymentStatus, LocalDate dueStart, LocalDate dueEnd,
                                 Boolean expiringSoon, String quickFilter) {
        Specification<Contract> spec = (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (hasText(keyword)) {
                String like = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(builder.or(
                        builder.like(builder.lower(root.get("contractNo")), like),
                        builder.like(builder.lower(root.get("name")), like)
                ));
            }
            if (hasText(supplierName)) {
                predicates.add(builder.like(builder.lower(root.get("supplierName")), "%" + supplierName.trim().toLowerCase() + "%"));
            }
            if (hasText(owner)) {
                predicates.add(builder.like(builder.lower(root.get("owner")), "%" + owner.trim().toLowerCase() + "%"));
            }
            if (status != null) {
                predicates.add(builder.equal(root.get("status"), status));
            }
            if (statuses != null && !statuses.isEmpty()) {
                predicates.add(root.get("status").in(statuses));
            }
            if (paymentStatus != null) {
                predicates.add(builder.equal(root.get("paymentStatus"), paymentStatus));
            }
            if (dueStart != null) {
                predicates.add(builder.greaterThanOrEqualTo(root.get("expiryDate"), dueStart));
            }
            if (dueEnd != null) {
                predicates.add(builder.lessThanOrEqualTo(root.get("expiryDate"), dueEnd));
            }
            if (Boolean.TRUE.equals(expiringSoon)) {
                LocalDate today = LocalDate.now();
                predicates.add(builder.between(root.get("expiryDate"), today, today.plusDays(30)));
            }
            if (hasText(quickFilter)) {
                LocalDate today = LocalDate.now();
                String filterKey = quickFilter.trim();
                switch (filterKey) {
                    case "pendingApproval" -> predicates.add(builder.equal(root.get("status"), ContractStatus.PENDING_APPROVAL));
                    case "executing" -> predicates.add(root.get("status").in(ContractStatus.EXECUTING, ContractStatus.PAYING));
                    case "pendingPayment" -> predicates.add(builder.and(
                            builder.notEqual(root.get("paymentStatus"), PaymentStatus.PAID),
                            builder.notEqual(root.get("status"), ContractStatus.ARCHIVED)
                    ));
                    case "expiringSoon" -> predicates.add(builder.between(root.get("expiryDate"), today, today.plusDays(30)));
                    default -> {
                    }
                }
            }
            return builder.and(predicates.toArray(new Predicate[0]));
        };
        return contractRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "updatedAt"));
    }

    @Transactional(readOnly = true)
    public ContractDetailResponse detail(Long id) {
        Contract contract = findContract(id);
        return new ContractDetailResponse(contract, logRepository.findByContractIdOrderByOperatedAtAsc(id));
    }

    @Transactional
    public Contract create(ContractRequest request) {
        contractRepository.findByContractNo(request.contractNo()).ifPresent(existing -> {
            throw new IllegalArgumentException("Contract number already exists");
        });
        Contract contract = new Contract();
        applyRequest(contract, request);
        contract.setStatus(ContractStatus.DRAFT);
        contract.setPaymentStatus(PaymentStatus.UNPAID);
        contract.setPaidAmount(BigDecimal.ZERO);
        Contract saved = contractRepository.save(contract);
        logRepository.save(new ContractFlowLog(saved.getId(), ContractAction.SUBMIT_SUPPLIER_CONFIRM, "System", null, ContractStatus.DRAFT, "Contract draft created"));
        return saved;
    }

    @Transactional
    public Contract update(Long id, ContractRequest request) {
        Contract contract = findContract(id);
        if (contract.getStatus() != ContractStatus.DRAFT && contract.getStatus() != ContractStatus.REJECTED) {
            throw new IllegalStateException("Only draft or rejected contracts can be edited");
        }
        contractRepository.findByContractNo(request.contractNo())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Contract number already exists");
                });
        applyRequest(contract, request);
        return contractRepository.save(contract);
    }

    @Transactional
    public Contract action(Long id, ContractActionRequest request) {
        Contract contract = findContract(id);
        ContractStatus before = contract.getStatus();
        ContractStatus after = nextStatus(contract, request);
        contract.setStatus(after);
        if (request.action() == ContractAction.APPROVE && contract.getEffectiveDate() == null) {
            contract.setEffectiveDate(LocalDate.now());
        }
        if (request.action() == ContractAction.REGISTER_PAYMENT) {
            updatePayment(contract, request.paidAmount());
        }
        Contract saved = contractRepository.save(contract);
        logRepository.save(new ContractFlowLog(saved.getId(), request.action(), request.operator(), before, after, request.comment()));
        return saved;
    }

    @Transactional(readOnly = true)
    public ContractStatsResponse stats() {
        List<Contract> contracts = contractRepository.findAll();
        LocalDate today = LocalDate.now();
        return new ContractStatsResponse(
                contracts.size(),
                contracts.stream().filter(item -> item.getStatus() == ContractStatus.PENDING_APPROVAL).count(),
                contracts.stream().filter(item -> item.getStatus() == ContractStatus.EXECUTING || item.getStatus() == ContractStatus.PAYING).count(),
                contracts.stream().filter(item -> item.getPaymentStatus() != PaymentStatus.PAID && item.getStatus() != ContractStatus.ARCHIVED).count(),
                contracts.stream().filter(item -> !item.getExpiryDate().isBefore(today) && !item.getExpiryDate().isAfter(today.plusDays(30))).count()
        );
    }

    private ContractStatus nextStatus(Contract contract, ContractActionRequest request) {
        if (request.action() == ContractAction.TERMINATE) {
            if (contract.getStatus() == ContractStatus.ARCHIVED || contract.getStatus() == ContractStatus.COMPLETED) {
                throw new IllegalStateException("Completed or archived contracts cannot be terminated");
            }
            return ContractStatus.TERMINATED;
        }
        if (request.action() == ContractAction.REGISTER_PAYMENT) {
            if (contract.getStatus() != ContractStatus.EXECUTING && contract.getStatus() != ContractStatus.PAYING) {
                throw new IllegalStateException("Current status does not allow this action");
            }
            return ContractStatus.PAYING;
        }
        ContractStatus required = requiredStatus.get(request.action());
        if (required == null) {
            throw new IllegalArgumentException("Unsupported workflow action");
        }
        if (contract.getStatus() != required) {
            throw new IllegalStateException("Current status does not allow this action");
        }
        if (request.action() == ContractAction.COMPLETE && contract.getPaymentStatus() != PaymentStatus.PAID) {
            throw new IllegalStateException("Contract can be completed only after full payment");
        }
        return switch (request.action()) {
            case SUBMIT_SUPPLIER_CONFIRM -> ContractStatus.SUPPLIER_CONFIRMING;
            case SUPPLIER_CONFIRMED -> ContractStatus.SUPPLIER_CONFIRMING;
            case SUBMIT_APPROVAL -> ContractStatus.PENDING_APPROVAL;
            case APPROVE -> ContractStatus.ACTIVE;
            case REJECT -> ContractStatus.REJECTED;
            case START_EXECUTION -> ContractStatus.EXECUTING;
            case REGISTER_PAYMENT -> ContractStatus.PAYING;
            case COMPLETE -> ContractStatus.COMPLETED;
            case ARCHIVE -> ContractStatus.ARCHIVED;
            case TERMINATE -> ContractStatus.TERMINATED;
        };
    }

    private void updatePayment(Contract contract, BigDecimal paidAmount) {
        if (paidAmount == null || paidAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Paid amount is required and cannot be less than 0");
        }
        if (paidAmount.compareTo(contract.getAmount()) > 0) {
            throw new IllegalArgumentException("Paid amount cannot be greater than contract amount");
        }
        contract.setPaidAmount(paidAmount);
        if (paidAmount.compareTo(BigDecimal.ZERO) == 0) {
            contract.setPaymentStatus(PaymentStatus.UNPAID);
        } else if (paidAmount.compareTo(contract.getAmount()) < 0) {
            contract.setPaymentStatus(PaymentStatus.PARTIAL);
        } else {
            contract.setPaymentStatus(PaymentStatus.PAID);
        }
    }

    private Contract findContract(Long id) {
        return contractRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Contract does not exist"));
    }

    private void applyRequest(Contract contract, ContractRequest request) {
        contract.setContractNo(request.contractNo());
        contract.setName(request.name());
        contract.setSupplierName(request.supplierName());
        contract.setOwner(request.owner());
        contract.setDepartment(request.department());
        contract.setCategory(request.category());
        contract.setAmount(request.amount());
        contract.setSignDate(request.signDate());
        contract.setEffectiveDate(request.effectiveDate());
        contract.setExpiryDate(request.expiryDate());
        contract.setRemark(request.remark());
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}

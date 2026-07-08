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
    private final ContractPaymentRecordRepository paymentRecordRepository;
    private final ContractAcceptanceRecordRepository acceptanceRecordRepository;
    private final Map<ContractAction, ContractStatus> requiredStatus = new EnumMap<>(ContractAction.class);

    public ContractService(ContractRepository contractRepository, ContractFlowLogRepository logRepository,
                           ContractPaymentRecordRepository paymentRecordRepository,
                           ContractAcceptanceRecordRepository acceptanceRecordRepository) {
        this.contractRepository = contractRepository;
        this.logRepository = logRepository;
        this.paymentRecordRepository = paymentRecordRepository;
        this.acceptanceRecordRepository = acceptanceRecordRepository;
        requiredStatus.put(ContractAction.SUBMIT_SUPPLIER_CONFIRM, ContractStatus.DRAFT);
        requiredStatus.put(ContractAction.SUPPLIER_CONFIRMED, ContractStatus.SUPPLIER_CONFIRMING);
        requiredStatus.put(ContractAction.SUBMIT_APPROVAL, ContractStatus.SUPPLIER_CONFIRMING);
        requiredStatus.put(ContractAction.APPROVE, ContractStatus.PENDING_APPROVAL);
        requiredStatus.put(ContractAction.REJECT, ContractStatus.PENDING_APPROVAL);
        requiredStatus.put(ContractAction.START_EXECUTION, ContractStatus.ACTIVE);
        requiredStatus.put(ContractAction.COMPLETE, ContractStatus.EXECUTING);
        requiredStatus.put(ContractAction.ARCHIVE, ContractStatus.COMPLETED);
    }

    @Transactional(readOnly = true)
    public List<Contract> search(String keyword, String supplierName, String owner, String department, String category,
                                 ContractStatus status, List<ContractStatus> statuses, PaymentStatus paymentStatus,
                                 ArchiveStatus archiveStatus, BigDecimal amountMin, BigDecimal amountMax,
                                 LocalDate signStart, LocalDate signEnd, LocalDate dueStart, LocalDate dueEnd,
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
            if (hasText(department)) {
                predicates.add(builder.like(builder.lower(root.get("department")), "%" + department.trim().toLowerCase() + "%"));
            }
            if (hasText(category)) {
                predicates.add(builder.like(builder.lower(root.get("category")), "%" + category.trim().toLowerCase() + "%"));
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
            if (archiveStatus != null) {
                predicates.add(builder.equal(root.get("archiveStatus"), archiveStatus));
            }
            if (amountMin != null) {
                predicates.add(builder.greaterThanOrEqualTo(root.get("amount"), amountMin));
            }
            if (amountMax != null) {
                predicates.add(builder.lessThanOrEqualTo(root.get("amount"), amountMax));
            }
            if (signStart != null) {
                predicates.add(builder.greaterThanOrEqualTo(root.get("signDate"), signStart));
            }
            if (signEnd != null) {
                predicates.add(builder.lessThanOrEqualTo(root.get("signDate"), signEnd));
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
                    case "executing" -> predicates.add(builder.equal(root.get("status"), ContractStatus.EXECUTING));
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
        return new ContractDetailResponse(
                contract,
                paymentRecordRepository.findByContractIdOrderByPaymentDateAscCreatedAtAsc(id),
                acceptanceRecordRepository.findByContractIdOrderByAcceptanceDateAscCreatedAtAsc(id),
                logRepository.findByContractIdOrderByOperatedAtAsc(id)
        );
    }

    @Transactional
    public Contract create(ContractRequest request) {
        contractRepository.findByContractNo(request.contractNo()).ifPresent(existing -> {
            throw new IllegalArgumentException("合同编号已存在");
        });
        Contract contract = new Contract();
        applyRequest(contract, request);
        contract.setStatus(ContractStatus.DRAFT);
        contract.setPaymentStatus(PaymentStatus.UNPAID);
        contract.setApprovalStatus(ApprovalStatus.NOT_SUBMITTED);
        contract.setSigningStatus(SigningStatus.UNSIGNED);
        contract.setArchiveStatus(ArchiveStatus.UNARCHIVED);
        contract.setPaidAmount(BigDecimal.ZERO);
        Contract saved = contractRepository.save(contract);
        logRepository.save(new ContractFlowLog(saved.getId(), ContractAction.SUBMIT_SUPPLIER_CONFIRM, "System", null, ContractStatus.DRAFT, "Contract draft created"));
        return saved;
    }

    @Transactional
    public Contract update(Long id, ContractRequest request) {
        Contract contract = findContract(id);
        if (contract.getStatus() != ContractStatus.DRAFT && contract.getStatus() != ContractStatus.REJECTED) {
            throw new IllegalStateException("仅草稿或驳回状态允许编辑");
        }
        contractRepository.findByContractNo(request.contractNo())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("合同编号已存在");
                });
        applyRequest(contract, request);
        return contractRepository.save(contract);
    }

    @Transactional
    public Contract action(Long id, ContractActionRequest request) {
        Contract contract = findContract(id);
        ContractStatus before = contract.getStatus();
        ContractStatus after = nextStatus(contract, request);
        BigDecimal paymentRecordAmount = null;
        contract.setStatus(after);
        if (request.action() == ContractAction.APPROVE && contract.getEffectiveDate() == null) {
            contract.setEffectiveDate(LocalDate.now());
        }
        if (request.action() == ContractAction.REGISTER_PAYMENT) {
            if (request.paidAmount() == null) {
                throw new IllegalArgumentException("本次付款金额不能为空");
            }
            paymentRecordAmount = request.paidAmount();
            updatePayment(contract, paymentRecordAmount);
        }
        applySubStatus(contract, request.action());
        Contract saved = contractRepository.save(contract);
        if (request.action() == ContractAction.REGISTER_PAYMENT) {
            savePaymentRecord(saved, request, paymentRecordAmount);
        }
        if (request.action() == ContractAction.COMPLETE) {
            saveAcceptanceRecord(saved, request);
        }
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
                contracts.stream().filter(item -> item.getStatus() == ContractStatus.EXECUTING).count(),
                contracts.stream().filter(item -> item.getPaymentStatus() != PaymentStatus.PAID && item.getStatus() != ContractStatus.ARCHIVED).count(),
                contracts.stream().filter(item -> !item.getExpiryDate().isBefore(today) && !item.getExpiryDate().isAfter(today.plusDays(30))).count()
        );
    }

    private ContractStatus nextStatus(Contract contract, ContractActionRequest request) {
        validateActionAllowed(contract, request.action());
        if (request.action() == ContractAction.TERMINATE) {
            if (contract.getStatus() == ContractStatus.ARCHIVED || contract.getStatus() == ContractStatus.COMPLETED) {
                throw new IllegalStateException("已完成或已归档的合同不能终止");
            }
            return ContractStatus.TERMINATED;
        }
        if (request.action() == ContractAction.REGISTER_PAYMENT) {
            if (contract.getStatus() != ContractStatus.ACTIVE && contract.getStatus() != ContractStatus.EXECUTING) {
                throw new IllegalStateException("当前状态不允许登记付款");
            }
            return contract.getStatus();
        }
        ContractStatus required = requiredStatus.get(request.action());
        if (required == null) {
            throw new IllegalArgumentException("不支持的流程动作");
        }
        if (contract.getStatus() != required) {
            throw new IllegalStateException("当前状态不允许执行该动作");
        }
        if (request.action() == ContractAction.COMPLETE && contract.getPaymentStatus() != PaymentStatus.PAID) {
            throw new IllegalStateException("合同未全额付款，不能完成合同");
        }
        return switch (request.action()) {
            case SUBMIT_SUPPLIER_CONFIRM -> ContractStatus.SUPPLIER_CONFIRMING;
            case SUPPLIER_CONFIRMED -> ContractStatus.SUPPLIER_CONFIRMING;
            case SUBMIT_APPROVAL -> ContractStatus.PENDING_APPROVAL;
            case APPROVE -> ContractStatus.ACTIVE;
            case REJECT -> ContractStatus.REJECTED;
            case START_EXECUTION -> ContractStatus.EXECUTING;
            case REGISTER_PAYMENT -> contract.getStatus();
            case COMPLETE -> ContractStatus.COMPLETED;
            case ARCHIVE -> ContractStatus.ARCHIVED;
            case TERMINATE -> ContractStatus.TERMINATED;
        };
    }

    private void validateActionAllowed(Contract contract, ContractAction action) {
        if (contract.getStatus() == ContractStatus.ARCHIVED) {
            throw new IllegalStateException("合同已归档，不能继续流程操作");
        }
        if (contract.getStatus() == ContractStatus.TERMINATED) {
            throw new IllegalStateException("合同已终止，不能继续流程操作");
        }
        if (contract.getStatus() == ContractStatus.COMPLETED && action != ContractAction.ARCHIVE) {
            throw new IllegalStateException("合同已完成，仅允许归档");
        }
        if (isExpired(contract) && isBeforeEffectiveCloseout(contract.getStatus(), action)) {
            throw new IllegalStateException("合同已过期，未生效流程不能继续推进，请修改到期日期或终止合同");
        }
    }

    private boolean isExpired(Contract contract) {
        return contract.getExpiryDate() != null && contract.getExpiryDate().isBefore(LocalDate.now());
    }

    private boolean isBeforeEffectiveCloseout(ContractStatus status, ContractAction action) {
        if (action == ContractAction.TERMINATE) {
            return false;
        }
        return status == ContractStatus.DRAFT
                || status == ContractStatus.SUPPLIER_CONFIRMING
                || status == ContractStatus.PENDING_APPROVAL
                || status == ContractStatus.REJECTED;
    }

    private void updatePayment(Contract contract, BigDecimal paymentAmount) {
        if (paymentAmount == null || paymentAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("本次付款金额必须大于 0");
        }
        BigDecimal paidAmount = contract.getPaidAmount() == null ? BigDecimal.ZERO : contract.getPaidAmount();
        BigDecimal paidAmountAfterPayment = paidAmount.add(paymentAmount);
        if (paidAmountAfterPayment.compareTo(contract.getAmount()) > 0) {
            throw new IllegalArgumentException("本次付款后累计已付金额不能大于合同金额");
        }
        contract.setPaidAmount(paidAmountAfterPayment);
        if (paidAmountAfterPayment.compareTo(BigDecimal.ZERO) == 0) {
            contract.setPaymentStatus(PaymentStatus.UNPAID);
        } else if (paidAmountAfterPayment.compareTo(contract.getAmount()) < 0) {
            contract.setPaymentStatus(PaymentStatus.PARTIAL);
        } else {
            contract.setPaymentStatus(PaymentStatus.PAID);
        }
    }

    private void applySubStatus(Contract contract, ContractAction action) {
        switch (action) {
            case SUBMIT_SUPPLIER_CONFIRM -> contract.setSigningStatus(SigningStatus.OUR_SIGNED);
            case SUBMIT_APPROVAL -> {
                contract.setSigningStatus(SigningStatus.BOTH_SIGNED);
                contract.setApprovalStatus(ApprovalStatus.PENDING);
            }
            case APPROVE -> approvalPassed(contract);
            case REJECT -> contract.setApprovalStatus(ApprovalStatus.REJECTED);
            case ARCHIVE -> contract.setArchiveStatus(ArchiveStatus.ARCHIVED);
            default -> {
            }
        }
    }

    private void approvalPassed(Contract contract) {
        contract.setApprovalStatus(ApprovalStatus.APPROVED);
        contract.setSigningStatus(SigningStatus.BOTH_SIGNED);
    }

    private Contract findContract(Long id) {
        return contractRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("合同不存在"));
    }

    private void applyRequest(Contract contract, ContractRequest request) {
        contract.setContractNo(request.contractNo());
        contract.setName(request.name());
        contract.setSupplierName(request.supplierName());
        contract.setOwner(request.owner());
        contract.setDepartment(request.department());
        contract.setCategory(request.category());
        contract.setPurchaseContent(request.purchaseContent());
        contract.setPurchaseQuantity(request.purchaseQuantity());
        contract.setPurchaseMethod(request.purchaseMethod());
        contract.setSupplierContact(request.supplierContact());
        contract.setSupplierPhone(request.supplierPhone());
        contract.setDeliveryLocation(request.deliveryLocation());
        contract.setPaymentMethod(request.paymentMethod());
        contract.setAcceptanceCriteria(request.acceptanceCriteria());
        contract.setSigningMethod(request.signingMethod());
        contract.setContractSource(request.contractSource());
        contract.setAmount(request.amount());
        contract.setSignDate(request.signDate());
        contract.setEffectiveDate(request.effectiveDate());
        contract.setExpiryDate(request.expiryDate());
        contract.setRemark(request.remark());
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private void savePaymentRecord(Contract contract, ContractActionRequest request, BigDecimal paymentRecordAmount) {
        paymentRecordRepository.save(new ContractPaymentRecord(
                contract.getId(),
                defaultText(request.paymentStage(), "合同付款"),
                paymentRecordAmount,
                contract.getPaidAmount(),
                request.paymentDate() == null ? LocalDate.now() : request.paymentDate(),
                request.invoiceNo(),
                request.comment(),
                request.operator()
        ));
    }

    private void saveAcceptanceRecord(Contract contract, ContractActionRequest request) {
        acceptanceRecordRepository.save(new ContractAcceptanceRecord(
                contract.getId(),
                request.deliveryDate(),
                request.acceptanceDate() == null ? LocalDate.now() : request.acceptanceDate(),
                defaultText(request.acceptanceResult(), "验收通过"),
                request.comment(),
                request.exceptionNote(),
                request.operator()
        ));
    }

    private String defaultText(String value, String fallback) {
        return hasText(value) ? value.trim() : fallback;
    }
}

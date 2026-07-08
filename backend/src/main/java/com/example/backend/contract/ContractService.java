package com.example.backend.contract;

import com.example.backend.contract.dto.ContractActionRequest;
import com.example.backend.contract.dto.ContractAcceptanceRequest;
import com.example.backend.contract.dto.ContractAttachmentRequest;
import com.example.backend.contract.dto.ContractDetailResponse;
import com.example.backend.contract.dto.ContractPageResponse;
import com.example.backend.contract.dto.ContractPaymentPlanRequest;
import com.example.backend.contract.dto.ContractRequest;
import com.example.backend.contract.dto.ContractStatsResponse;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private final ContractAttachmentRepository attachmentRepository;
    private final ContractPaymentPlanRepository paymentPlanRepository;
    private final Map<ContractAction, ContractStatus> requiredStatus = new EnumMap<>(ContractAction.class);

    public ContractService(ContractRepository contractRepository, ContractFlowLogRepository logRepository,
                           ContractPaymentRecordRepository paymentRecordRepository,
                           ContractAcceptanceRecordRepository acceptanceRecordRepository,
                           ContractAttachmentRepository attachmentRepository,
                           ContractPaymentPlanRepository paymentPlanRepository) {
        this.contractRepository = contractRepository;
        this.logRepository = logRepository;
        this.paymentRecordRepository = paymentRecordRepository;
        this.acceptanceRecordRepository = acceptanceRecordRepository;
        this.attachmentRepository = attachmentRepository;
        this.paymentPlanRepository = paymentPlanRepository;
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
    public ContractPageResponse search(String keyword, String supplierName, String owner, String department, String category,
                                       ContractStatus status, List<ContractStatus> statuses, PaymentStatus paymentStatus,
                                       ArchiveStatus archiveStatus, BigDecimal amountMin, BigDecimal amountMax,
                                       LocalDate signStart, LocalDate signEnd, LocalDate dueStart, LocalDate dueEnd,
                                       Boolean expiringSoon, String quickFilter, int page, int size) {
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
                            builder.notEqual(root.get("status"), ContractStatus.ARCHIVED),
                            builder.notEqual(root.get("status"), ContractStatus.TERMINATED),
                            builder.notEqual(root.get("status"), ContractStatus.CANCELLED)
                    ));
                    case "expiringSoon" -> predicates.add(builder.between(root.get("expiryDate"), today, today.plusDays(30)));
                    default -> {
                    }
                }
            }
            return builder.and(predicates.toArray(new Predicate[0]));
        };
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        Page<Contract> result = contractRepository.findAll(spec, PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "updatedAt")));
        return new ContractPageResponse(result.getContent(), result.getTotalElements(), result.getTotalPages(), result.getNumber(), result.getSize());
    }

    @Transactional(readOnly = true)
    public ContractDetailResponse detail(Long id) {
        Contract contract = findContract(id);
        return new ContractDetailResponse(
                contract,
                paymentPlanRepository.findByContractIdOrderByPlannedDateAscCreatedAtAsc(id),
                paymentRecordRepository.findByContractIdOrderByPaymentDateAscCreatedAtAsc(id),
                acceptanceRecordRepository.findByContractIdOrderByAcceptanceDateAscCreatedAtAsc(id),
                attachmentRepository.findByContractIdOrderByUploadedAtDesc(id),
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
    public void delete(Long id) {
        Contract contract = findContract(id);
        validateDeleteAllowed(contract);
        paymentPlanRepository.deleteAll(paymentPlanRepository.findByContractIdOrderByPlannedDateAscCreatedAtAsc(id));
        attachmentRepository.deleteAll(attachmentRepository.findByContractIdOrderByUploadedAtDesc(id));
        logRepository.deleteAll(logRepository.findByContractIdOrderByOperatedAtAsc(id));
        contractRepository.delete(contract);
    }

    @Transactional
    public ContractPaymentPlan addPaymentPlan(Long id, ContractPaymentPlanRequest request) {
        Contract contract = findContract(id);
        if (contract.getStatus() == ContractStatus.ARCHIVED || contract.getStatus() == ContractStatus.TERMINATED) {
            throw new IllegalStateException("已归档或已终止的合同不能新增付款计划");
        }
        if (request.plannedAmount().compareTo(contract.getAmount()) > 0) {
            throw new IllegalArgumentException("计划付款金额不能大于合同金额");
        }
        validatePaymentPlanRatio(contract, request);
        validatePaymentPlanTotal(contract, request);
        return paymentPlanRepository.save(new ContractPaymentPlan(
                id,
                request.paymentStage(),
                request.plannedRatio(),
                request.plannedAmount(),
                request.plannedDate(),
                request.paymentCondition(),
                request.creator()
        ));
    }

    @Transactional
    public ContractAttachment addAttachment(Long id, ContractAttachmentRequest request) {
        findContract(id);
        return attachmentRepository.save(new ContractAttachment(
                id,
                request.fileName(),
                request.fileType(),
                request.uploader(),
                request.remark()
        ));
    }

    @Transactional
    public ContractAcceptanceRecord addAcceptance(Long id, ContractAcceptanceRequest request) {
        Contract contract = findContract(id);
        if (contract.getStatus() != ContractStatus.EXECUTING && contract.getStatus() != ContractStatus.COMPLETED) {
            throw new IllegalStateException("仅履约中或已完成合同允许登记验收");
        }
        ContractAcceptanceRecord record = acceptanceRecordRepository.save(new ContractAcceptanceRecord(
                id,
                request.deliveryDate(),
                request.acceptanceDate() == null ? LocalDate.now() : request.acceptanceDate(),
                request.acceptanceResult(),
                request.acceptanceNote(),
                request.exceptionNote(),
                request.accepter()
        ));
        logRepository.save(new ContractFlowLog(id, ContractAction.REGISTER_ACCEPTANCE, request.accepter(), contract.getStatus(), contract.getStatus(), request.acceptanceNote()));
        return record;
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
            validatePaymentPlan(contract, request, paymentRecordAmount);
            updatePayment(contract, paymentRecordAmount);
        }
        applySubStatus(contract, request.action());
        Contract saved = contractRepository.save(contract);
        if (request.action() == ContractAction.REGISTER_PAYMENT) {
            savePaymentRecord(saved, request, paymentRecordAmount);
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
                contracts.stream().filter(item -> item.getPaymentStatus() != PaymentStatus.PAID
                        && item.getStatus() != ContractStatus.ARCHIVED
                        && item.getStatus() != ContractStatus.TERMINATED
                        && item.getStatus() != ContractStatus.CANCELLED).count(),
                contracts.stream().filter(item -> !item.getExpiryDate().isBefore(today) && !item.getExpiryDate().isAfter(today.plusDays(30))).count()
        );
    }

    private ContractStatus nextStatus(Contract contract, ContractActionRequest request) {
        validateActionAllowed(contract, request.action());
        if (request.action() == ContractAction.CANCEL_PROCESS) {
            if (contract.getStatus() != ContractStatus.DRAFT
                    && contract.getStatus() != ContractStatus.SUPPLIER_CONFIRMING
                    && contract.getStatus() != ContractStatus.REJECTED) {
                throw new IllegalStateException("仅草稿、供应商确认中或已驳回合同允许取消流程");
            }
            return ContractStatus.CANCELLED;
        }
        if (request.action() == ContractAction.WITHDRAW_APPROVAL) {
            if (contract.getStatus() != ContractStatus.PENDING_APPROVAL) {
                throw new IllegalStateException("仅待审批合同允许撤回审批");
            }
            if (!hasText(request.comment())) {
                throw new IllegalArgumentException("撤回审批必须填写原因");
            }
            return ContractStatus.SUPPLIER_CONFIRMING;
        }
        if (request.action() == ContractAction.TERMINATE) {
            if (contract.getStatus() != ContractStatus.ACTIVE && contract.getStatus() != ContractStatus.EXECUTING) {
                throw new IllegalStateException("仅已生效或执行中的合同允许终止");
            }
            if (!hasText(request.comment())) {
                throw new IllegalArgumentException("终止合同必须填写终止原因和结算说明");
            }
            return ContractStatus.TERMINATED;
        }
        if (request.action() == ContractAction.REQUEST_TERMINATION) {
            if (contract.getStatus() != ContractStatus.ACTIVE && contract.getStatus() != ContractStatus.EXECUTING) {
                throw new IllegalStateException("仅已生效或执行中的合同允许发起终止申请");
            }
            if (!hasText(request.comment())) {
                throw new IllegalArgumentException("发起终止申请必须填写终止原因和结算说明");
            }
            return ContractStatus.TERMINATED;
        }
        if (request.action() == ContractAction.APPROVE_TERMINATION) {
            if (contract.getStatus() != ContractStatus.TERMINATION_PENDING) {
                throw new IllegalStateException("仅终止审批中的合同允许确认终止");
            }
            if (!hasText(request.comment())) {
                throw new IllegalArgumentException("确认终止必须填写审批意见");
            }
            return ContractStatus.TERMINATED;
        }
        if (request.action() == ContractAction.REJECT_TERMINATION) {
            if (contract.getStatus() != ContractStatus.TERMINATION_PENDING) {
                throw new IllegalStateException("仅终止审批中的合同允许驳回终止");
            }
            if (!hasText(request.comment())) {
                throw new IllegalArgumentException("驳回终止必须填写审批意见");
            }
            return previousStatusBeforeTermination(contract.getId());
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
        if (request.action() == ContractAction.COMPLETE) {
            if (contract.getPaymentStatus() != PaymentStatus.PAID) {
                throw new IllegalStateException("合同未全额付款，不能完成合同");
            }
            if (acceptanceRecordRepository.findByContractIdOrderByAcceptanceDateAscCreatedAtAsc(contract.getId()).isEmpty()) {
                throw new IllegalStateException("请先登记验收记录，再完成合同");
            }
        }
        if (request.action() == ContractAction.ARCHIVE) {
            validateArchiveReady(contract);
        }
        return switch (request.action()) {
            case SUBMIT_SUPPLIER_CONFIRM -> ContractStatus.SUPPLIER_CONFIRMING;
            case SUPPLIER_CONFIRMED -> ContractStatus.SUPPLIER_CONFIRMING;
            case SUBMIT_APPROVAL -> ContractStatus.PENDING_APPROVAL;
            case CANCEL_PROCESS -> ContractStatus.CANCELLED;
            case WITHDRAW_APPROVAL -> ContractStatus.SUPPLIER_CONFIRMING;
            case APPROVE -> ContractStatus.ACTIVE;
            case REJECT -> ContractStatus.REJECTED;
            case START_EXECUTION -> ContractStatus.EXECUTING;
            case REGISTER_PAYMENT -> contract.getStatus();
            case REGISTER_ACCEPTANCE -> contract.getStatus();
            case COMPLETE -> ContractStatus.COMPLETED;
            case ARCHIVE -> ContractStatus.ARCHIVED;
            case REQUEST_TERMINATION -> ContractStatus.TERMINATED;
            case APPROVE_TERMINATION -> ContractStatus.TERMINATED;
            case REJECT_TERMINATION -> previousStatusBeforeTermination(contract.getId());
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
        if (contract.getStatus() == ContractStatus.CANCELLED) {
            throw new IllegalStateException("合同流程已取消，不能继续流程操作");
        }
        if (contract.getStatus() == ContractStatus.TERMINATION_PENDING
                && action != ContractAction.APPROVE_TERMINATION
                && action != ContractAction.REJECT_TERMINATION) {
            throw new IllegalStateException("合同终止审批中，仅允许确认或驳回终止");
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
        if (action == ContractAction.CANCEL_PROCESS
                || action == ContractAction.WITHDRAW_APPROVAL
                || action == ContractAction.REJECT
                || action == ContractAction.REQUEST_TERMINATION
                || action == ContractAction.TERMINATE) {
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
            case WITHDRAW_APPROVAL -> contract.setApprovalStatus(ApprovalStatus.NOT_SUBMITTED);
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

    private void validateDeleteAllowed(Contract contract) {
        boolean deletableStatus = contract.getStatus() == ContractStatus.DRAFT
                || contract.getStatus() == ContractStatus.REJECTED
                || contract.getStatus() == ContractStatus.CANCELLED;
        if (!deletableStatus) {
            throw new IllegalStateException("仅草稿、已驳回或已取消且未形成业务事实的合同允许删除");
        }
        BigDecimal paidAmount = contract.getPaidAmount() == null ? BigDecimal.ZERO : contract.getPaidAmount();
        if (paidAmount.compareTo(BigDecimal.ZERO) > 0 || contract.getPaymentStatus() != PaymentStatus.UNPAID) {
            throw new IllegalStateException("合同已有付款信息，不能删除");
        }
        if (!paymentRecordRepository.findByContractIdOrderByPaymentDateAscCreatedAtAsc(contract.getId()).isEmpty()) {
            throw new IllegalStateException("合同已有付款记录，不能删除");
        }
        if (!acceptanceRecordRepository.findByContractIdOrderByAcceptanceDateAscCreatedAtAsc(contract.getId()).isEmpty()) {
            throw new IllegalStateException("合同已有验收记录，不能删除");
        }
        if (contract.getArchiveStatus() == ArchiveStatus.ARCHIVED) {
            throw new IllegalStateException("已归档合同不能删除");
        }
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
        ContractPaymentRecord record = new ContractPaymentRecord(
                contract.getId(),
                defaultText(request.paymentStage(), "合同付款"),
                paymentRecordAmount,
                contract.getPaidAmount(),
                request.paymentDate() == null ? LocalDate.now() : request.paymentDate(),
                request.invoiceNo(),
                request.comment(),
                request.operator()
        );
        record.setPaymentPlanId(request.paymentPlanId());
        paymentRecordRepository.save(record);
        if (request.paymentPlanId() != null) {
            paymentPlanRepository.findById(request.paymentPlanId()).ifPresent(plan -> {
                if (plan.getContractId().equals(contract.getId())) {
                    BigDecimal planPaidAmount = plan.getPaidAmount() == null ? BigDecimal.ZERO : plan.getPaidAmount();
                    BigDecimal paidAmountAfterPayment = planPaidAmount.add(paymentRecordAmount);
                    plan.setPaidAmount(paidAmountAfterPayment);
                    plan.setPaid(paidAmountAfterPayment.compareTo(plan.getPlannedAmount()) >= 0);
                    paymentPlanRepository.save(plan);
                }
            });
        }
    }

    private void validatePaymentPlan(Contract contract, ContractActionRequest request, BigDecimal paymentAmount) {
        if (request.paymentPlanId() == null) {
            return;
        }
        ContractPaymentPlan plan = paymentPlanRepository.findById(request.paymentPlanId())
                .orElseThrow(() -> new IllegalArgumentException("付款计划不存在"));
        if (!plan.getContractId().equals(contract.getId())) {
            throw new IllegalArgumentException("付款计划不属于当前合同");
        }
        BigDecimal planPaidAmount = plan.getPaidAmount() == null ? BigDecimal.ZERO : plan.getPaidAmount();
        BigDecimal remainingPlanAmount = plan.getPlannedAmount().subtract(planPaidAmount);
        if (remainingPlanAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("该付款计划已付清");
        }
        if (paymentAmount.compareTo(remainingPlanAmount) > 0) {
            throw new IllegalArgumentException("本次付款金额不能大于该付款计划剩余金额");
        }
        BigDecimal contractPaidAmount = contract.getPaidAmount() == null ? BigDecimal.ZERO : contract.getPaidAmount();
        BigDecimal remainingContractAmount = contract.getAmount().subtract(contractPaidAmount);
        BigDecimal expectedPaymentAmount = remainingPlanAmount.min(remainingContractAmount);
        if (paymentAmount.compareTo(expectedPaymentAmount) != 0) {
            throw new IllegalArgumentException("已选择付款计划时，本次付款金额必须等于计划和合同剩余可付金额");
        }
    }

    private void validatePaymentPlanRatio(Contract contract, ContractPaymentPlanRequest request) {
        if (request.plannedRatio() == null || contract.getAmount().compareTo(BigDecimal.ZERO) == 0) {
            return;
        }
        BigDecimal expectedAmount = contract.getAmount()
                .multiply(request.plannedRatio())
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal diff = expectedAmount.subtract(request.plannedAmount()).abs();
        if (diff.compareTo(new BigDecimal("0.01")) > 0) {
            throw new IllegalArgumentException("付款比例与计划金额不一致，请按合同金额重新计算");
        }
    }

    private void validatePaymentPlanTotal(Contract contract, ContractPaymentPlanRequest request) {
        List<ContractPaymentPlan> existingPlans = paymentPlanRepository.findByContractIdOrderByPlannedDateAscCreatedAtAsc(contract.getId());
        BigDecimal totalRatio = existingPlans.stream()
                .map(ContractPaymentPlan::getPlannedRatio)
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .add(request.plannedRatio() == null ? BigDecimal.ZERO : request.plannedRatio());
        if (totalRatio.compareTo(new BigDecimal("100.00")) > 0) {
            throw new IllegalArgumentException("付款计划比例合计不能超过 100%");
        }
        BigDecimal totalAmount = existingPlans.stream()
                .map(ContractPaymentPlan::getPlannedAmount)
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .add(request.plannedAmount());
        if (totalAmount.compareTo(contract.getAmount()) > 0) {
            throw new IllegalArgumentException("付款计划金额合计不能大于合同金额");
        }
    }

    private ContractStatus previousStatusBeforeTermination(Long contractId) {
        return logRepository.findByContractIdOrderByOperatedAtAsc(contractId).stream()
                .filter(log -> log.getAction() == ContractAction.REQUEST_TERMINATION)
                .reduce((first, second) -> second)
                .map(ContractFlowLog::getFromStatus)
                .filter(status -> status == ContractStatus.ACTIVE || status == ContractStatus.EXECUTING)
                .orElse(ContractStatus.EXECUTING);
    }

    private String defaultText(String value, String fallback) {
        return hasText(value) ? value.trim() : fallback;
    }

    private void validateArchiveReady(Contract contract) {
        if (contract.getPaymentStatus() != PaymentStatus.PAID) {
            throw new IllegalStateException("合同未全额付款，不能归档");
        }
        if (acceptanceRecordRepository.findByContractIdOrderByAcceptanceDateAscCreatedAtAsc(contract.getId()).isEmpty()) {
            throw new IllegalStateException("缺少验收记录，不能归档");
        }
        boolean hasContractFile = attachmentRepository.findByContractIdOrderByUploadedAtDesc(contract.getId()).stream()
                .anyMatch(item -> "合同正文".equals(item.getFileType()));
        if (!hasContractFile) {
            throw new IllegalStateException("缺少合同正文附件，不能归档");
        }
    }
}

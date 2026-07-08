package com.example.backend.contract;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class DemoDataInitializer implements CommandLineRunner {
    private final ContractRepository contractRepository;
    private final ContractFlowLogRepository logRepository;
    private final ContractPaymentRecordRepository paymentRecordRepository;
    private final ContractAcceptanceRecordRepository acceptanceRecordRepository;
    private final ContractAttachmentRepository attachmentRepository;
    private final ContractPaymentPlanRepository paymentPlanRepository;

    public DemoDataInitializer(ContractRepository contractRepository, ContractFlowLogRepository logRepository,
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
    }

    @Override
    public void run(String... args) {
        if (contractRepository.count() > 0) {
            return;
        }

        Contract draft = saveContract("PO-2026-001", "办公电脑采购合同", "上海云启科技有限公司", "王敏",
                "行政部", "办公设备", "185000.00", "0.00", LocalDate.now().minusDays(4), null,
                LocalDate.now().plusDays(120), ContractStatus.DRAFT, PaymentStatus.UNPAID, "用于研发团队批量更换办公电脑。");
        logRepository.save(new ContractFlowLog(draft.getId(), ContractAction.SUBMIT_SUPPLIER_CONFIRM, "王敏", null, ContractStatus.DRAFT, "采购合同草稿创建完成"));

        Contract approval = saveContract("PO-2026-002", "年度云服务器采购合同", "北京星河云计算有限公司", "陈浩",
                "技术部", "云服务", "320000.00", "0.00", LocalDate.now().minusDays(10), null,
                LocalDate.now().plusDays(365), ContractStatus.PENDING_APPROVAL, PaymentStatus.UNPAID, "年度服务器资源包采购。");
        logRepository.save(new ContractFlowLog(approval.getId(), ContractAction.SUBMIT_SUPPLIER_CONFIRM, "陈浩", null, ContractStatus.DRAFT, "合同拟稿"));
        logRepository.save(new ContractFlowLog(approval.getId(), ContractAction.SUBMIT_APPROVAL, "陈浩", ContractStatus.SUPPLIER_CONFIRMING, ContractStatus.PENDING_APPROVAL, "供应商确认后提交审批"));

        Contract executing = saveContract("PO-2026-003", "网络安全服务采购合同", "深圳安盾信息安全有限公司", "李倩",
                "信息安全部", "安全服务", "96000.00", "48000.00", LocalDate.now().minusDays(45), LocalDate.now().minusDays(30),
                LocalDate.now().plusDays(24), ContractStatus.EXECUTING, PaymentStatus.PARTIAL, "渗透测试和应急响应服务，临近到期需跟进验收。");
        logRepository.save(new ContractFlowLog(executing.getId(), ContractAction.APPROVE, "赵总", ContractStatus.PENDING_APPROVAL, ContractStatus.ACTIVE, "审批通过"));
        logRepository.save(new ContractFlowLog(executing.getId(), ContractAction.START_EXECUTION, "李倩", ContractStatus.ACTIVE, ContractStatus.EXECUTING, "服务开始执行"));
        logRepository.save(new ContractFlowLog(executing.getId(), ContractAction.REGISTER_PAYMENT, "财务刘洋", ContractStatus.EXECUTING, ContractStatus.EXECUTING, "已支付首款"));
        paymentRecordRepository.save(new ContractPaymentRecord(executing.getId(), "首付款", new BigDecimal("48000.00"), new BigDecimal("48000.00"), LocalDate.now().minusDays(20), "INV-2026-0301", "服务启动后支付 50% 首款", "财务刘洋"));
        addStandardPlans(executing.getId(), new BigDecimal("96000.00"), new BigDecimal("48000.00"), LocalDate.now().minusDays(20));
        addStandardAttachments(executing.getId(), "网络安全服务");

        Contract completed = saveContract("PO-2026-004", "办公家具采购合同", "杭州木禾办公家具有限公司", "王敏",
                "行政部", "办公家具", "58000.00", "58000.00", LocalDate.now().minusDays(90), LocalDate.now().minusDays(80),
                LocalDate.now().minusDays(5), ContractStatus.COMPLETED, PaymentStatus.PAID, "家具已验收，等待档案归档。");
        logRepository.save(new ContractFlowLog(completed.getId(), ContractAction.COMPLETE, "王敏", ContractStatus.EXECUTING, ContractStatus.COMPLETED, "履约与付款完成"));
        paymentRecordRepository.save(new ContractPaymentRecord(completed.getId(), "验收款", new BigDecimal("58000.00"), new BigDecimal("58000.00"), LocalDate.now().minusDays(12), "INV-2026-0401", "验收通过后一次性付款", "财务刘洋"));
        acceptanceRecordRepository.save(new ContractAcceptanceRecord(completed.getId(), LocalDate.now().minusDays(14), LocalDate.now().minusDays(10), "验收通过", "桌椅、柜体数量和质量符合合同约定。", "", "王敏"));
        addStandardPlans(completed.getId(), new BigDecimal("58000.00"), new BigDecimal("58000.00"), LocalDate.now().minusDays(70));
        addStandardAttachments(completed.getId(), "办公家具");

        Contract rejected = saveContract("PO-2026-005", "会议系统升级采购合同", "南京声景智能科技有限公司", "周悦",
                "行政部", "会议设备", "76000.00", "0.00", LocalDate.now().minusDays(6), null,
                LocalDate.now().plusDays(90), ContractStatus.REJECTED, PaymentStatus.UNPAID, "审批时要求补充售后服务条款。");
        logRepository.save(new ContractFlowLog(rejected.getId(), ContractAction.REJECT, "赵总", ContractStatus.PENDING_APPROVAL, ContractStatus.REJECTED, "请补充三年维保条款后重新提交。"));

        Contract active = saveContract("PO-2026-006", "工位改造施工服务合同", "苏州筑方工程服务有限公司", "王敏",
                "行政部", "施工服务", "148000.00", "0.00", LocalDate.now().minusDays(18), LocalDate.now().minusDays(12),
                LocalDate.now().plusDays(70), ContractStatus.ACTIVE, PaymentStatus.UNPAID, "审批通过，待项目负责人启动履约。");
        logRepository.save(new ContractFlowLog(active.getId(), ContractAction.APPROVE, "赵总", ContractStatus.PENDING_APPROVAL, ContractStatus.ACTIVE, "预算和施工周期确认无误。"));
        addStandardPlans(active.getId(), new BigDecimal("148000.00"), BigDecimal.ZERO, LocalDate.now().plusDays(10));
        addStandardAttachments(active.getId(), "工位改造");

        Contract paidExecuting = saveContract("PO-2026-007", "研发实验室耗材采购合同", "上海启衡实验器材有限公司", "陈浩",
                "研发部", "实验耗材", "42500.00", "42500.00", LocalDate.now().minusDays(36), LocalDate.now().minusDays(30),
                LocalDate.now().plusDays(18), ContractStatus.EXECUTING, PaymentStatus.PAID, "货物已到场，待验收完成。");
        logRepository.save(new ContractFlowLog(paidExecuting.getId(), ContractAction.REGISTER_PAYMENT, "财务刘洋", ContractStatus.EXECUTING, ContractStatus.EXECUTING, "全额付款完成，待验收。"));
        paymentRecordRepository.save(new ContractPaymentRecord(paidExecuting.getId(), "到货款", new BigDecimal("42500.00"), new BigDecimal("42500.00"), LocalDate.now().minusDays(8), "INV-2026-0701", "到货后全额付款。", "财务刘洋"));
        addStandardPlans(paidExecuting.getId(), new BigDecimal("42500.00"), new BigDecimal("42500.00"), LocalDate.now().minusDays(20));
        addStandardAttachments(paidExecuting.getId(), "实验耗材");

        Contract archived = saveContract("PO-2026-008", "年度打印服务采购合同", "上海印捷办公服务有限公司", "王敏",
                "行政部", "办公服务", "36000.00", "36000.00", LocalDate.now().minusDays(210), LocalDate.now().minusDays(200),
                LocalDate.now().minusDays(30), ContractStatus.ARCHIVED, PaymentStatus.PAID, "服务期结束并完成归档。");
        logRepository.save(new ContractFlowLog(archived.getId(), ContractAction.ARCHIVE, "档案管理员", ContractStatus.COMPLETED, ContractStatus.ARCHIVED, "ARCH-2026-008；行政档案柜 A-02"));
        paymentRecordRepository.save(new ContractPaymentRecord(archived.getId(), "尾款", new BigDecimal("36000.00"), new BigDecimal("36000.00"), LocalDate.now().minusDays(45), "INV-2026-0801", "服务完成后支付尾款。", "财务刘洋"));
        acceptanceRecordRepository.save(new ContractAcceptanceRecord(archived.getId(), LocalDate.now().minusDays(60), LocalDate.now().minusDays(50), "验收通过", "打印服务按月结算，服务质量达标。", "", "王敏"));
        addStandardPlans(archived.getId(), new BigDecimal("36000.00"), new BigDecimal("36000.00"), LocalDate.now().minusDays(180));
        addStandardAttachments(archived.getId(), "打印服务");

        Contract terminated = saveContract("PO-2026-009", "临时仓储服务采购合同", "昆山盛达仓储有限公司", "李倩",
                "供应链部", "仓储服务", "52000.00", "0.00", LocalDate.now().minusDays(28), null,
                LocalDate.now().plusDays(42), ContractStatus.TERMINATED, PaymentStatus.UNPAID, "因项目取消，合同提前终止。");
        logRepository.save(new ContractFlowLog(terminated.getId(), ContractAction.APPROVE, "赵总", ContractStatus.PENDING_APPROVAL, ContractStatus.ACTIVE, "合同审批通过。"));
        logRepository.save(new ContractFlowLog(terminated.getId(), ContractAction.REQUEST_TERMINATION, "李倩", ContractStatus.ACTIVE, ContractStatus.TERMINATION_PENDING, "供应商仓储资源无法按期提供，申请终止并确认无付款结算。"));
        logRepository.save(new ContractFlowLog(terminated.getId(), ContractAction.APPROVE_TERMINATION, "赵总", ContractStatus.TERMINATION_PENDING, ContractStatus.TERMINATED, "同意终止，双方无未结算款项。"));

        Contract overdue = saveContract("PO-2026-010", "车间检测仪器采购合同", "无锡精测仪器有限公司", "陈浩",
                "研发部", "检测仪器", "218000.00", "109000.00", LocalDate.now().minusDays(150), LocalDate.now().minusDays(140),
                LocalDate.now().minusDays(3), ContractStatus.EXECUTING, PaymentStatus.PARTIAL, "合同已到期，仍有尾款和验收事项未关闭。");
        paymentRecordRepository.save(new ContractPaymentRecord(overdue.getId(), "首付款", new BigDecimal("109000.00"), new BigDecimal("109000.00"), LocalDate.now().minusDays(120), "INV-2026-1001", "设备发货前支付 50%。", "财务刘洋"));
        addStandardPlans(overdue.getId(), new BigDecimal("218000.00"), new BigDecimal("109000.00"), LocalDate.now().minusDays(120));
        addStandardAttachments(overdue.getId(), "检测仪器");
    }

    private void addStandardPlans(Long contractId, BigDecimal amount, BigDecimal paidAmount, LocalDate firstDate) {
        BigDecimal firstAmount = amount.multiply(new BigDecimal("0.30"));
        BigDecimal secondAmount = amount.multiply(new BigDecimal("0.70"));
        BigDecimal firstPaidAmount = paidAmount.min(firstAmount);
        BigDecimal secondPaidAmount = paidAmount.subtract(firstPaidAmount).max(BigDecimal.ZERO).min(secondAmount);
        paymentPlanRepository.save(buildPaymentPlan(contractId, "预付款", new BigDecimal("30.00"), firstAmount, firstPaidAmount, firstDate, "合同生效后支付"));
        paymentPlanRepository.save(buildPaymentPlan(contractId, "验收款", new BigDecimal("70.00"), secondAmount, secondPaidAmount, firstDate.plusDays(45), "验收通过后支付"));
    }

    private ContractPaymentPlan buildPaymentPlan(Long contractId, String stage, BigDecimal ratio, BigDecimal amount,
                                                 BigDecimal paidAmount, LocalDate plannedDate, String condition) {
        ContractPaymentPlan plan = new ContractPaymentPlan(contractId, stage, ratio, amount, plannedDate, condition, "系统初始化");
        plan.setPaidAmount(paidAmount);
        plan.setPaid(paidAmount.compareTo(amount) >= 0);
        return plan;
    }

    private void addStandardAttachments(Long contractId, String prefix) {
        attachmentRepository.save(new ContractAttachment(contractId, prefix + "合同正文.pdf", "合同正文", "王敏", "双方确认版本"));
        attachmentRepository.save(new ContractAttachment(contractId, prefix + "报价单.pdf", "报价单", "王敏", "供应商报价材料"));
    }

    private Contract saveContract(String contractNo, String name, String supplierName, String owner, String department,
                                  String category, String amount, String paidAmount, LocalDate signDate,
                                  LocalDate effectiveDate, LocalDate expiryDate, ContractStatus status,
                                  PaymentStatus paymentStatus, String remark) {
        Contract contract = new Contract();
        contract.setContractNo(contractNo);
        contract.setName(name);
        contract.setSupplierName(supplierName);
        contract.setOwner(owner);
        contract.setDepartment(department);
        contract.setCategory(category);
        contract.setPurchaseContent(category + "采购");
        contract.setPurchaseQuantity("1 批");
        contract.setPurchaseMethod("询价采购");
        contract.setSupplierContact("张经理");
        contract.setSupplierPhone("13800000000");
        contract.setDeliveryLocation(department + "指定地点");
        contract.setPaymentMethod("按合同节点付款");
        contract.setAcceptanceCriteria("数量、质量、交付周期符合合同约定，验收资料齐全。");
        contract.setSigningMethod("电子签署");
        contract.setContractSource("年度采购计划");
        contract.setAmount(new BigDecimal(amount));
        contract.setPaidAmount(new BigDecimal(paidAmount));
        contract.setSignDate(signDate);
        contract.setEffectiveDate(effectiveDate);
        contract.setExpiryDate(expiryDate);
        contract.setStatus(status);
        contract.setPaymentStatus(paymentStatus);
        if (status == ContractStatus.DRAFT) {
            contract.setApprovalStatus(ApprovalStatus.NOT_SUBMITTED);
            contract.setSigningStatus(SigningStatus.UNSIGNED);
        } else if (status == ContractStatus.PENDING_APPROVAL) {
            contract.setApprovalStatus(ApprovalStatus.PENDING);
            contract.setSigningStatus(SigningStatus.BOTH_SIGNED);
        } else {
            contract.setApprovalStatus(ApprovalStatus.APPROVED);
            contract.setSigningStatus(SigningStatus.BOTH_SIGNED);
        }
        contract.setArchiveStatus(status == ContractStatus.ARCHIVED ? ArchiveStatus.ARCHIVED : ArchiveStatus.UNARCHIVED);
        contract.setRemark(remark);
        return contractRepository.save(contract);
    }
}

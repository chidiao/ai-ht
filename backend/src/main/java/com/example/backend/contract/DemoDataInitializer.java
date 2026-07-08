package com.example.backend.contract;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class DemoDataInitializer implements CommandLineRunner {
    private final ContractRepository contractRepository;
    private final ContractFlowLogRepository logRepository;

    public DemoDataInitializer(ContractRepository contractRepository, ContractFlowLogRepository logRepository) {
        this.contractRepository = contractRepository;
        this.logRepository = logRepository;
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

        Contract completed = saveContract("PO-2026-004", "办公家具采购合同", "杭州木禾办公家具有限公司", "王敏",
                "行政部", "办公家具", "58000.00", "58000.00", LocalDate.now().minusDays(90), LocalDate.now().minusDays(80),
                LocalDate.now().minusDays(5), ContractStatus.COMPLETED, PaymentStatus.PAID, "家具已验收，等待档案归档。");
        logRepository.save(new ContractFlowLog(completed.getId(), ContractAction.COMPLETE, "王敏", ContractStatus.EXECUTING, ContractStatus.COMPLETED, "履约与付款完成"));
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

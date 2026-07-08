export const statusOptions = [
  { value: 'DRAFT', label: '草稿' },
  { value: 'SUPPLIER_CONFIRMING', label: '供应商确认' },
  { value: 'PENDING_APPROVAL', label: '待审批' },
  { value: 'REJECTED', label: '已驳回' },
  { value: 'CANCELLED', label: '已取消' },
  { value: 'ACTIVE', label: '已生效' },
  { value: 'EXECUTING', label: '执行中' },
  { value: 'TERMINATION_PENDING', label: '终止处理中' },
  { value: 'COMPLETED', label: '已完成' },
  { value: 'ARCHIVED', label: '已归档' },
  { value: 'TERMINATED', label: '已终止' }
]

export const paymentOptions = [
  { value: 'UNPAID', label: '未付款' },
  { value: 'PARTIAL', label: '部分付款' },
  { value: 'PAID', label: '已付款' }
]

export const approvalOptions = [
  { value: 'NOT_SUBMITTED', label: '未提交' },
  { value: 'PENDING', label: '审批中' },
  { value: 'APPROVED', label: '已通过' },
  { value: 'REJECTED', label: '已驳回' }
]

export const signingOptions = [
  { value: 'UNSIGNED', label: '未签署' },
  { value: 'OUR_SIGNED', label: '我方已签' },
  { value: 'SUPPLIER_SIGNED', label: '供应商已签' },
  { value: 'BOTH_SIGNED', label: '双方已签' }
]

export const archiveOptions = [
  { value: 'UNARCHIVED', label: '未归档' },
  { value: 'ARCHIVED', label: '已归档' }
]

export const actionOptions = {
  DRAFT: [
    { value: 'SUBMIT_SUPPLIER_CONFIRM', label: '提交供应商确认' },
    { value: 'CANCEL_PROCESS', label: '取消流程' }
  ],
  SUPPLIER_CONFIRMING: [
    { value: 'SUBMIT_APPROVAL', label: '供应商确认并提交审批' },
    { value: 'CANCEL_PROCESS', label: '取消流程' }
  ],
  PENDING_APPROVAL: [
    { value: 'WITHDRAW_APPROVAL', label: '撤回审批' },
    { value: 'APPROVE', label: '审批通过' },
    { value: 'REJECT', label: '审批驳回' }
  ],
  REJECTED: [{ value: 'CANCEL_PROCESS', label: '取消流程' }],
  ACTIVE: [
    { value: 'START_EXECUTION', label: '开始执行' },
    { value: 'REGISTER_PAYMENT', label: '登记付款' },
    { value: 'TERMINATE', label: '终止合同' }
  ],
  EXECUTING: [
    { value: 'REGISTER_PAYMENT', label: '登记付款' },
    { value: 'REGISTER_ACCEPTANCE', label: '登记验收' },
    { value: 'COMPLETE', label: '完成合同' },
    { value: 'TERMINATE', label: '终止合同' }
  ],
  COMPLETED: [{ value: 'ARCHIVE', label: '归档' }]
}

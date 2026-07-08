export const statusOptions = [
  { value: 'DRAFT', label: '草稿' },
  { value: 'SUPPLIER_CONFIRMING', label: '供应商确认' },
  { value: 'PENDING_APPROVAL', label: '待审批' },
  { value: 'REJECTED', label: '已驳回' },
  { value: 'ACTIVE', label: '已生效' },
  { value: 'EXECUTING', label: '执行中' },
  { value: 'PAYING', label: '付款中' },
  { value: 'COMPLETED', label: '已完成' },
  { value: 'ARCHIVED', label: '已归档' },
  { value: 'TERMINATED', label: '已终止' }
]

export const paymentOptions = [
  { value: 'UNPAID', label: '未付款' },
  { value: 'PARTIAL', label: '部分付款' },
  { value: 'PAID', label: '已付款' }
]

export const actionOptions = {
  DRAFT: [{ value: 'SUBMIT_SUPPLIER_CONFIRM', label: '提交供应商确认' }],
  SUPPLIER_CONFIRMING: [{ value: 'SUBMIT_APPROVAL', label: '供应商确认并提交审批' }],
  PENDING_APPROVAL: [
    { value: 'APPROVE', label: '审批通过' },
    { value: 'REJECT', label: '审批驳回' }
  ],
  ACTIVE: [{ value: 'START_EXECUTION', label: '开始执行' }],
  EXECUTING: [{ value: 'REGISTER_PAYMENT', label: '登记付款' }],
  PAYING: [
    { value: 'REGISTER_PAYMENT', label: '继续登记付款' },
    { value: 'COMPLETE', label: '完成合同' }
  ],
  COMPLETED: [{ value: 'ARCHIVE', label: '归档' }]
}

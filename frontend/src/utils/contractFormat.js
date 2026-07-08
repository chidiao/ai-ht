import { actionOptions, approvalOptions, archiveOptions, paymentOptions, signingOptions, statusOptions } from '../constants/contracts'

export function statusLabel(value) {
  return statusOptions.find((item) => item.value === value)?.label || ''
}

export function paymentLabel(value) {
  return paymentOptions.find((item) => item.value === value)?.label || ''
}

export function approvalLabel(value) {
  return approvalOptions.find((item) => item.value === value)?.label || ''
}

export function signingLabel(value) {
  return signingOptions.find((item) => item.value === value)?.label || ''
}

export function archiveLabel(value) {
  return archiveOptions.find((item) => item.value === value)?.label || ''
}

export function actionLabel(value) {
  const allActions = Object.values(actionOptions).flat().concat(
    { value: 'REGISTER_ACCEPTANCE', label: '登记验收' },
    { value: 'TERMINATE', label: '终止合同' }
  )
  return allActions.find((item) => item.value === value)?.label || value
}

export function statusType(value) {
  return {
    DRAFT: 'info',
    SUPPLIER_CONFIRMING: 'warning',
    PENDING_APPROVAL: 'warning',
    REJECTED: 'danger',
    CANCELLED: 'info',
    ACTIVE: 'success',
    EXECUTING: 'primary',
    TERMINATION_PENDING: 'warning',
    COMPLETED: 'success',
    ARCHIVED: 'info',
    TERMINATED: 'danger'
  }[value]
}

export function paymentType(value) {
  return { UNPAID: 'danger', PARTIAL: 'warning', PAID: 'success' }[value]
}

export function money(value) {
  return Number(value || 0).toLocaleString('zh-CN', { style: 'currency', currency: 'CNY' })
}

export function isExpiring(dateValue) {
  const today = new Date()
  const expiry = new Date(`${dateValue}T00:00:00`)
  const diff = (expiry.getTime() - today.getTime()) / 86400000
  return diff >= 0 && diff <= 30
}

export function formatDateTime(value) {
  return value ? value.replace('T', ' ').slice(0, 16) : ''
}

export function availableActions(row) {
  let actions = [...(actionOptions[row.status] || [])]
  if (isExpiredBeforeEffective(row)) {
    actions = actions.filter((action) => ['CANCEL_PROCESS', 'WITHDRAW_APPROVAL', 'REJECT'].includes(action.value))
  }
  if (row.status === 'EXECUTING' && row.paymentStatus !== 'PAID') {
    actions = actions.filter((action) => action.value !== 'COMPLETE')
  }
  if (['ACTIVE', 'EXECUTING'].includes(row.status) && row.paymentStatus === 'PAID') {
    actions = actions.filter((action) => action.value !== 'REGISTER_PAYMENT')
  }
  return actions
}

function isExpiredBeforeEffective(row) {
  if (!row?.expiryDate || !['DRAFT', 'SUPPLIER_CONFIRMING', 'PENDING_APPROVAL', 'REJECTED'].includes(row.status)) {
    return false
  }
  const today = new Date()
  const expiry = new Date(`${row.expiryDate}T00:00:00`)
  return expiry.getTime() < new Date(today.toISOString().slice(0, 10)).getTime()
}

export function canEdit(row) {
  return ['DRAFT', 'REJECTED'].includes(row.status)
}

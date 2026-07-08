import { actionOptions, paymentOptions, statusOptions } from '../constants/contracts'

export function statusLabel(value) {
  return statusOptions.find((item) => item.value === value)?.label || ''
}

export function paymentLabel(value) {
  return paymentOptions.find((item) => item.value === value)?.label || ''
}

export function actionLabel(value) {
  const allActions = Object.values(actionOptions).flat().concat({ value: 'TERMINATE', label: '终止合同' })
  return allActions.find((item) => item.value === value)?.label || value
}

export function statusType(value) {
  return {
    DRAFT: 'info',
    SUPPLIER_CONFIRMING: 'warning',
    PENDING_APPROVAL: 'warning',
    REJECTED: 'danger',
    ACTIVE: 'success',
    EXECUTING: 'primary',
    PAYING: 'primary',
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
  const actions = [...(actionOptions[row.status] || [])]
  if (!['COMPLETED', 'ARCHIVED', 'TERMINATED'].includes(row.status)) {
    actions.push({ value: 'TERMINATE', label: '终止合同' })
  }
  return actions
}

export function canEdit(row) {
  return ['DRAFT', 'REJECTED'].includes(row.status)
}

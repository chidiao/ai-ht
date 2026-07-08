export function canRunAction(role, action) {
  if (!role || !action) {
    return false
  }
  const actionRoles = {
    SUBMIT_SUPPLIER_CONFIRM: ['PURCHASER', 'ADMIN'],
    SUBMIT_APPROVAL: ['PURCHASER', 'ADMIN'],
    CANCEL_PROCESS: ['PURCHASER', 'ADMIN'],
    WITHDRAW_APPROVAL: ['PURCHASER', 'ADMIN'],
    APPROVE: ['APPROVER', 'ADMIN'],
    REJECT: ['APPROVER', 'ADMIN'],
    START_EXECUTION: ['ACCEPTOR', 'ADMIN'],
    REGISTER_PAYMENT: ['FINANCE', 'ADMIN'],
    REGISTER_ACCEPTANCE: ['ACCEPTOR', 'ADMIN'],
    COMPLETE: ['ADMIN'],
    ARCHIVE: ['ARCHIVIST', 'ADMIN'],
    TERMINATE: ['ADMIN']
  }
  return (actionRoles[action] || []).includes(normalizeRole(role))
}

export function canCreateContract(role) {
  return ['PURCHASER', 'ADMIN'].includes(normalizeRole(role))
}

export function canEditContract(role, contract) {
  return ['PURCHASER', 'ADMIN'].includes(normalizeRole(role)) && ['DRAFT', 'REJECTED'].includes(contract?.status)
}

export function canDeleteContract(role, contract) {
  return ['PURCHASER', 'ADMIN'].includes(normalizeRole(role))
    && ['DRAFT', 'REJECTED', 'CANCELLED'].includes(contract?.status)
    && Number(contract?.paidAmount || 0) === 0
    && contract?.paymentStatus === 'UNPAID'
}

export function canExportContracts(role) {
  return ['PURCHASER', 'APPROVER', 'FINANCE', 'ACCEPTOR', 'ARCHIVIST', 'ADMIN'].includes(normalizeRole(role))
}

export function canManagePaymentPlans(role, contract) {
  const editableStatus = ['DRAFT', 'SUPPLIER_CONFIRMING', 'REJECTED', 'ACTIVE'].includes(contract?.status)
  const unpaid = Number(contract?.paidAmount || 0) === 0 && contract?.paymentStatus === 'UNPAID'
  return ['PURCHASER', 'ADMIN'].includes(normalizeRole(role)) && editableStatus && unpaid
}

export function canManageAttachments(role, contract) {
  const editableStatus = !['CANCELLED', 'ARCHIVED'].includes(contract?.status)
  return ['PURCHASER', 'FINANCE', 'ACCEPTOR', 'ARCHIVIST', 'ADMIN'].includes(normalizeRole(role)) && editableStatus
}

export function normalizeRole(role) {
  if (role === 'NORMAL') {
    return 'PURCHASER'
  }
  if (role === 'ADVANCED') {
    return 'ADMIN'
  }
  return role
}

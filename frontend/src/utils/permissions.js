export function canRunAction(role, action) {
  if (!role || !action) {
    return false
  }
  const normalActions = ['SUBMIT_SUPPLIER_CONFIRM', 'SUBMIT_APPROVAL']
  const advancedActions = [
    ...normalActions,
    'APPROVE',
    'REJECT',
    'START_EXECUTION',
    'REGISTER_PAYMENT',
    'COMPLETE',
    'ARCHIVE',
    'TERMINATE'
  ]
  return role === 'ADVANCED' ? advancedActions.includes(action) : normalActions.includes(action)
}

export function canCreateContract(role) {
  return role === 'NORMAL' || role === 'ADVANCED'
}

export function canEditContract(role, contract) {
  return Boolean(role) && ['DRAFT', 'REJECTED'].includes(contract?.status)
}

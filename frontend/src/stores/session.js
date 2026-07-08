import { computed, ref } from 'vue'

const ROLE_KEY = 'contract_role'
const role = ref(localStorage.getItem(ROLE_KEY) || '')

export const roleOptions = [
  { value: 'PURCHASER', label: '采购经办人', userName: '王敏', description: '创建合同、维护资料、提交供应商确认和内部审批。' },
  { value: 'APPROVER', label: '审批人', userName: '赵总', description: '审批合同、驳回合同并给出审批意见。' },
  { value: 'FINANCE', label: '财务', userName: '刘洋', description: '登记付款、维护发票和付款进度。' },
  { value: 'ACCEPTOR', label: '验收人', userName: '李倩', description: '开始履约、登记验收并确认履约结果。' },
  { value: 'ARCHIVIST', label: '档案管理员', userName: '周悦', description: '核验归档资料并完成合同归档。' },
  { value: 'ADMIN', label: '系统管理员', userName: '管理员', description: '拥有演示系统全部操作权限。' }
]

export function useSession() {
  const isLoggedIn = computed(() => Boolean(role.value))
  const currentRole = computed(() => normalizeRole(role.value))
  const currentUser = computed(() => roleOptions.find((item) => item.value === currentRole.value) || null)
  const currentRoleLabel = computed(() => currentUser.value?.label || '未登录')
  const currentUserName = computed(() => currentUser.value?.userName || '')

  function login(nextRole) {
    role.value = nextRole
    localStorage.setItem(ROLE_KEY, nextRole)
  }

  function switchRole(nextRole) {
    login(nextRole)
  }

  function logout() {
    role.value = ''
    localStorage.removeItem(ROLE_KEY)
  }

  return {
    isLoggedIn,
    currentRole,
    currentRoleLabel,
    currentUserName,
    login,
    logout,
    switchRole
  }
}

function normalizeRole(value) {
  if (value === 'NORMAL') {
    return 'PURCHASER'
  }
  if (value === 'ADVANCED') {
    return 'ADMIN'
  }
  return value
}

import { computed, ref } from 'vue'

const ROLE_KEY = 'contract_role'
const role = ref(localStorage.getItem(ROLE_KEY) || '')

export const roleOptions = [
  { value: 'NORMAL', label: '普通用户', userName: '王敏', description: '可创建合同、查看合同、提交前置流程。' },
  { value: 'ADVANCED', label: '高级用户', userName: '赵总', description: '可审批、执行、完成、归档和终止合同。' }
]

export function useSession() {
  const isLoggedIn = computed(() => Boolean(role.value))
  const currentRole = computed(() => role.value)
  const currentUser = computed(() => roleOptions.find((item) => item.value === role.value) || null)
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

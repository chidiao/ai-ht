import { createRouter, createWebHistory } from 'vue-router'
import ContractCreatePage from '../views/ContractCreatePage.vue'
import ContractDetailPage from '../views/ContractDetailPage.vue'
import ContractListPage from '../views/ContractListPage.vue'
import DashboardPage from '../views/DashboardPage.vue'
import LoginPage from '../views/LoginPage.vue'
import TaskCenterPage from '../views/TaskCenterPage.vue'
import { useSession } from '../stores/session'
import { canAccessRoute } from '../utils/routeAccess'

const routes = [
  { path: '/', redirect: '/dashboard' },
  { path: '/login', name: 'login', component: LoginPage, meta: { public: true } },
  { path: '/dashboard', name: 'dashboard', component: DashboardPage },
  { path: '/contracts', name: 'contracts', component: ContractListPage },
  { path: '/approval', name: 'approval', component: TaskCenterPage, meta: { module: 'approval', roles: ['APPROVER', 'ADMIN'] } },
  { path: '/finance', name: 'finance', component: TaskCenterPage, meta: { module: 'finance', roles: ['FINANCE', 'ADMIN'] } },
  { path: '/fulfillment', name: 'fulfillment', component: TaskCenterPage, meta: { module: 'fulfillment', roles: ['ACCEPTOR', 'ADMIN'] } },
  { path: '/archive', name: 'archive', component: TaskCenterPage, meta: { module: 'archive', roles: ['ARCHIVIST', 'ADMIN'] } },
  { path: '/contracts/new', name: 'contract-new', component: ContractCreatePage, meta: { roles: ['PURCHASER', 'ADMIN'] } },
  { path: '/contracts/:id', name: 'contract-detail', component: ContractDetailPage, props: true },
  { path: '/contracts/:id/edit', name: 'contract-edit', component: ContractCreatePage, props: true, meta: { roles: ['PURCHASER', 'ADMIN'] } }
]

export const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const { currentRole, isLoggedIn } = useSession()
  if (!to.meta.public && !isLoggedIn.value) {
    return { name: 'login', replace: true }
  }
  if (to.name === 'login' && isLoggedIn.value) {
    return { name: 'dashboard', replace: true }
  }
  if (isLoggedIn.value && !canAccessRoute(to, currentRole.value)) {
    return { name: 'dashboard', replace: true }
  }
  return true
})

import { createRouter, createWebHistory } from 'vue-router'
import ContractCreatePage from '../views/ContractCreatePage.vue'
import ContractDetailPage from '../views/ContractDetailPage.vue'
import ContractListPage from '../views/ContractListPage.vue'
import DashboardPage from '../views/DashboardPage.vue'
import LoginPage from '../views/LoginPage.vue'
import { useSession } from '../stores/session'

const routes = [
  { path: '/', redirect: '/dashboard' },
  { path: '/login', name: 'login', component: LoginPage, meta: { public: true } },
  { path: '/dashboard', name: 'dashboard', component: DashboardPage },
  { path: '/contracts', name: 'contracts', component: ContractListPage },
  { path: '/contracts/new', name: 'contract-new', component: ContractCreatePage },
  { path: '/contracts/:id', name: 'contract-detail', component: ContractDetailPage, props: true },
  { path: '/contracts/:id/edit', name: 'contract-edit', component: ContractCreatePage, props: true }
]

export const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const { isLoggedIn } = useSession()
  if (!to.meta.public && !isLoggedIn.value) {
    return { name: 'login' }
  }
  if (to.name === 'login' && isLoggedIn.value) {
    return { name: 'dashboard' }
  }
  return true
})

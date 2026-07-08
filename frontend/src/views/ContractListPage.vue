<template>
  <AppLayout>
      <PageHeader
        title="合同列表"
        eyebrow="Contracts"
        :breadcrumbs="[{ label: '首页', to: '/contracts' }, { label: '合同列表' }]"
      >
        <el-button type="primary" :icon="Plus" :disabled="!canCreateContract(currentRole)" @click="$router.push('/contracts/new')">
          新建合同
        </el-button>
      </PageHeader>

      <StatsOverview :stats="stats" :active-key="activeStatKey" @select="applyQuickFilter" />
      <ContractFilters v-model="filters" @search="loadData" @reset="resetFilters" />
      <ContractTable
        :contracts="contracts"
        :loading="loading"
        @detail="(row) => $router.push(`/contracts/${row.id}`)"
        @edit="(row) => $router.push(`/contracts/${row.id}/edit`)"
      />
  </AppLayout>
</template>

<script setup>
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import AppLayout from '../components/AppLayout.vue'
import ContractFilters from '../components/ContractFilters.vue'
import ContractTable from '../components/ContractTable.vue'
import PageHeader from '../components/PageHeader.vue'
import StatsOverview from '../components/StatsOverview.vue'
import { fetchContracts, fetchContractStats } from '../api/contracts'
import { useSession } from '../stores/session'
import { canCreateContract } from '../utils/permissions'

const { currentRole } = useSession()
const contracts = ref([])
const stats = ref({ total: 0, pendingApproval: 0, executing: 0, pendingPayment: 0, expiringSoon: 0 })
const loading = ref(false)
const filters = ref({
  keyword: '',
  supplierName: '',
  owner: '',
  status: '',
  statuses: [],
  paymentStatus: '',
  dueRange: [],
  expiringSoon: false,
  quickFilter: ''
})

const activeStatKey = computed(() => {
  const statuses = filters.value.statuses || []
  if (statuses.length === 1 && statuses[0] === 'PENDING_APPROVAL') {
    return 'pendingApproval'
  }
  if (statuses.length === 1 && statuses[0] === 'EXECUTING') {
    return 'executing'
  }
  if (filters.value.quickFilter === 'pendingPayment') {
    return 'pendingPayment'
  }
  if (filters.value.expiringSoon) {
    return 'expiringSoon'
  }
  return 'total'
})

async function loadData() {
  loading.value = true
  try {
    const params = {
      keyword: filters.value.keyword || undefined,
      supplierName: filters.value.supplierName || undefined,
      owner: filters.value.owner || undefined,
      status: filters.value.status || undefined,
      statuses: filters.value.statuses?.length ? filters.value.statuses : undefined,
      paymentStatus: filters.value.paymentStatus || undefined,
      dueStart: filters.value.dueRange?.[0],
      dueEnd: filters.value.dueRange?.[1],
      expiringSoon: filters.value.expiringSoon || undefined,
      quickFilter: filters.value.quickFilter || undefined
    }
    const [list, statData] = await Promise.all([fetchContracts(params), fetchContractStats()])
    contracts.value = applyClientQuickFilter(list)
    stats.value = statData
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '数据加载失败')
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  filters.value = {
    keyword: '',
    supplierName: '',
    owner: '',
    status: '',
    statuses: [],
    paymentStatus: '',
    dueRange: [],
    expiringSoon: false,
    quickFilter: ''
  }
  loadData()
}

function applyQuickFilter(key) {
  filters.value.quickFilter = ''
  filters.value.status = ''
  filters.value.statuses = []
  filters.value.paymentStatus = ''
  filters.value.expiringSoon = false

  if (key === 'pendingApproval') {
    filters.value.statuses = ['PENDING_APPROVAL']
  } else if (key === 'pendingPayment') {
    filters.value.paymentStatus = 'UNPAID'
    filters.value.quickFilter = 'pendingPayment'
  } else if (key === 'expiringSoon') {
    filters.value.expiringSoon = true
  } else if (key === 'executing') {
    filters.value.statuses = ['EXECUTING']
  }

  loadData()
}

function applyClientQuickFilter(list) {
  switch (filters.value.quickFilter) {
    case 'pendingApproval':
      return list.filter((item) => item.status === 'PENDING_APPROVAL')
    case 'executing':
      return list.filter((item) => item.status === 'EXECUTING')
    case 'pendingPayment':
      return list.filter((item) => item.paymentStatus !== 'PAID' && item.status !== 'ARCHIVED')
    case 'expiringSoon':
      return list.filter((item) => {
        const today = new Date()
        const expiry = new Date(`${item.expiryDate}T00:00:00`)
        const diff = (expiry.getTime() - today.getTime()) / 86400000
        return diff >= 0 && diff <= 30
      })
    default:
      return list
  }
}

loadData()
</script>

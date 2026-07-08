<template>
  <AppLayout>
      <PageHeader
        title="合同列表"
        eyebrow="Contracts"
        :breadcrumbs="[{ label: '首页', to: '/dashboard' }, { label: '合同列表' }]"
      >
        <el-button type="primary" :icon="Plus" :disabled="!canCreateContract(currentRole)" @click="$router.push('/contracts/new')">
          新建合同
        </el-button>
      </PageHeader>

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
import { ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import AppLayout from '../components/AppLayout.vue'
import ContractFilters from '../components/ContractFilters.vue'
import ContractTable from '../components/ContractTable.vue'
import PageHeader from '../components/PageHeader.vue'
import { fetchContracts } from '../api/contracts'
import { useSession } from '../stores/session'
import { canCreateContract } from '../utils/permissions'

const { currentRole } = useSession()
const route = useRoute()
const contracts = ref([])
const loading = ref(false)
const filters = ref({
  keyword: '',
  supplierName: '',
  owner: '',
  department: '',
  category: '',
  status: '',
  statuses: [],
  paymentStatus: '',
  archiveStatus: '',
  amountMin: undefined,
  amountMax: undefined,
  signRange: [],
  dueRange: [],
  expiringSoon: false,
  quickFilter: ''
})

async function loadData() {
  loading.value = true
  try {
    const params = {
      keyword: filters.value.keyword || undefined,
      supplierName: filters.value.supplierName || undefined,
      owner: filters.value.owner || undefined,
      department: filters.value.department || undefined,
      category: filters.value.category || undefined,
      status: filters.value.status || undefined,
      statuses: filters.value.statuses?.length ? filters.value.statuses : undefined,
      paymentStatus: filters.value.paymentStatus || undefined,
      archiveStatus: filters.value.archiveStatus || undefined,
      amountMin: filters.value.amountMin ?? undefined,
      amountMax: filters.value.amountMax ?? undefined,
      signStart: filters.value.signRange?.[0],
      signEnd: filters.value.signRange?.[1],
      dueStart: filters.value.dueRange?.[0],
      dueEnd: filters.value.dueRange?.[1],
      expiringSoon: filters.value.expiringSoon || undefined,
      quickFilter: filters.value.quickFilter || undefined
    }
    const list = await fetchContracts(params)
    contracts.value = applyClientQuickFilter(list)
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
    department: '',
    category: '',
    status: '',
    statuses: [],
    paymentStatus: '',
    archiveStatus: '',
    amountMin: undefined,
    amountMax: undefined,
    signRange: [],
    dueRange: [],
    expiringSoon: false,
    quickFilter: ''
  }
  loadData()
}

function applyQuickFilter(key, shouldLoad = true) {
  filters.value.quickFilter = ''
  filters.value.status = ''
  filters.value.statuses = []
  filters.value.paymentStatus = ''
  filters.value.archiveStatus = ''
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

  if (shouldLoad) {
    loadData()
  }
}

function applyRouteQuery() {
  filters.value.statuses = []
  filters.value.paymentStatus = ''
  filters.value.quickFilter = ''
  filters.value.expiringSoon = false

  if (route.query.status) {
    filters.value.statuses = [String(route.query.status)]
  }
  if (route.query.quick) {
    applyQuickFilter(String(route.query.quick), false)
  }
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

watch(
  () => route.query,
  () => {
    applyRouteQuery()
    loadData()
  },
  { immediate: true }
)
</script>

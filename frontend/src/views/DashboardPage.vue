<template>
  <AppLayout>
    <PageHeader
      title="合同工作台"
      eyebrow="Dashboard"
      :breadcrumbs="[{ label: '首页' }]"
    />

    <section class="dashboard-welcome">
      <div>
        <p class="eyebrow">Welcome</p>
        <h2>{{ greetingText }}</h2>
        <span>{{ roleProfile.description }}</span>
      </div>
      <div class="role-focus-grid">
        <button v-for="item in roleFocusItems" :key="item.label" type="button" @click="router.push(item.to)">
          <span>{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
          <small>{{ item.hint }}</small>
        </button>
      </div>
    </section>

    <section class="dashboard-kpis">
      <button v-for="item in kpiItems" :key="item.key" type="button" @click="openStat(item.key)">
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
        <small>{{ item.hint }}</small>
      </button>
    </section>

    <section class="dashboard-grid">
      <div class="content-panel">
        <div class="section-title">
          <h2>待办提醒</h2>
          <span>按处理优先级排序</span>
        </div>
        <div class="todo-list">
          <button v-for="item in todoItems" :key="item.key" class="todo-row" type="button" @click="openStat(item.key)">
            <span class="todo-row__mark" :class="`todo-row__mark--${item.tone}`"></span>
            <span>
              <strong>{{ item.label }}</strong>
              <small>{{ item.hint }}</small>
            </span>
            <em>{{ item.value }}</em>
          </button>
        </div>
      </div>

      <div class="content-panel">
        <div class="section-title">
          <h2>状态分布</h2>
          <span>{{ contracts.length }} 份合同</span>
        </div>
        <div class="status-bars">
          <button v-for="item in statusDistribution" :key="item.status" class="status-bar-row" type="button" @click="openStatus(item.status)">
            <span>{{ statusLabel(item.status) }}</span>
            <div class="status-bar-track">
              <i :style="{ width: `${item.percent}%` }"></i>
            </div>
            <strong>{{ item.count }}</strong>
          </button>
        </div>
      </div>
    </section>

    <section class="dashboard-grid dashboard-grid--wide">
      <div class="content-panel">
        <div class="section-title">
          <h2>近期需跟进</h2>
          <span>临期、待付款、执行中</span>
        </div>
        <el-table :data="followUpContracts" border>
          <el-table-column prop="contractNo" label="合同编号" width="140" />
          <el-table-column prop="name" label="合同名称" min-width="180" show-overflow-tooltip />
          <el-table-column prop="supplierName" label="供应商" min-width="180" show-overflow-tooltip />
          <el-table-column prop="status" label="状态" width="120">
            <template #default="{ row }">
              <el-tag :type="statusType(row.status)">{{ statusLabel(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="paymentStatus" label="付款" width="110">
            <template #default="{ row }">{{ paymentLabel(row.paymentStatus) }}</template>
          </el-table-column>
          <el-table-column prop="expiryDate" label="到期日期" width="130" />
          <el-table-column label="操作" width="96">
            <template #default="{ row }">
              <el-button link type="primary" @click="$router.push(`/contracts/${row.id}`)">详情</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="content-panel">
        <div class="section-title">
          <h2>金额概览</h2>
          <span>合同金额与付款进度</span>
        </div>
        <div class="amount-overview">
          <div>
            <span>合同总金额</span>
            <strong>{{ money(amountSummary.totalAmount) }}</strong>
          </div>
          <div>
            <span>累计已付</span>
            <strong>{{ money(amountSummary.paidAmount) }}</strong>
          </div>
          <div>
            <span>待支付</span>
            <strong>{{ money(amountSummary.pendingAmount) }}</strong>
          </div>
        </div>
        <div class="payment-progress">
          <span>付款完成度</span>
          <strong>{{ amountSummary.paymentPercent }}%</strong>
          <el-progress :percentage="amountSummary.paymentPercent" :stroke-width="12" />
        </div>
      </div>
    </section>
  </AppLayout>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import AppLayout from '../components/AppLayout.vue'
import PageHeader from '../components/PageHeader.vue'
import { fetchContracts, fetchContractStats } from '../api/contracts'
import { money, paymentLabel, statusLabel, statusType } from '../utils/contractFormat'
import { useSession } from '../stores/session'

const router = useRouter()
const { currentRole, currentRoleLabel, currentUserName } = useSession()
const contracts = ref([])
const stats = ref({ total: 0, pendingApproval: 0, executing: 0, pendingPayment: 0, expiringSoon: 0 })

const roleProfiles = {
  PURCHASER: {
    description: '你主要负责合同拟稿、供应商确认、提交审批、取消流程和发起终止申请。',
    focuses: [
      { label: '我的合同入口', value: '合同管理', hint: '新建、查询和维护合同', to: '/contracts' },
      { label: '待审批', value: () => stats.value.pendingApproval, hint: '关注已提交审批的合同', to: '/approval' }
    ]
  },
  APPROVER: {
    description: '你主要负责合同审批、审批驳回，以及生效后终止申请的确认。',
    focuses: [
      { label: '合同审批', value: () => countByStatus('PENDING_APPROVAL'), hint: '等待审批意见', to: '/approval' },
      { label: '终止审批', value: () => countByStatus('TERMINATION_PENDING'), hint: '确认终止和结算意见', to: '/approval' }
    ]
  },
  FINANCE: {
    description: '你主要负责查看付款计划、登记付款、维护发票和付款进度。',
    focuses: [
      { label: '待付款', value: () => stats.value.pendingPayment, hint: '未结清合同', to: '/finance' },
      { label: '部分付款', value: () => contracts.value.filter((item) => item.paymentStatus === 'PARTIAL').length, hint: '仍有后续款项', to: '/finance' }
    ]
  },
  ACCEPTOR: {
    description: '你主要负责启动履约、跟进交付、登记验收和确认合同完成。',
    focuses: [
      { label: '待启动', value: () => countByStatus('ACTIVE'), hint: '可开始执行', to: '/fulfillment' },
      { label: '执行中', value: () => stats.value.executing, hint: '跟进验收和完成', to: '/fulfillment' }
    ]
  },
  ARCHIVIST: {
    description: '你主要负责核验合同正文、付款验收记录和完成归档。',
    focuses: [
      { label: '待归档', value: () => countByStatus('COMPLETED'), hint: '资料齐全后归档', to: '/archive' },
      { label: '已归档', value: () => countByStatus('ARCHIVED'), hint: '已完成档案沉淀', to: '/contracts?status=ARCHIVED' }
    ]
  },
  ADMIN: {
    description: '你可以查看全部业务入口，用于演示、排查和跨角色处理。',
    focuses: [
      { label: '审批事项', value: () => countByStatus('PENDING_APPROVAL') + countByStatus('TERMINATION_PENDING'), hint: '合同审批和终止审批', to: '/approval' },
      { label: '付款事项', value: () => stats.value.pendingPayment, hint: '财务待处理', to: '/finance' }
    ]
  }
}

const roleProfile = computed(() => roleProfiles[currentRole.value] || roleProfiles.ADMIN)
const greetingText = computed(() => `${currentUserName.value || '你好'}，${currentRoleLabel.value}工作台`)
const roleFocusItems = computed(() => roleProfile.value.focuses.map((item) => ({
  ...item,
  value: typeof item.value === 'function' ? item.value() : item.value
})))

const kpiItems = computed(() => [
  { key: 'total', label: '合同总数', value: stats.value.total, hint: '全部合同' },
  { key: 'executing', label: '履约中', value: stats.value.executing, hint: '交付与验收' },
  { key: 'pendingPayment', label: '待付款', value: stats.value.pendingPayment, hint: '未结清' },
  { key: 'expiringSoon', label: '临期', value: stats.value.expiringSoon, hint: '30 天内到期' }
])

const todoItems = computed(() => [
  { key: 'pendingApproval', label: '待审批合同', value: stats.value.pendingApproval, hint: '需要负责人确认审批意见', tone: 'amber' },
  { key: 'pendingPayment', label: '待付款合同', value: stats.value.pendingPayment, hint: '已生效或履约中但付款未结清', tone: 'rose' },
  { key: 'expiringSoon', label: '临期合同', value: stats.value.expiringSoon, hint: '30 天内到期，建议确认履约和归档', tone: 'emerald' },
  { key: 'executing', label: '履约中合同', value: stats.value.executing, hint: '关注交付、验收和付款进度', tone: 'cyan' }
])

const statusDistribution = computed(() => {
  const total = contracts.value.length || 1
  const counts = contracts.value.reduce((map, item) => {
    map[item.status] = (map[item.status] || 0) + 1
    return map
  }, {})
  return Object.entries(counts)
    .map(([status, count]) => ({ status, count, percent: Math.round((count / total) * 100) }))
    .sort((a, b) => b.count - a.count)
})

const followUpContracts = computed(() => {
  const today = new Date()
  return [...contracts.value]
    .filter((item) => item.status === 'PENDING_APPROVAL'
      || item.status === 'EXECUTING'
      || (item.paymentStatus !== 'PAID' && item.status !== 'ARCHIVED')
      || isWithinDays(item.expiryDate, today, 30))
    .sort((a, b) => new Date(`${a.expiryDate}T00:00:00`) - new Date(`${b.expiryDate}T00:00:00`))
    .slice(0, 6)
})

const amountSummary = computed(() => {
  const totalAmount = contracts.value.reduce((sum, item) => sum + Number(item.amount || 0), 0)
  const paidAmount = contracts.value.reduce((sum, item) => sum + Number(item.paidAmount || 0), 0)
  const pendingAmount = Math.max(totalAmount - paidAmount, 0)
  const paymentPercent = totalAmount ? Math.round((paidAmount / totalAmount) * 100) : 0
  return { totalAmount, paidAmount, pendingAmount, paymentPercent }
})

function countByStatus(status) {
  return contracts.value.filter((item) => item.status === status).length
}

function openStat(key) {
  router.push({ path: '/contracts', query: { quick: key } })
}

function openStatus(status) {
  router.push({ path: '/contracts', query: { status } })
}

function isWithinDays(dateValue, today, days) {
  if (!dateValue) {
    return false
  }
  const expiry = new Date(`${dateValue}T00:00:00`)
  const diff = (expiry.getTime() - today.getTime()) / 86400000
  return diff >= 0 && diff <= days
}

async function loadDashboard() {
  try {
    const [list, statData] = await Promise.all([fetchContracts(), fetchContractStats()])
    contracts.value = list
    stats.value = statData
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '工作台数据加载失败')
  }
}

loadDashboard()
</script>

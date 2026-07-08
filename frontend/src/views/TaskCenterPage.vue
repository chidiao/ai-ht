<template>
  <AppLayout>
    <PageHeader
      :title="moduleConfig.title"
      :eyebrow="moduleConfig.eyebrow"
      :breadcrumbs="[{ label: '首页', to: '/dashboard' }, { label: moduleConfig.title }]"
    >
      <el-button @click="loadData">刷新</el-button>
    </PageHeader>

    <section class="task-summary">
      <article v-for="item in summaryCards" :key="item.label" class="task-summary-card">
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
        <small>{{ item.hint }}</small>
      </article>
    </section>

    <section class="content-panel">
      <div class="section-title">
        <h2>{{ moduleConfig.tableTitle }}</h2>
        <span>{{ moduleConfig.description }}</span>
      </div>
      <el-table v-loading="loading" :data="contracts" border height="540">
        <el-table-column prop="contractNo" label="合同编号" width="140" fixed />
        <el-table-column prop="name" label="合同名称" min-width="210" show-overflow-tooltip />
        <el-table-column prop="supplierName" label="供应商" min-width="190" show-overflow-tooltip />
        <el-table-column prop="amount" label="金额" width="130">
          <template #default="{ row }">{{ money(row.amount) }}</template>
        </el-table-column>
        <el-table-column prop="paidAmount" label="已付" width="130">
          <template #default="{ row }">{{ money(row.paidAmount) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="130">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="paymentStatus" label="付款" width="112">
          <template #default="{ row }">
            <el-tag :type="paymentType(row.paymentStatus)" effect="plain">{{ paymentLabel(row.paymentStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="owner" label="负责人" width="100" />
        <el-table-column prop="expiryDate" label="到期日期" width="130">
          <template #default="{ row }">
            <span :class="{ danger: isExpiring(row.expiryDate) }">{{ row.expiryDate }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <div class="row-actions task-row-actions">
              <el-button link type="primary" @click="$router.push(`/contracts/${row.id}`)">详情</el-button>
              <el-button
                v-for="action in rowActions(row)"
                :key="action.value"
                link
                :type="dangerActions.includes(action.value) ? 'danger' : 'primary'"
                @click="openAction(row, action)"
              >
                {{ action.shortLabel || action.label }}
              </el-button>
            </div>
          </template>
        </el-table-column>
        <template #empty>
          <div class="table-empty-state">
            <el-icon><FolderOpened /></el-icon>
            <strong>{{ moduleConfig.emptyText }}</strong>
            <span>{{ moduleConfig.emptyHint }}</span>
          </div>
        </template>
      </el-table>
    </section>

    <ContractActionDialog
      v-model="actionVisible"
      :contract="selectedDetail.contract"
      :action="currentAction"
      :saving="saving"
      :operator-name="currentUserName"
      :payment-plans="selectedDetail.paymentPlans"
      @submit="submitAction"
    />
  </AppLayout>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { FolderOpened } from '@element-plus/icons-vue'
import AppLayout from '../components/AppLayout.vue'
import ContractActionDialog from '../components/ContractActionDialog.vue'
import PageHeader from '../components/PageHeader.vue'
import { addContractAcceptance, fetchContractDetail, fetchContractPage, runContractAction } from '../api/contracts'
import { useSession } from '../stores/session'
import { canRunAction } from '../utils/permissions'
import { availableActions, isExpiring, money, paymentLabel, paymentType, statusLabel, statusType } from '../utils/contractFormat'

const route = useRoute()
const { currentRole, currentUserName } = useSession()
const contracts = ref([])
const loading = ref(false)
const saving = ref(false)
const actionVisible = ref(false)
const currentAction = ref(null)
const selectedDetail = reactive({ contract: null, paymentPlans: [], acceptanceRecords: [] })
const dangerActions = ['REJECT', 'TERMINATE']

const modules = {
  approval: {
    title: '审批中心',
    eyebrow: 'Approval Center',
    tableTitle: '待审批事项',
    description: '集中处理合同提交后的内部审批。',
    emptyText: '当前没有待审批事项',
    emptyHint: '提交审批后的合同会集中出现在这里。',
    params: { statuses: ['PENDING_APPROVAL'] },
    actions: ['APPROVE', 'REJECT'],
    summary: [
      { label: '合同审批', match: (item) => item.status === 'PENDING_APPROVAL', hint: '等待审批通过或驳回' }
    ]
  },
  finance: {
    title: '付款管理',
    eyebrow: 'Payment',
    tableTitle: '履约付款任务',
    description: '付款属于履约阶段的财务事项，这里只展示未付清的合同。',
    emptyText: '当前没有待付款合同',
    emptyHint: '已生效或执行中的未结清合同会进入付款管理。',
    params: { statuses: ['ACTIVE', 'EXECUTING'], quickFilter: 'pendingPayment' },
    actions: ['REGISTER_PAYMENT'],
    summary: [
      { label: '待付款', match: (item) => item.paymentStatus !== 'PAID', hint: '可登记付款' },
      { label: '部分付款', match: (item) => item.paymentStatus === 'PARTIAL', hint: '仍有尾款待处理' }
    ]
  },
  fulfillment: {
    title: '履约管理',
    eyebrow: 'Fulfillment',
    tableTitle: '履约任务',
    description: '集中跟进合同履约中的付款、交付和验收；合同完成由管理员最终确认。',
    emptyText: '当前没有履约任务',
    emptyHint: '已生效和执行中的合同会进入履约管理工作台。',
    params: { statuses: ['ACTIVE', 'EXECUTING'] },
    actions: ['START_EXECUTION', 'REGISTER_PAYMENT', 'REGISTER_ACCEPTANCE', 'COMPLETE', 'TERMINATE'],
    summary: [
      { label: '待启动', match: (item) => item.status === 'ACTIVE', hint: '可开始执行' },
      { label: '执行中', match: (item) => item.status === 'EXECUTING', hint: '跟进交付验收' }
    ]
  },
  archive: {
    title: '档案管理',
    eyebrow: 'Archive',
    tableTitle: '待归档合同',
    description: '核验合同正文、验收和付款完成情况后归档。',
    emptyText: '当前没有待归档合同',
    emptyHint: '合同完成后会进入档案管理，等待资料核验和归档。',
    params: { statuses: ['COMPLETED'] },
    actions: ['ARCHIVE'],
    summary: [
      { label: '待归档', match: (item) => item.status === 'COMPLETED', hint: '可核验资料并归档' },
      { label: '已付款', match: (item) => item.paymentStatus === 'PAID', hint: '付款已闭环' }
    ]
  }
}

const moduleKey = computed(() => route.meta.module || 'approval')
const moduleConfig = computed(() => modules[moduleKey.value])
const summaryCards = computed(() => moduleConfig.value.summary.map((item) => ({
  label: item.label,
  value: contracts.value.filter(item.match).length,
  hint: item.hint
})))

async function loadData() {
  loading.value = true
  try {
    const data = await fetchContractPage({ ...moduleConfig.value.params, page: 0, size: 100 })
    contracts.value = data.content || []
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '任务加载失败')
  } finally {
    loading.value = false
  }
}

function rowActions(row) {
  return availableActions(row)
    .filter((action) => moduleConfig.value.actions.includes(action.value))
    .filter((action) => canRunAction(currentRole.value, action.value))
    .filter((action) => action.value !== 'REGISTER_ACCEPTANCE' || row.status === 'EXECUTING')
    .filter((action) => action.value !== 'COMPLETE' || row.paymentStatus === 'PAID')
    .map((action) => ({ ...action, shortLabel: shortActionLabel(action.value, action.label) }))
}

function shortActionLabel(value, fallback) {
  return {
    REGISTER_PAYMENT: '付款',
    REGISTER_ACCEPTANCE: '验收',
    START_EXECUTION: '开始执行',
    COMPLETE: '完成',
    TERMINATE: '终止'
  }[value] || fallback
}

async function openAction(row, action) {
  try {
    const detail = await fetchContractDetail(row.id)
    selectedDetail.contract = detail.contract
    selectedDetail.paymentPlans = detail.paymentPlans || []
    selectedDetail.acceptanceRecords = detail.acceptanceRecords || []
    currentAction.value = action
    actionVisible.value = true
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '详情加载失败')
  }
}

async function submitAction(actionForm) {
  saving.value = true
  try {
    if (currentAction.value.value === 'REGISTER_ACCEPTANCE') {
      await addContractAcceptance(selectedDetail.contract.id, {
        deliveryDate: actionForm.deliveryDate,
        acceptanceDate: actionForm.acceptanceDate,
        acceptanceResult: actionForm.acceptanceResult,
        acceptanceNote: actionForm.comment,
        exceptionNote: actionForm.exceptionNote,
        accepter: actionForm.operator
      })
      ElMessage.success('验收记录已登记')
    } else {
      await runContractAction(selectedDetail.contract.id, {
        action: currentAction.value.value,
        operator: actionForm.operator,
        comment: actionForm.comment,
        paidAmount: currentAction.value.value === 'REGISTER_PAYMENT' ? actionForm.paidAmount : undefined,
        paymentPlanId: actionForm.paymentPlanId,
        paymentStage: actionForm.paymentStage,
        paymentDate: actionForm.paymentDate,
        invoiceNo: actionForm.invoiceNo
      })
      ElMessage.success('任务已处理')
    }
    actionVisible.value = false
    await loadData()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '操作失败')
  } finally {
    saving.value = false
  }
}

watch(moduleKey, loadData, { immediate: true })
</script>

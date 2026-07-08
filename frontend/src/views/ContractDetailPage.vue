<template>
  <AppLayout>
      <PageHeader
        :title="detail.contract?.name || '合同详情'"
        eyebrow="Contract Detail"
        back-to="/contracts"
        :breadcrumbs="[
          { label: '首页', to: '/contracts' },
          { label: '合同列表', to: '/contracts' },
          { label: detail.contract?.contractNo || '合同详情' }
        ]"
      >
          <el-button v-if="canEditContract(currentRole, detail.contract)" type="primary" @click="$router.push(`/contracts/${id}/edit`)">
            编辑合同
          </el-button>
      </PageHeader>

      <template v-if="detail.contract">
        <WorkflowProgress :status="detail.contract.status" />

        <section class="content-panel">
          <div class="section-title">
            <h2>可执行动作</h2>
            <span>{{ currentRoleLabel }}</span>
          </div>
          <div class="action-list" v-if="permittedActions.length">
            <el-button
              v-for="action in permittedActions"
              :key="action.value"
              :type="action.value === 'REJECT' || action.value === 'TERMINATE' ? 'danger' : 'primary'"
              plain
              @click="openAction(action)"
            >
              {{ action.label }}
            </el-button>
          </div>
          <el-empty v-else description="当前身份或合同状态下暂无可执行动作" :image-size="72" />
        </section>

        <section class="content-panel">
          <div class="section-title">
            <h2>合同信息</h2>
            <el-tag :type="statusType(detail.contract.status)">{{ statusLabel(detail.contract.status) }}</el-tag>
          </div>
          <el-descriptions :column="3" border>
            <el-descriptions-item label="合同编号">{{ detail.contract.contractNo }}</el-descriptions-item>
            <el-descriptions-item label="供应商">{{ detail.contract.supplierName }}</el-descriptions-item>
            <el-descriptions-item label="负责人">{{ detail.contract.owner }}</el-descriptions-item>
            <el-descriptions-item label="部门">{{ detail.contract.department || '-' }}</el-descriptions-item>
            <el-descriptions-item label="品类">{{ detail.contract.category || '-' }}</el-descriptions-item>
            <el-descriptions-item label="付款状态">{{ paymentLabel(detail.contract.paymentStatus) }}</el-descriptions-item>
            <el-descriptions-item label="合同金额">{{ money(detail.contract.amount) }}</el-descriptions-item>
            <el-descriptions-item label="已付金额">{{ money(detail.contract.paidAmount) }}</el-descriptions-item>
            <el-descriptions-item label="到期日期">{{ detail.contract.expiryDate }}</el-descriptions-item>
            <el-descriptions-item label="签订日期">{{ detail.contract.signDate || '-' }}</el-descriptions-item>
            <el-descriptions-item label="生效日期">{{ detail.contract.effectiveDate || '-' }}</el-descriptions-item>
            <el-descriptions-item label="备注">{{ detail.contract.remark || '-' }}</el-descriptions-item>
          </el-descriptions>
        </section>

        <section class="content-panel">
          <div class="section-title">
            <h2>流程记录</h2>
          </div>
          <el-timeline>
            <el-timeline-item v-for="log in detail.logs" :key="log.id" :timestamp="formatDateTime(log.operatedAt)">
              <strong>{{ actionLabel(log.action) }}</strong>
              <p>{{ statusLabel(log.fromStatus) || '开始' }} → {{ statusLabel(log.toStatus) }}</p>
              <p>{{ log.operator }}：{{ log.comment || '无备注' }}</p>
            </el-timeline-item>
          </el-timeline>
        </section>
      </template>

    <ContractActionDialog
      v-model="actionVisible"
      :contract="detail.contract"
      :action="currentAction"
      :saving="saving"
      :operator-name="currentUserName"
      @submit="submitAction"
    />
  </AppLayout>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import AppLayout from '../components/AppLayout.vue'
import ContractActionDialog from '../components/ContractActionDialog.vue'
import PageHeader from '../components/PageHeader.vue'
import WorkflowProgress from '../components/WorkflowProgress.vue'
import { fetchContractDetail, runContractAction } from '../api/contracts'
import { useSession } from '../stores/session'
import { canEditContract, canRunAction } from '../utils/permissions'
import {
  actionLabel,
  availableActions,
  formatDateTime,
  money,
  paymentLabel,
  statusLabel,
  statusType
} from '../utils/contractFormat'

const props = defineProps({
  id: {
    type: String,
    required: true
  }
})

const { currentRole, currentRoleLabel, currentUserName } = useSession()
const detail = reactive({ contract: null, logs: [] })
const actionVisible = ref(false)
const currentAction = ref(null)
const saving = ref(false)

const permittedActions = computed(() => {
  if (!detail.contract) {
    return []
  }
  return availableActions(detail.contract).filter((action) => canRunAction(currentRole.value, action.value))
})

async function loadDetail() {
  try {
    const data = await fetchContractDetail(props.id)
    detail.contract = data.contract
    detail.logs = data.logs
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '详情加载失败')
  }
}

function openAction(action) {
  currentAction.value = action
  actionVisible.value = true
}

async function submitAction(actionForm) {
  saving.value = true
  try {
    await runContractAction(props.id, {
      action: currentAction.value.value,
      operator: actionForm.operator,
      comment: actionForm.comment,
      paidAmount: currentAction.value.value === 'REGISTER_PAYMENT' ? actionForm.paidAmount : undefined
    })
    ElMessage.success('流程已更新')
    actionVisible.value = false
    await loadDetail()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '流程操作失败')
  } finally {
    saving.value = false
  }
}

loadDetail()
</script>

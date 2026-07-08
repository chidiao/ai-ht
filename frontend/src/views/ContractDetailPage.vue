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
          <div class="status-strip">
            <div>
              <span>主流程</span>
              <strong>{{ statusLabel(detail.contract.status) }}</strong>
            </div>
            <div>
              <span>签署</span>
              <strong>{{ signingLabel(detail.contract.signingStatus) }}</strong>
            </div>
            <div>
              <span>审批</span>
              <strong>{{ approvalLabel(detail.contract.approvalStatus) }}</strong>
            </div>
            <div>
              <span>付款</span>
              <strong>{{ paymentLabel(detail.contract.paymentStatus) }}</strong>
            </div>
            <div>
              <span>归档</span>
              <strong>{{ archiveLabel(detail.contract.archiveStatus) }}</strong>
            </div>
          </div>
          <h3>基础信息</h3>
          <el-descriptions :column="3" border>
            <el-descriptions-item label="合同编号">{{ detail.contract.contractNo }}</el-descriptions-item>
            <el-descriptions-item label="负责人">{{ detail.contract.owner }}</el-descriptions-item>
            <el-descriptions-item label="部门">{{ detail.contract.department || '-' }}</el-descriptions-item>
            <el-descriptions-item label="品类">{{ detail.contract.category || '-' }}</el-descriptions-item>
            <el-descriptions-item label="采购方式">{{ detail.contract.purchaseMethod || '-' }}</el-descriptions-item>
            <el-descriptions-item label="合同来源">{{ detail.contract.contractSource || '-' }}</el-descriptions-item>
            <el-descriptions-item label="采购内容" :span="2">{{ detail.contract.purchaseContent || '-' }}</el-descriptions-item>
            <el-descriptions-item label="采购数量">{{ detail.contract.purchaseQuantity || '-' }}</el-descriptions-item>
          </el-descriptions>

          <h3>供应商信息</h3>
          <el-descriptions :column="3" border>
            <el-descriptions-item label="供应商">{{ detail.contract.supplierName }}</el-descriptions-item>
            <el-descriptions-item label="联系人">{{ detail.contract.supplierContact || '-' }}</el-descriptions-item>
            <el-descriptions-item label="联系电话">{{ detail.contract.supplierPhone || '-' }}</el-descriptions-item>
          </el-descriptions>

          <h3>金额与付款</h3>
          <el-descriptions :column="3" border>
            <el-descriptions-item label="付款状态">{{ paymentLabel(detail.contract.paymentStatus) }}</el-descriptions-item>
            <el-descriptions-item label="合同金额">{{ money(detail.contract.amount) }}</el-descriptions-item>
            <el-descriptions-item label="已付金额">{{ money(detail.contract.paidAmount) }}</el-descriptions-item>
            <el-descriptions-item label="付款方式" :span="3">{{ detail.contract.paymentMethod || '-' }}</el-descriptions-item>
          </el-descriptions>

          <h3>签署与履约</h3>
          <el-descriptions :column="3" border>
            <el-descriptions-item label="签署方式">{{ detail.contract.signingMethod || '-' }}</el-descriptions-item>
            <el-descriptions-item label="交付地点">{{ detail.contract.deliveryLocation || '-' }}</el-descriptions-item>
            <el-descriptions-item label="到期日期">{{ detail.contract.expiryDate }}</el-descriptions-item>
            <el-descriptions-item label="签订日期">{{ detail.contract.signDate || '-' }}</el-descriptions-item>
            <el-descriptions-item label="生效日期">{{ detail.contract.effectiveDate || '-' }}</el-descriptions-item>
            <el-descriptions-item label="验收标准" :span="3">{{ detail.contract.acceptanceCriteria || '-' }}</el-descriptions-item>
            <el-descriptions-item label="备注" :span="3">{{ detail.contract.remark || '-' }}</el-descriptions-item>
          </el-descriptions>
        </section>

        <section class="content-panel">
          <div class="section-title">
            <h2>付款记录</h2>
            <span>{{ detail.paymentRecords.length }} 条记录</span>
          </div>
          <el-table :data="detail.paymentRecords" border>
            <el-table-column prop="paymentStage" label="付款阶段" width="120" />
            <el-table-column prop="amount" label="本次付款" width="140">
              <template #default="{ row }">{{ money(row.amount) }}</template>
            </el-table-column>
            <el-table-column prop="paidAmountAfterPayment" label="付款后累计" width="140">
              <template #default="{ row }">{{ money(row.paidAmountAfterPayment) }}</template>
            </el-table-column>
            <el-table-column prop="paymentDate" label="付款日期" width="130" />
            <el-table-column prop="invoiceNo" label="发票编号" width="150" />
            <el-table-column prop="operator" label="操作人" width="110" />
            <el-table-column prop="note" label="付款说明" min-width="220" show-overflow-tooltip />
          </el-table>
        </section>

        <section class="content-panel">
          <div class="section-title">
            <h2>验收记录</h2>
            <span>{{ detail.acceptanceRecords.length }} 条记录</span>
          </div>
          <el-table :data="detail.acceptanceRecords" border>
            <el-table-column prop="deliveryDate" label="交付日期" width="130" />
            <el-table-column prop="acceptanceDate" label="验收日期" width="130" />
            <el-table-column prop="acceptanceResult" label="验收结果" width="130" />
            <el-table-column prop="accepter" label="验收人" width="110" />
            <el-table-column prop="acceptanceNote" label="验收说明" min-width="220" show-overflow-tooltip />
            <el-table-column prop="exceptionNote" label="异常说明" min-width="180" show-overflow-tooltip />
          </el-table>
        </section>

        <section class="content-panel">
          <div class="section-title">
            <h2>流程记录</h2>
            <span>{{ detail.logs.length }} 条记录</span>
          </div>
          <div class="flow-log-list">
            <article v-for="log in detail.logs" :key="log.id" class="flow-log-card">
              <div class="flow-log-marker"></div>
              <div class="flow-log-body">
                <div class="flow-log-head">
                  <div>
                    <strong>{{ actionLabel(log.action) }}</strong>
                    <p>{{ statusLabel(log.fromStatus) || '开始' }} → {{ statusLabel(log.toStatus) }}</p>
                  </div>
                  <time>{{ formatDateTime(log.operatedAt) }}</time>
                </div>
                <div class="flow-log-meta">
                  <span>操作人</span>
                  <strong>{{ log.operator || '-' }}</strong>
                </div>
                <div class="flow-log-comment">
                  <span v-for="item in splitComment(log.comment)" :key="item">{{ item }}</span>
                </div>
              </div>
            </article>
          </div>
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
  approvalLabel,
  archiveLabel,
  availableActions,
  formatDateTime,
  money,
  paymentLabel,
  signingLabel,
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
const detail = reactive({ contract: null, paymentRecords: [], acceptanceRecords: [], logs: [] })
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
    detail.paymentRecords = data.paymentRecords || []
    detail.acceptanceRecords = data.acceptanceRecords || []
    detail.logs = data.logs
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '详情加载失败')
  }
}

function openAction(action) {
  currentAction.value = action
  actionVisible.value = true
}

function splitComment(comment) {
  if (!comment) {
    return ['无备注']
  }
  return comment.split('；').filter(Boolean)
}

async function submitAction(actionForm) {
  saving.value = true
  try {
    await runContractAction(props.id, {
      action: currentAction.value.value,
      operator: actionForm.operator,
      comment: actionForm.comment,
      paidAmount: currentAction.value.value === 'REGISTER_PAYMENT' ? actionForm.paidAmount : undefined,
      paymentStage: actionForm.paymentStage,
      paymentDate: actionForm.paymentDate,
      invoiceNo: actionForm.invoiceNo,
      deliveryDate: actionForm.deliveryDate,
      acceptanceDate: actionForm.acceptanceDate,
      acceptanceResult: actionForm.acceptanceResult,
      exceptionNote: actionForm.exceptionNote
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

<style scoped>
.flow-log-list {
  position: relative;
  display: grid;
  gap: 14px;
}

.status-strip {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 10px;
  margin-bottom: 18px;
}

.status-strip div {
  padding: 12px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: #f8fafc;
}

.status-strip span,
.status-strip strong {
  display: block;
}

.status-strip span {
  color: #64748b;
  font-size: 12px;
}

.status-strip strong {
  margin-top: 6px;
  color: #0f172a;
  font-size: 14px;
}

.flow-log-list::before {
  position: absolute;
  top: 10px;
  bottom: 10px;
  left: 9px;
  width: 2px;
  content: "";
  background: #e2e8f0;
}

.flow-log-card {
  position: relative;
  display: grid;
  grid-template-columns: 20px minmax(0, 1fr);
  gap: 14px;
}

.flow-log-marker {
  position: relative;
  z-index: 1;
  width: 20px;
  height: 20px;
  margin-top: 18px;
  border: 4px solid #eff6ff;
  border-radius: 50%;
  background: #2563eb;
}

.flow-log-body {
  padding: 14px 16px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: #ffffff;
}

.flow-log-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #eef2f7;
}

.flow-log-head strong {
  color: #0f172a;
  font-size: 15px;
}

.flow-log-head p {
  margin-top: 6px;
  color: #64748b;
  font-size: 13px;
}

.flow-log-head time {
  flex: 0 0 auto;
  color: #64748b;
  font-size: 12px;
}

.flow-log-meta {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  margin-top: 12px;
  padding: 5px 8px;
  border-radius: 999px;
  background: #f8fafc;
}

.flow-log-meta span {
  color: #64748b;
  font-size: 12px;
}

.flow-log-meta strong {
  color: #334155;
  font-size: 12px;
}

.flow-log-comment {
  display: grid;
  gap: 6px;
  margin-top: 12px;
}

.flow-log-comment span {
  color: #334155;
  font-size: 13px;
  line-height: 1.6;
}

@media (max-width: 760px) {
  .status-strip {
    grid-template-columns: 1fr;
  }

  .flow-log-head {
    display: grid;
  }
}
</style>

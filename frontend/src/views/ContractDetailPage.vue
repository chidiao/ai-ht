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

        <el-tabs class="detail-tabs" model-value="overview">
          <el-tab-pane label="概览" name="overview">
        <section class="content-panel">
          <div class="section-title">
            <h2>可执行动作</h2>
            <span>{{ currentRoleLabel }}</span>
          </div>
          <div class="action-list" v-if="permittedActions.length">
            <el-button
              v-for="action in permittedActions"
              :key="action.value"
              :type="dangerActions.includes(action.value) ? 'danger' : 'primary'"
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
          </el-tab-pane>

          <el-tab-pane label="付款" name="payment">
        <section class="content-panel">
          <div class="section-title">
            <h2>付款计划</h2>
            <el-button v-if="canManagePaymentPlans(currentRole)" type="primary" plain @click="openPaymentPlanDialog">新增计划</el-button>
          </div>
          <el-table :data="detail.paymentPlans" border>
            <el-table-column prop="paymentStage" label="付款阶段" width="120" />
            <el-table-column prop="plannedRatio" label="比例" width="100">
              <template #default="{ row }">{{ row.plannedRatio ?? '-' }}%</template>
            </el-table-column>
            <el-table-column prop="plannedAmount" label="计划金额" width="140">
              <template #default="{ row }">{{ money(row.plannedAmount) }}</template>
            </el-table-column>
            <el-table-column prop="paidAmount" label="计划已付" width="140">
              <template #default="{ row }">{{ money(row.paidAmount) }}</template>
            </el-table-column>
            <el-table-column label="计划状态" width="110">
              <template #default="{ row }">
                <el-tag :type="paymentPlanStatus(row).type">{{ paymentPlanStatus(row).label }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="plannedDate" label="计划日期" width="130" />
            <el-table-column prop="paymentCondition" label="付款条件" min-width="220" show-overflow-tooltip />
          </el-table>
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
          </el-tab-pane>

          <el-tab-pane label="验收" name="acceptance">
        <section class="content-panel">
          <div class="section-title">
            <h2>验收记录</h2>
            <el-button v-if="canRegisterAcceptance" type="primary" plain @click="acceptanceVisible = true">登记验收</el-button>
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
          </el-tab-pane>

          <el-tab-pane label="附件" name="attachments">
        <section class="content-panel">
          <div class="section-title">
            <h2>附件资料</h2>
            <el-button v-if="canManageAttachments(currentRole)" type="primary" plain @click="attachmentVisible = true">新增附件</el-button>
          </div>
          <el-table :data="detail.attachments" border>
            <el-table-column prop="fileName" label="附件名称" min-width="220" show-overflow-tooltip />
            <el-table-column prop="fileType" label="类型" width="130" />
            <el-table-column prop="uploader" label="上传人" width="110" />
            <el-table-column prop="uploadedAt" label="上传时间" width="170">
              <template #default="{ row }">{{ formatDateTime(row.uploadedAt) }}</template>
            </el-table-column>
            <el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip />
          </el-table>
        </section>
          </el-tab-pane>

          <el-tab-pane label="流程日志" name="logs">
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
          </el-tab-pane>
        </el-tabs>
      </template>

    <ContractActionDialog
      v-model="actionVisible"
      :contract="detail.contract"
      :action="currentAction"
      :saving="saving"
      :operator-name="currentUserName"
      :payment-plans="detail.paymentPlans"
      @submit="submitAction"
    />

    <el-dialog v-model="paymentPlanVisible" title="新增付款计划" width="560px">
      <el-form :model="paymentPlanForm" label-width="112px">
        <el-form-item label="付款阶段"><el-input v-model="paymentPlanForm.paymentStage" /></el-form-item>
        <el-form-item label="剩余可规划">
          <div class="payment-plan-remain">
            <span>{{ remainingPlanRatio }}%</span>
            <strong>{{ money(remainingPlanAmount) }}</strong>
          </div>
        </el-form-item>
        <el-form-item label="付款比例"><el-input-number v-model="paymentPlanForm.plannedRatio" :min="0" :max="remainingPlanRatio" :precision="2" @change="syncPaymentPlanAmount" /></el-form-item>
        <el-form-item label="计划金额"><el-input-number v-model="paymentPlanForm.plannedAmount" :min="0" :max="remainingPlanAmount" :precision="2" @change="syncPaymentPlanRatio" /></el-form-item>
        <el-form-item label="计划日期"><el-date-picker v-model="paymentPlanForm.plannedDate" type="date" value-format="YYYY-MM-DD" /></el-form-item>
        <el-form-item label="付款条件"><el-input v-model="paymentPlanForm.paymentCondition" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="paymentPlanVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPaymentPlan">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="acceptanceVisible" title="登记验收" width="560px">
      <el-form :model="acceptanceForm" label-width="112px">
        <el-form-item label="交付日期"><el-date-picker v-model="acceptanceForm.deliveryDate" type="date" value-format="YYYY-MM-DD" /></el-form-item>
        <el-form-item label="验收日期"><el-date-picker v-model="acceptanceForm.acceptanceDate" type="date" value-format="YYYY-MM-DD" /></el-form-item>
        <el-form-item label="验收结论">
          <el-select v-model="acceptanceForm.acceptanceResult">
            <el-option label="验收通过" value="验收通过" />
            <el-option label="有条件通过" value="有条件通过" />
            <el-option label="验收不通过" value="验收不通过" />
          </el-select>
        </el-form-item>
        <el-form-item label="验收说明"><el-input v-model="acceptanceForm.acceptanceNote" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="异常说明"><el-input v-model="acceptanceForm.exceptionNote" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="acceptanceVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAcceptance">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="attachmentVisible" title="新增附件" width="520px">
      <el-form :model="attachmentForm" label-width="96px">
        <el-form-item label="附件名称"><el-input v-model="attachmentForm.fileName" /></el-form-item>
        <el-form-item label="附件类型">
          <el-select v-model="attachmentForm.fileType">
            <el-option label="合同正文" value="合同正文" />
            <el-option label="报价单" value="报价单" />
            <el-option label="审批材料" value="审批材料" />
            <el-option label="发票" value="发票" />
            <el-option label="验收单" value="验收单" />
            <el-option label="补充协议" value="补充协议" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="attachmentForm.remark" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="attachmentVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAttachment">保存</el-button>
      </template>
    </el-dialog>
  </AppLayout>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import AppLayout from '../components/AppLayout.vue'
import ContractActionDialog from '../components/ContractActionDialog.vue'
import PageHeader from '../components/PageHeader.vue'
import WorkflowProgress from '../components/WorkflowProgress.vue'
import { addContractAcceptance, addContractAttachment, addContractPaymentPlan, fetchContractDetail, runContractAction } from '../api/contracts'
import { useSession } from '../stores/session'
import { canEditContract, canManageAttachments, canManagePaymentPlans, canRunAction } from '../utils/permissions'
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
const detail = reactive({ contract: null, paymentPlans: [], paymentRecords: [], acceptanceRecords: [], attachments: [], logs: [] })
const actionVisible = ref(false)
const paymentPlanVisible = ref(false)
const acceptanceVisible = ref(false)
const attachmentVisible = ref(false)
const currentAction = ref(null)
const saving = ref(false)
const paymentPlanForm = reactive({ paymentStage: '验收款', plannedRatio: 70, plannedAmount: 0, plannedDate: '', paymentCondition: '验收通过后支付' })
const acceptanceForm = reactive({ deliveryDate: '', acceptanceDate: '', acceptanceResult: '验收通过', acceptanceNote: '', exceptionNote: '' })
const attachmentForm = reactive({ fileName: '', fileType: '合同正文', remark: '' })
const dangerActions = ['REJECT', 'CANCEL_PROCESS', 'TERMINATE']

const permittedActions = computed(() => {
  if (!detail.contract) {
    return []
  }
  return availableActions(detail.contract)
    .filter((action) => action.value !== 'REGISTER_ACCEPTANCE' || detail.acceptanceRecords.length === 0)
    .filter((action) => action.value !== 'COMPLETE' || detail.acceptanceRecords.length > 0)
    .filter((action) => canRunAction(currentRole.value, action.value))
})

const canRegisterAcceptance = computed(() => ['EXECUTING', 'COMPLETED'].includes(detail.contract?.status) && canRunAction(currentRole.value, 'REGISTER_ACCEPTANCE'))
const plannedRatioTotal = computed(() => detail.paymentPlans.reduce((sum, item) => sum + Number(item.plannedRatio || 0), 0))
const plannedAmountTotal = computed(() => detail.paymentPlans.reduce((sum, item) => sum + Number(item.plannedAmount || 0), 0))
const remainingPlanRatio = computed(() => roundMoney(Math.max(100 - plannedRatioTotal.value, 0)))
const remainingPlanAmount = computed(() => roundMoney(Math.max(Number(detail.contract?.amount || 0) - plannedAmountTotal.value, 0)))

async function loadDetail() {
  try {
    const data = await fetchContractDetail(props.id)
    detail.contract = data.contract
    detail.paymentPlans = data.paymentPlans || []
    detail.paymentRecords = data.paymentRecords || []
    detail.acceptanceRecords = data.acceptanceRecords || []
    detail.attachments = data.attachments || []
    detail.logs = data.logs
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '详情加载失败')
  }
}

async function submitPaymentPlan() {
  try {
    syncPaymentPlanAmount()
    if (Number(paymentPlanForm.plannedRatio || 0) <= 0 || Number(paymentPlanForm.plannedAmount || 0) <= 0) {
      ElMessage.warning('付款比例和计划金额必须大于 0')
      return
    }
    if (Number(paymentPlanForm.plannedRatio || 0) > remainingPlanRatio.value || Number(paymentPlanForm.plannedAmount || 0) > remainingPlanAmount.value) {
      ElMessage.warning('付款计划合计不能超过合同金额或 100%')
      return
    }
    await addContractPaymentPlan(props.id, { ...paymentPlanForm, creator: currentUserName.value })
    ElMessage.success('付款计划已新增')
    paymentPlanVisible.value = false
    await loadDetail()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '付款计划保存失败')
  }
}

function syncPaymentPlanAmount() {
  const contractAmount = Number(detail.contract?.amount || 0)
  if (!contractAmount) {
    return
  }
  paymentPlanForm.plannedRatio = Math.min(Number(paymentPlanForm.plannedRatio || 0), remainingPlanRatio.value)
  paymentPlanForm.plannedAmount = Math.min(roundMoney(contractAmount * Number(paymentPlanForm.plannedRatio || 0) / 100), remainingPlanAmount.value)
}

function syncPaymentPlanRatio() {
  const contractAmount = Number(detail.contract?.amount || 0)
  if (!contractAmount) {
    return
  }
  paymentPlanForm.plannedAmount = Math.min(Number(paymentPlanForm.plannedAmount || 0), remainingPlanAmount.value)
  paymentPlanForm.plannedRatio = Math.min(roundMoney(Number(paymentPlanForm.plannedAmount || 0) / contractAmount * 100), remainingPlanRatio.value)
}

function roundMoney(value) {
  return Math.round(Number(value || 0) * 100) / 100
}

async function submitAcceptance() {
  try {
    await addContractAcceptance(props.id, { ...acceptanceForm, accepter: currentUserName.value })
    ElMessage.success('验收记录已登记')
    acceptanceVisible.value = false
    await loadDetail()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '验收保存失败')
  }
}

async function submitAttachment() {
  try {
    await addContractAttachment(props.id, { ...attachmentForm, uploader: currentUserName.value })
    ElMessage.success('附件记录已新增')
    attachmentVisible.value = false
    await loadDetail()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '附件保存失败')
  }
}

function openAction(action) {
  currentAction.value = action
  actionVisible.value = true
}

function openPaymentPlanDialog() {
  paymentPlanForm.plannedRatio = remainingPlanRatio.value || 0
  syncPaymentPlanAmount()
  paymentPlanVisible.value = true
}

function splitComment(comment) {
  if (!comment) {
    return ['无备注']
  }
  return comment.split('；').filter(Boolean)
}

function paymentPlanStatus(plan) {
  const paidAmount = Number(plan.paidAmount || 0)
  const plannedAmount = Number(plan.plannedAmount || 0)
  if (plannedAmount > 0 && paidAmount >= plannedAmount) {
    return { label: '已付清', type: 'success' }
  }
  if (paidAmount > 0) {
    return { label: '部分付款', type: 'warning' }
  }
  return { label: '未付款', type: 'info' }
}

async function submitAction(actionForm) {
  saving.value = true
  try {
    if (currentAction.value.value === 'REGISTER_ACCEPTANCE') {
      await addContractAcceptance(props.id, {
        deliveryDate: actionForm.deliveryDate,
        acceptanceDate: actionForm.acceptanceDate,
        acceptanceResult: actionForm.acceptanceResult,
        acceptanceNote: actionForm.comment,
        exceptionNote: actionForm.exceptionNote,
        accepter: actionForm.operator
      })
      ElMessage.success('验收记录已登记')
      actionVisible.value = false
      await loadDetail()
      return
    }
    await runContractAction(props.id, {
      action: currentAction.value.value,
      operator: actionForm.operator,
      comment: actionForm.comment,
      paidAmount: currentAction.value.value === 'REGISTER_PAYMENT' ? actionForm.paidAmount : undefined,
      paymentPlanId: actionForm.paymentPlanId,
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

.detail-tabs {
  margin-top: 18px;
}

:deep(.el-dialog .el-input-number),
:deep(.el-dialog .el-date-editor),
:deep(.el-dialog .el-select) {
  width: 100%;
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

.payment-plan-remain {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding: 9px 12px;
  border: 1px solid #dde4ee;
  border-radius: 8px;
  background: #f8fafc;
}

.payment-plan-remain span {
  color: #2563eb;
  font-weight: 700;
}

.payment-plan-remain strong {
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

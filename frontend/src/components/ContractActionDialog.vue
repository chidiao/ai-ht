<template>
  <el-dialog v-model="visible" :title="action?.label" width="560px">
    <div v-if="action" class="action-context">
      <span>操作人</span>
      <strong>{{ actionForm.operator }}</strong>
    </div>

    <el-form :model="actionForm" label-width="112px">
      <template v-if="action?.value === 'SUBMIT_SUPPLIER_CONFIRM'">
        <el-form-item label="供应商联系人">
          <el-input v-model="actionForm.supplierContact" placeholder="例如：张经理" />
        </el-form-item>
        <el-form-item label="期望确认日期">
          <el-date-picker v-model="actionForm.expectedConfirmDate" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="发送方式">
          <el-select v-model="actionForm.confirmMethod">
            <el-option label="邮件确认" value="邮件确认" />
            <el-option label="线下盖章确认" value="线下盖章确认" />
            <el-option label="系统确认" value="系统确认" />
          </el-select>
        </el-form-item>
      </template>

      <template v-else-if="action?.value === 'SUBMIT_APPROVAL'">
        <el-form-item label="审批负责人">
          <el-input v-model="actionForm.approver" placeholder="例如：赵总" />
        </el-form-item>
        <el-form-item label="提交说明">
          <el-input v-model="actionForm.comment" type="textarea" :rows="3" placeholder="说明供应商确认结果和需审批事项" />
        </el-form-item>
      </template>

      <template v-else-if="action?.value === 'CANCEL_PROCESS'">
        <el-alert class="action-alert" type="warning" :closable="false" show-icon>
          <template #title>取消用于合同未正式生效前的流程中止，取消后不再继续提交或审批。</template>
        </el-alert>
        <el-form-item label="取消原因">
          <el-input v-model="actionForm.comment" type="textarea" :rows="3" placeholder="说明需求取消、供应商不配合或条款变更等原因" />
        </el-form-item>
      </template>

      <template v-else-if="action?.value === 'WITHDRAW_APPROVAL'">
        <el-form-item label="撤回原因">
          <el-input v-model="actionForm.comment" type="textarea" :rows="3" placeholder="说明需补充或调整的合同内容" />
        </el-form-item>
      </template>

      <template v-else-if="action?.value === 'APPROVE' || action?.value === 'REJECT'">
        <el-form-item label="审批意见">
          <el-input v-model="actionForm.comment" type="textarea" :rows="3" placeholder="填写审批意见" />
        </el-form-item>
      </template>

      <template v-else-if="action?.value === 'START_EXECUTION'">
        <el-form-item label="执行负责人">
          <el-input v-model="actionForm.executionOwner" placeholder="例如：李倩" />
        </el-form-item>
        <el-form-item label="计划交付日期">
          <el-date-picker v-model="actionForm.plannedDeliveryDate" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="执行说明">
          <el-input v-model="actionForm.comment" type="textarea" :rows="3" />
        </el-form-item>
      </template>

      <template v-else-if="action?.value === 'REGISTER_PAYMENT'">
        <div class="payment-summary">
          <div>
            <span>合同金额</span>
            <strong>{{ formatMoney(contract?.amount) }}</strong>
          </div>
          <div>
            <span>已付金额</span>
            <strong>{{ formatMoney(contract?.paidAmount) }}</strong>
          </div>
          <div>
            <span>剩余可付</span>
            <strong>{{ formatMoney(remainingAmount) }}</strong>
          </div>
        </div>
        <el-form-item label="付款计划">
          <el-select v-model="actionForm.paymentPlanId" clearable placeholder="选择计划或手动填写" @change="applyPaymentPlan">
            <el-option
              v-for="plan in unpaidPaymentPlans"
              :key="plan.id"
              :label="`${plan.paymentStage} · 剩余 ${formatMoney(paymentPlanRemaining(plan))}`"
              :value="plan.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="付款阶段">
          <el-input v-model="actionForm.paymentStage" placeholder="例如：预付款、验收款、尾款" />
        </el-form-item>
        <el-form-item label="本次付款金额">
          <el-input-number
            v-model="actionForm.paidAmount"
            :min="0"
            :max="paymentAmountMax"
            :precision="2"
            :step="1000"
            :disabled="hasSelectedPaymentPlan"
          />
        </el-form-item>
        <el-form-item label="付款日期">
          <el-date-picker v-model="actionForm.paymentDate" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="发票编号">
          <el-input v-model="actionForm.invoiceNo" placeholder="可选" />
        </el-form-item>
        <el-form-item label="付款说明">
          <el-input v-model="actionForm.comment" type="textarea" :rows="3" />
        </el-form-item>
      </template>

      <template v-else-if="action?.value === 'REGISTER_ACCEPTANCE'">
        <el-form-item label="交付日期">
          <el-date-picker v-model="actionForm.deliveryDate" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="验收日期">
          <el-date-picker v-model="actionForm.acceptanceDate" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="验收结论">
          <el-select v-model="actionForm.acceptanceResult">
            <el-option label="验收通过" value="验收通过" />
            <el-option label="有条件通过" value="有条件通过" />
          </el-select>
        </el-form-item>
        <el-form-item label="验收说明">
          <el-input v-model="actionForm.comment" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="异常说明">
          <el-input v-model="actionForm.exceptionNote" type="textarea" :rows="2" />
        </el-form-item>
      </template>

      <template v-else-if="action?.value === 'COMPLETE'">
        <el-form-item label="完成说明">
          <el-input v-model="actionForm.comment" type="textarea" :rows="3" placeholder="确认付款和验收已完成，关闭合同履约流程" />
        </el-form-item>
      </template>

      <template v-else-if="action?.value === 'ARCHIVE'">
        <el-form-item label="归档编号">
          <el-input v-model="actionForm.archiveNo" placeholder="例如：ARCH-2026-001" />
        </el-form-item>
        <el-form-item label="归档位置">
          <el-input v-model="actionForm.archiveLocation" placeholder="例如：行政档案柜 A-03" />
        </el-form-item>
        <el-form-item label="归档说明">
          <el-input v-model="actionForm.comment" type="textarea" :rows="3" />
        </el-form-item>
      </template>

      <template v-else-if="action?.value === 'REQUEST_TERMINATION'">
        <el-alert class="action-alert" type="warning" :closable="false" show-icon>
          <template #title>终止申请会进入审批，需说明终止原因、责任和已付款结算情况。</template>
        </el-alert>
        <el-form-item label="申请日期">
          <el-date-picker v-model="actionForm.terminateDate" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="申请说明">
          <el-input v-model="actionForm.comment" type="textarea" :rows="3" placeholder="必填，例如：供应商无法按期交付，双方确认终止并按已完成部分结算" />
        </el-form-item>
      </template>

      <template v-else-if="action?.value === 'APPROVE_TERMINATION' || action?.value === 'REJECT_TERMINATION'">
        <el-form-item label="审批意见">
          <el-input v-model="actionForm.comment" type="textarea" :rows="3" placeholder="说明是否同意终止，以及付款、交付、责任处理意见" />
        </el-form-item>
      </template>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="submit">确认</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ElMessage } from 'element-plus'
import { computed, reactive, watch } from 'vue'

const props = defineProps({
  contract: {
    type: Object,
    default: null
  },
  action: {
    type: Object,
    default: null
  },
  saving: {
    type: Boolean,
    default: false
  },
  operatorName: {
    type: String,
    default: ''
  },
  paymentPlans: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['submit'])
const visible = defineModel({ required: true })
const actionForm = reactive(emptyActionForm())
const requiredCommentActions = ['CANCEL_PROCESS', 'WITHDRAW_APPROVAL', 'REQUEST_TERMINATION', 'APPROVE_TERMINATION', 'REJECT_TERMINATION']
const remainingAmount = computed(() => Math.max(Number(props.contract?.amount || 0) - Number(props.contract?.paidAmount || 0), 0))
const unpaidPaymentPlans = computed(() => props.paymentPlans.filter((item) => paymentPlanRemaining(item) > 0))
const selectedPaymentPlan = computed(() => props.paymentPlans.find((item) => item.id === actionForm.paymentPlanId))
const hasSelectedPaymentPlan = computed(() => Boolean(selectedPaymentPlan.value))
const selectedPaymentPlanAmount = computed(() => {
  if (!selectedPaymentPlan.value) {
    return remainingAmount.value
  }
  return Math.min(paymentPlanRemaining(selectedPaymentPlan.value), remainingAmount.value)
})
const paymentAmountMax = computed(() => hasSelectedPaymentPlan.value ? selectedPaymentPlanAmount.value : remainingAmount.value)

watch(
  () => [visible.value, props.contract, props.action],
  () => {
    if (visible.value) {
      Object.assign(actionForm, emptyActionForm(), defaultsForAction())
    }
  },
  { immediate: true }
)

function submit() {
  if (requiredCommentActions.includes(props.action?.value) && !actionForm.comment.trim()) {
    ElMessage.warning('请填写操作原因或审批意见')
    return
  }
  emit('submit', {
    operator: actionForm.operator,
    paidAmount: props.action?.value === 'REGISTER_PAYMENT' ? actionForm.paidAmount : undefined,
    paymentPlanId: props.action?.value === 'REGISTER_PAYMENT' && actionForm.paymentPlanId ? actionForm.paymentPlanId : undefined,
    paymentStage: props.action?.value === 'REGISTER_PAYMENT' ? actionForm.paymentStage : undefined,
    paymentDate: props.action?.value === 'REGISTER_PAYMENT' ? actionForm.paymentDate : undefined,
    invoiceNo: props.action?.value === 'REGISTER_PAYMENT' ? actionForm.invoiceNo : undefined,
    deliveryDate: props.action?.value === 'REGISTER_ACCEPTANCE' ? actionForm.deliveryDate : undefined,
    acceptanceDate: props.action?.value === 'REGISTER_ACCEPTANCE' ? actionForm.acceptanceDate : undefined,
    acceptanceResult: props.action?.value === 'REGISTER_ACCEPTANCE' ? actionForm.acceptanceResult : undefined,
    exceptionNote: props.action?.value === 'REGISTER_ACCEPTANCE' ? actionForm.exceptionNote : undefined,
    comment: buildComment()
  })
}

function defaultsForAction() {
  const today = formatDate(new Date())
  return {
    operator: props.operatorName || '',
    paidAmount: props.action?.value === 'REGISTER_PAYMENT' ? remainingAmount.value : 0,
    paymentPlanId: '',
    paymentStage: '验收款',
    expectedConfirmDate: formatDate(addDays(new Date(), 3)),
    confirmMethod: '邮件确认',
    approver: '赵总',
    executionOwner: props.contract?.owner || '',
    plannedDeliveryDate: formatDate(addDays(new Date(), 30)),
    deliveryDate: today,
    paymentDate: today,
    acceptanceDate: today,
    acceptanceResult: '验收通过',
    archiveNo: `ARCH-${new Date().getFullYear()}-${String(props.contract?.id || 1).padStart(3, '0')}`,
    archiveLocation: '行政档案柜 A-01',
    terminateDate: today
  }
}

function buildComment() {
  const action = props.action?.value
  const lines = []
  if (action === 'SUBMIT_SUPPLIER_CONFIRM') {
    lines.push(`供应商联系人：${actionForm.supplierContact || '-'}`)
    lines.push(`期望确认日期：${actionForm.expectedConfirmDate || '-'}`)
    lines.push(`发送方式：${actionForm.confirmMethod || '-'}`)
  } else if (action === 'SUBMIT_APPROVAL') {
    lines.push(`审批负责人：${actionForm.approver || '-'}`)
  } else if (action === 'CANCEL_PROCESS') {
    lines.push(`取消原因：${actionForm.comment || '-'}`)
  } else if (action === 'WITHDRAW_APPROVAL') {
    lines.push(`撤回原因：${actionForm.comment || '-'}`)
  } else if (action === 'START_EXECUTION') {
    lines.push(`执行负责人：${actionForm.executionOwner || '-'}`)
    lines.push(`计划交付日期：${actionForm.plannedDeliveryDate || '-'}`)
  } else if (action === 'REGISTER_PAYMENT') {
    lines.push(`本次付款金额：${actionForm.paidAmount}`)
    lines.push(`付款阶段：${actionForm.paymentStage || '-'}`)
    lines.push(`付款日期：${actionForm.paymentDate || '-'}`)
    lines.push(`发票编号：${actionForm.invoiceNo || '-'}`)
  } else if (action === 'REGISTER_ACCEPTANCE') {
    lines.push(`交付日期：${actionForm.deliveryDate || '-'}`)
    lines.push(`验收日期：${actionForm.acceptanceDate || '-'}`)
    lines.push(`验收结论：${actionForm.acceptanceResult || '-'}`)
    if (actionForm.exceptionNote) {
      lines.push(`异常说明：${actionForm.exceptionNote}`)
    }
  } else if (action === 'ARCHIVE') {
    lines.push(`归档编号：${actionForm.archiveNo || '-'}`)
    lines.push(`归档位置：${actionForm.archiveLocation || '-'}`)
  } else if (action === 'REQUEST_TERMINATION') {
    lines.push(`申请日期：${actionForm.terminateDate || '-'}`)
  } else if (action === 'APPROVE_TERMINATION' || action === 'REJECT_TERMINATION') {
    lines.push(`审批意见：${actionForm.comment || '-'}`)
  }
  if (actionForm.comment && !['CANCEL_PROCESS', 'WITHDRAW_APPROVAL', 'APPROVE_TERMINATION', 'REJECT_TERMINATION'].includes(action)) {
    lines.push(`说明：${actionForm.comment}`)
  }
  return lines.join('；')
}

function emptyActionForm() {
  return {
    operator: '',
    comment: '',
    paidAmount: 0,
    paymentPlanId: '',
    paymentStage: '',
    supplierContact: '',
    expectedConfirmDate: '',
    confirmMethod: '',
    approver: '',
    executionOwner: '',
    plannedDeliveryDate: '',
    deliveryDate: '',
    paymentDate: '',
    invoiceNo: '',
    acceptanceDate: '',
    acceptanceResult: '',
    exceptionNote: '',
    archiveNo: '',
    archiveLocation: '',
    terminateDate: ''
  }
}

function addDays(date, days) {
  const next = new Date(date)
  next.setDate(next.getDate() + days)
  return next
}

function formatDate(date) {
  return date.toISOString().slice(0, 10)
}

function formatMoney(value) {
  return Number(value || 0).toLocaleString('zh-CN', { style: 'currency', currency: 'CNY' })
}

function paymentPlanRemaining(plan) {
  return Math.max(Number(plan?.plannedAmount || 0) - Number(plan?.paidAmount || 0), 0)
}

function applyPaymentPlan(planId) {
  const plan = props.paymentPlans.find((item) => item.id === planId)
  if (!plan) {
    return
  }
  actionForm.paymentStage = plan.paymentStage
  actionForm.paidAmount = selectedPaymentPlanAmount.value
}
</script>

<style scoped>
.action-context {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18px;
  padding: 10px 12px;
  border: 1px solid #dde4ee;
  border-radius: 8px;
  background: #f8fafc;
}

.action-context span {
  color: #64748b;
  font-size: 13px;
}

.action-context strong {
  color: #0f172a;
  font-size: 14px;
}

.payment-summary {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  margin-bottom: 18px;
}

.payment-summary div {
  padding: 10px 12px;
  border: 1px solid #dde4ee;
  border-radius: 8px;
  background: #f8fafc;
}

.payment-summary span,
.payment-summary strong {
  display: block;
}

.payment-summary span {
  color: #64748b;
  font-size: 12px;
}

.payment-summary strong {
  margin-top: 4px;
  color: #0f172a;
  font-size: 14px;
}

:deep(.el-input-number),
:deep(.el-date-editor),
:deep(.el-select) {
  width: 100%;
}

.action-alert {
  margin-bottom: 16px;
}
</style>

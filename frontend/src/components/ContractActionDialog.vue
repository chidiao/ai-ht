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
        <el-form-item label="已付金额">
          <el-input-number v-model="actionForm.paidAmount" :min="0" :max="contract?.amount || 0" :precision="2" :step="1000" />
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

      <template v-else-if="action?.value === 'COMPLETE'">
        <el-form-item label="验收日期">
          <el-date-picker v-model="actionForm.acceptanceDate" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="验收结论">
          <el-select v-model="actionForm.acceptanceResult">
            <el-option label="验收通过" value="验收通过" />
            <el-option label="有条件通过" value="有条件通过" />
          </el-select>
        </el-form-item>
        <el-form-item label="完成说明">
          <el-input v-model="actionForm.comment" type="textarea" :rows="3" />
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

      <template v-else-if="action?.value === 'TERMINATE'">
        <el-form-item label="终止日期">
          <el-date-picker v-model="actionForm.terminateDate" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="终止原因">
          <el-input v-model="actionForm.comment" type="textarea" :rows="3" placeholder="说明终止原因" />
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
import { reactive, watch } from 'vue'

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
  }
})

const emit = defineEmits(['submit'])
const visible = defineModel({ required: true })
const actionForm = reactive(emptyActionForm())

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
  emit('submit', {
    operator: actionForm.operator,
    paidAmount: props.action?.value === 'REGISTER_PAYMENT' ? actionForm.paidAmount : undefined,
    comment: buildComment()
  })
}

function defaultsForAction() {
  const today = formatDate(new Date())
  return {
    operator: props.operatorName || '',
    paidAmount: props.action?.value === 'REGISTER_PAYMENT' ? Number(props.contract?.amount || 0) : 0,
    expectedConfirmDate: formatDate(addDays(new Date(), 3)),
    confirmMethod: '邮件确认',
    approver: '赵总',
    executionOwner: props.contract?.owner || '',
    plannedDeliveryDate: formatDate(addDays(new Date(), 30)),
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
  } else if (action === 'START_EXECUTION') {
    lines.push(`执行负责人：${actionForm.executionOwner || '-'}`)
    lines.push(`计划交付日期：${actionForm.plannedDeliveryDate || '-'}`)
  } else if (action === 'REGISTER_PAYMENT') {
    lines.push(`已付金额：${actionForm.paidAmount}`)
    lines.push(`付款日期：${actionForm.paymentDate || '-'}`)
    lines.push(`发票编号：${actionForm.invoiceNo || '-'}`)
  } else if (action === 'COMPLETE') {
    lines.push(`验收日期：${actionForm.acceptanceDate || '-'}`)
    lines.push(`验收结论：${actionForm.acceptanceResult || '-'}`)
  } else if (action === 'ARCHIVE') {
    lines.push(`归档编号：${actionForm.archiveNo || '-'}`)
    lines.push(`归档位置：${actionForm.archiveLocation || '-'}`)
  } else if (action === 'TERMINATE') {
    lines.push(`终止日期：${actionForm.terminateDate || '-'}`)
  }
  if (actionForm.comment) {
    lines.push(`说明：${actionForm.comment}`)
  }
  return lines.join('；')
}

function emptyActionForm() {
  return {
    operator: '',
    comment: '',
    paidAmount: 0,
    supplierContact: '',
    expectedConfirmDate: '',
    confirmMethod: '',
    approver: '',
    executionOwner: '',
    plannedDeliveryDate: '',
    paymentDate: '',
    invoiceNo: '',
    acceptanceDate: '',
    acceptanceResult: '',
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

:deep(.el-input-number),
:deep(.el-date-editor),
:deep(.el-select) {
  width: 100%;
}
</style>

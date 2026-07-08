<template>
  <section class="content-panel">
    <div class="section-title">
      <h2>流程进度</h2>
      <el-tag :type="statusType(status)">{{ statusLabel(status) }}</el-tag>
    </div>
    <el-steps :active="activeStep" finish-status="success" align-center>
      <el-step v-for="step in steps" :key="step.status" :title="step.label" />
    </el-steps>
  </section>
</template>

<script setup>
import { computed } from 'vue'
import { statusLabel, statusType } from '../utils/contractFormat'

const props = defineProps({
  status: {
    type: String,
    required: true
  }
})

const steps = [
  { status: 'DRAFT', label: '拟稿' },
  { status: 'SUPPLIER_CONFIRMING', label: '供应商确认' },
  { status: 'PENDING_APPROVAL', label: '内部审批' },
  { status: 'ACTIVE', label: '生效' },
  { status: 'EXECUTING', label: '执行' },
  { status: 'PAYING', label: '付款' },
  { status: 'COMPLETED', label: '完成' },
  { status: 'ARCHIVED', label: '归档' }
]

const activeStep = computed(() => {
  if (props.status === 'REJECTED' || props.status === 'TERMINATED') {
    return 2
  }
  return Math.max(0, steps.findIndex((item) => item.status === props.status))
})
</script>

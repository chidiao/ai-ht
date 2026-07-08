<template>
  <section class="content-panel workflow-panel">
    <div class="section-title workflow-title">
      <div>
        <h2>流程进度</h2>
        <p>{{ currentSummary }}</p>
      </div>
      <el-tag :type="statusType(status)" size="large">{{ statusLabel(status) }}</el-tag>
    </div>

    <div class="workflow-track" :class="{ exception: isException }">
      <div
        v-for="(step, index) in decoratedSteps"
        :key="step.status"
        class="workflow-step"
        :class="step.state"
      >
        <div class="workflow-node">
          <el-icon v-if="step.state === 'done'"><Check /></el-icon>
          <el-icon v-else-if="step.state === 'current'"><Position /></el-icon>
          <el-icon v-else-if="step.state === 'exception'"><Warning /></el-icon>
          <span v-else>{{ index + 1 }}</span>
        </div>
        <div class="workflow-copy">
          <strong>{{ step.label }}</strong>
          <span v-if="step.state === 'current' || step.state === 'exception'">{{ step.description }}</span>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed } from 'vue'
import { Check, Position, Warning } from '@element-plus/icons-vue'
import { statusLabel, statusType } from '../utils/contractFormat'

const props = defineProps({
  status: {
    type: String,
    required: true
  }
})

const steps = [
  { status: 'DRAFT', label: '拟稿', description: '采购创建合同' },
  { status: 'SUPPLIER_CONFIRMING', label: '供应商确认', description: '确认条款与商务信息' },
  { status: 'PENDING_APPROVAL', label: '内部审批', description: '负责人审批生效' },
  { status: 'ACTIVE', label: '合同生效', description: '进入正式履约' },
  { status: 'EXECUTING', label: '履约执行', description: '跟进付款、交付与验收' },
  { status: 'COMPLETED', label: '合同完成', description: '付款验收闭环' },
  { status: 'ARCHIVED', label: '归档', description: '完成档案沉淀' }
]

const exceptionText = {
  REJECTED: '审批驳回，需返回编辑后重新提交。',
  CANCELLED: '流程已取消，合同未进入正式履约。',
  TERMINATED: '合同已终止，流程不再继续。'
}

const isException = computed(() => ['REJECTED', 'CANCELLED', 'TERMINATED'].includes(props.status))

const activeIndex = computed(() => {
  if (props.status === 'REJECTED') {
    return 2
  }
  if (props.status === 'CANCELLED') {
    return 0
  }
  if (props.status === 'TERMINATED') {
    return Math.max(0, steps.findIndex((item) => item.status === 'EXECUTING'))
  }
  if (props.status === 'TERMINATION_PENDING') {
    return Math.max(0, steps.findIndex((item) => item.status === 'EXECUTING'))
  }
  return Math.max(0, steps.findIndex((item) => item.status === props.status))
})

const currentSummary = computed(() => {
  if (isException.value) {
    return exceptionText[props.status]
  }
  const current = steps[activeIndex.value]
  return current ? `当前处于「${current.label}」阶段，${current.description}。` : '流程状态待确认。'
})

const decoratedSteps = computed(() =>
  steps.map((step, index) => {
    let state = 'todo'
    if (isException.value && index === activeIndex.value) {
      state = 'exception'
    } else if (index < activeIndex.value) {
      state = 'done'
    } else if (index === activeIndex.value) {
      state = 'current'
    }
    return { ...step, state }
  })
)
</script>

<style scoped>
.workflow-panel {
  overflow: hidden;
}

.workflow-title {
  align-items: flex-start;
}

.workflow-title p {
  margin-top: 8px;
  color: #64748b;
  font-size: 13px;
  line-height: 1.6;
}

.workflow-track {
  position: relative;
  display: grid;
  grid-template-columns: repeat(7, minmax(96px, 1fr));
  gap: 0;
  padding: 8px 8px 4px;
}

.workflow-step {
  position: relative;
  min-height: 86px;
  padding: 0 8px;
  text-align: center;
}

.workflow-step::after {
  position: absolute;
  top: 17px;
  right: calc(-50% + 17px);
  left: calc(50% + 17px);
  width: auto;
  height: 2px;
  content: "";
  background: #dbe3ef;
}

.workflow-step:last-child::after {
  display: none;
}

.workflow-node {
  display: grid;
  width: 34px;
  height: 34px;
  place-items: center;
  margin: 0 auto 12px;
  border: 2px solid #cbd5e1;
  border-radius: 50%;
  color: #64748b;
  background: #ffffff;
  font-size: 13px;
  font-weight: 700;
  position: relative;
  z-index: 1;
}

.workflow-copy strong,
.workflow-copy span {
  display: block;
}

.workflow-copy strong {
  color: #334155;
  font-size: 14px;
  font-weight: 600;
}

.workflow-copy span {
  margin-top: 6px;
  color: #475569;
  font-size: 12px;
  line-height: 1.5;
}

.workflow-step.done {
  color: #047857;
}

.workflow-step.done::after {
  background: #86efac;
}

.workflow-step.done .workflow-node {
  border-color: #86efac;
  color: #047857;
  background: #dcfce7;
}

.workflow-step.current {
  color: #1d4ed8;
}

.workflow-step.current .workflow-node {
  border-color: #60a5fa;
  color: #1d4ed8;
  background: #dbeafe;
  box-shadow: 0 0 0 5px #eff6ff;
}

.workflow-step.current .workflow-copy strong {
  color: #1d4ed8;
}

.workflow-step.exception {
  color: #be123c;
}

.workflow-step.exception .workflow-node {
  border-color: #fb7185;
  color: #be123c;
  background: #ffe4e6;
  box-shadow: 0 0 0 5px #fff1f2;
}

.workflow-step.exception .workflow-copy strong {
  color: #be123c;
}

.workflow-track.exception .workflow-step::after {
  background: #e2e8f0;
}

@media (max-width: 1180px) {
  .workflow-track {
    grid-template-columns: repeat(4, 1fr);
    row-gap: 18px;
  }

  .workflow-step:nth-child(4n)::after {
    display: none;
  }
}

@media (max-width: 760px) {
  .workflow-track {
    grid-template-columns: 1fr;
  }

  .workflow-step {
    display: grid;
    grid-template-columns: 34px 1fr;
    gap: 12px;
    min-height: 72px;
    padding: 0;
    text-align: left;
  }

  .workflow-step::after {
    top: 34px;
    right: auto;
    bottom: auto;
    left: 16px;
    width: 2px;
    height: calc(100% - 16px);
  }

  .workflow-node {
    margin: 0;
  }
}
</style>

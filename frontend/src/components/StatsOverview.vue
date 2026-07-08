<template>
  <section class="stats-grid">
    <button
      v-for="item in statItems"
      :key="item.key"
      class="stat-card"
      :class="[`stat-card--${item.tone}`, { active: activeKey === item.key }]"
      type="button"
      @click="$emit('select', item.key)"
    >
      <span class="stat-icon">
        <el-icon><component :is="item.icon" /></el-icon>
      </span>
      <span class="stat-label">{{ item.label }}</span>
      <strong>{{ item.value }}</strong>
      <small>{{ item.hint }}</small>
    </button>
  </section>
</template>

<script setup>
import { computed } from 'vue'
import { Bell, Checked, Files, Timer, Wallet } from '@element-plus/icons-vue'

const props = defineProps({
  stats: {
    type: Object,
    required: true
  },
  activeKey: {
    type: String,
    default: 'total'
  }
})

defineEmits(['select'])

const statItems = computed(() => [
  { key: 'total', label: '合同总数', value: props.stats.total, hint: '查看全部', tone: 'blue', icon: Files },
  { key: 'pendingApproval', label: '待审批', value: props.stats.pendingApproval, hint: '内部审批', tone: 'amber', icon: Checked },
  { key: 'executing', label: '履约中', value: props.stats.executing, hint: '付款/验收', tone: 'cyan', icon: Timer },
  { key: 'pendingPayment', label: '待付款', value: props.stats.pendingPayment, hint: '未结清', tone: 'rose', icon: Wallet },
  { key: 'expiringSoon', label: '30天内到期', value: props.stats.expiringSoon, hint: '需跟进', tone: 'emerald', icon: Bell }
])
</script>

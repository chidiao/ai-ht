<template>
  <el-dialog v-model="visible" :title="action?.label" width="460px">
    <el-form :model="actionForm" label-width="96px">
      <el-form-item label="操作人">
        <el-input v-model="actionForm.operator" />
      </el-form-item>
      <el-form-item v-if="action?.value === 'REGISTER_PAYMENT'" label="已付金额">
        <el-input-number v-model="actionForm.paidAmount" :min="0" :max="contract?.amount || 0" :precision="2" :step="1000" />
      </el-form-item>
      <el-form-item label="意见">
        <el-input v-model="actionForm.comment" type="textarea" :rows="3" />
      </el-form-item>
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
const actionForm = reactive({ operator: '', comment: '', paidAmount: 0 })

watch(
  () => [visible.value, props.contract, props.action],
  () => {
    if (visible.value) {
      Object.assign(actionForm, {
        operator: props.operatorName || '',
        comment: '',
        paidAmount: props.action?.value === 'REGISTER_PAYMENT' ? Number(props.contract?.amount || 0) : 0
      })
    }
  },
  { immediate: true }
)

function submit() {
  emit('submit', { ...actionForm })
}
</script>

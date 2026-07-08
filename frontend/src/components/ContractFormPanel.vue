<template>
  <section class="content-panel">
    <el-form ref="contractFormRef" :model="contractForm" :rules="rules" label-width="96px">
      <div class="form-grid">
        <el-form-item label="合同编号" prop="contractNo">
          <el-input v-model="contractForm.contractNo" />
        </el-form-item>
        <el-form-item label="合同名称" prop="name">
          <el-input v-model="contractForm.name" />
        </el-form-item>
        <el-form-item label="供应商" prop="supplierName">
          <el-input v-model="contractForm.supplierName" />
        </el-form-item>
        <el-form-item label="负责人" prop="owner">
          <el-input v-model="contractForm.owner" />
        </el-form-item>
        <el-form-item label="所属部门">
          <el-input v-model="contractForm.department" />
        </el-form-item>
        <el-form-item label="采购品类">
          <el-input v-model="contractForm.category" />
        </el-form-item>
        <el-form-item label="合同金额" prop="amount">
          <el-input-number v-model="contractForm.amount" :min="0" :precision="2" :step="1000" controls-position="right" />
        </el-form-item>
        <el-form-item label="签订日期">
          <el-date-picker v-model="contractForm.signDate" value-format="YYYY-MM-DD" type="date" />
        </el-form-item>
        <el-form-item label="生效日期">
          <el-date-picker v-model="contractForm.effectiveDate" value-format="YYYY-MM-DD" type="date" />
        </el-form-item>
        <el-form-item label="到期日期" prop="expiryDate">
          <el-date-picker v-model="contractForm.expiryDate" value-format="YYYY-MM-DD" type="date" />
        </el-form-item>
      </div>
      <el-form-item label="备注">
        <el-input v-model="contractForm.remark" type="textarea" :rows="4" />
      </el-form-item>
    </el-form>

    <div class="page-actions">
      <el-button v-if="allowMock" @click="fillMockData">填充模拟数据</el-button>
      <el-button @click="$emit('cancel')">取消</el-button>
      <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
    </div>
  </section>
</template>

<script setup>
import { reactive, ref, watch } from 'vue'

const props = defineProps({
  initialValue: {
    type: Object,
    default: null
  },
  saving: {
    type: Boolean,
    default: false
  },
  allowMock: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['save', 'cancel'])
const contractFormRef = ref(null)
const contractForm = reactive(emptyContractForm())

const rules = {
  contractNo: [{ required: true, message: '请输入合同编号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入合同名称', trigger: 'blur' }],
  supplierName: [{ required: true, message: '请输入供应商', trigger: 'blur' }],
  owner: [{ required: true, message: '请输入负责人', trigger: 'blur' }],
  amount: [{ required: true, message: '请输入合同金额', trigger: 'blur' }],
  expiryDate: [{ required: true, message: '请选择到期日期', trigger: 'change' }]
}

watch(
  () => props.initialValue,
  () => Object.assign(contractForm, emptyContractForm(), props.initialValue || {}),
  { immediate: true }
)

async function submit() {
  const valid = await contractFormRef.value.validate().catch(() => false)
  if (valid) {
    emit('save', { ...contractForm })
  }
}

function emptyContractForm() {
  return {
    contractNo: '',
    name: '',
    supplierName: '',
    owner: '',
    department: '',
    category: '',
    amount: 0,
    signDate: '',
    effectiveDate: '',
    expiryDate: '',
    remark: ''
  }
}

function fillMockData() {
  const seed = Date.now().toString().slice(-5)
  Object.assign(contractForm, {
    contractNo: `PO-2026-${seed}`,
    name: '研发测试设备采购合同',
    supplierName: '苏州启明智能设备有限公司',
    owner: '王敏',
    department: '研发部',
    category: '测试设备',
    amount: 126800,
    signDate: formatDate(new Date()),
    effectiveDate: formatDate(new Date()),
    expiryDate: formatDate(addDays(new Date(), 180)),
    remark: '用于研发实验室测试设备采购，含安装调试和一年质保服务。'
  })
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

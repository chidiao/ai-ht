<template>
  <section class="content-panel">
    <el-form ref="contractFormRef" :model="contractForm" :rules="rules" label-width="112px">
      <h3>基础信息</h3>
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
        <el-form-item label="采购内容">
          <el-input v-model="contractForm.purchaseContent" />
        </el-form-item>
        <el-form-item label="采购数量">
          <el-input v-model="contractForm.purchaseQuantity" />
        </el-form-item>
        <el-form-item label="采购方式">
          <el-select v-model="contractForm.purchaseMethod" clearable>
            <el-option label="询价采购" value="询价采购" />
            <el-option label="竞争性谈判" value="竞争性谈判" />
            <el-option label="单一来源" value="单一来源" />
            <el-option label="框架协议" value="框架协议" />
          </el-select>
        </el-form-item>
        <el-form-item label="合同来源">
          <el-input v-model="contractForm.contractSource" />
        </el-form-item>
      </div>

      <h3>供应商信息</h3>
      <div class="form-grid">
        <el-form-item label="供应商联系人">
          <el-input v-model="contractForm.supplierContact" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="contractForm.supplierPhone" />
        </el-form-item>
      </div>

      <h3>金额与付款</h3>
      <div class="form-grid">
        <el-form-item label="合同金额" prop="amount">
          <el-input-number v-model="contractForm.amount" :min="0" :precision="2" :step="1000" controls-position="right" />
        </el-form-item>
        <el-form-item label="付款方式">
          <el-input v-model="contractForm.paymentMethod" placeholder="例如：30% 首付款，70% 验收款" />
        </el-form-item>
      </div>

      <h3>签署与履约</h3>
      <div class="form-grid">
        <el-form-item label="签署方式">
          <el-select v-model="contractForm.signingMethod" clearable>
            <el-option label="电子签署" value="电子签署" />
            <el-option label="线下盖章" value="线下盖章" />
            <el-option label="混合签署" value="混合签署" />
          </el-select>
        </el-form-item>
        <el-form-item label="交付地点">
          <el-input v-model="contractForm.deliveryLocation" />
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
      <el-form-item label="验收标准">
        <el-input v-model="contractForm.acceptanceCriteria" type="textarea" :rows="3" />
      </el-form-item>
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
    purchaseContent: '',
    purchaseQuantity: '',
    purchaseMethod: '',
    supplierContact: '',
    supplierPhone: '',
    deliveryLocation: '',
    paymentMethod: '',
    acceptanceCriteria: '',
    signingMethod: '',
    contractSource: '',
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
    purchaseContent: '研发实验室自动化测试台及配套夹具',
    purchaseQuantity: '3 套',
    purchaseMethod: '询价采购',
    supplierContact: '张经理',
    supplierPhone: '13800000000',
    deliveryLocation: '研发中心三楼实验室',
    paymentMethod: '30% 预付款，70% 验收通过后支付',
    acceptanceCriteria: '设备数量、型号、安装调试结果符合合同约定，并提供培训和质保资料。',
    signingMethod: '电子签署',
    contractSource: '研发实验室能力建设计划',
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

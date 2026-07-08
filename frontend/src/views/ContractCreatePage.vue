<template>
  <AppLayout narrow>
      <PageHeader
        :title="isEdit ? '编辑合同' : '新建合同'"
        eyebrow="Contract Form"
        :back-to="isEdit ? `/contracts/${id}` : '/contracts'"
        :breadcrumbs="[
          { label: '首页', to: '/contracts' },
          { label: '合同列表', to: '/contracts' },
          { label: isEdit ? '编辑合同' : '新建合同' }
        ]"
      />

      <ContractFormPanel
        :initial-value="contract"
        :saving="saving"
        :allow-mock="!isEdit"
        @save="save"
        @cancel="$router.push(isEdit ? `/contracts/${id}` : '/contracts')"
      />
  </AppLayout>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import AppLayout from '../components/AppLayout.vue'
import ContractFormPanel from '../components/ContractFormPanel.vue'
import PageHeader from '../components/PageHeader.vue'
import { createContract, fetchContractDetail, updateContract } from '../api/contracts'

const props = defineProps({
  id: {
    type: String,
    default: ''
  }
})

const router = useRouter()
const contract = ref(null)
const saving = ref(false)
const isEdit = computed(() => Boolean(props.id))

async function loadContract() {
  if (!isEdit.value) {
    return
  }
  try {
    const data = await fetchContractDetail(props.id)
    contract.value = data.contract
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '合同加载失败')
  }
}

async function save(payload) {
  saving.value = true
  try {
    const saved = isEdit.value ? await updateContract(props.id, payload) : await createContract(payload)
    ElMessage.success('保存成功')
    router.push(`/contracts/${saved.id}`)
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

loadContract()
</script>

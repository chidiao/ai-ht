<template>
  <section class="query-panel">
    <el-form :model="filters" label-width="82px">
      <div class="query-grid">
        <el-form-item label="关键词">
          <el-input v-model="filters.keyword" clearable placeholder="编号 / 名称" />
        </el-form-item>
        <el-form-item label="供应商">
          <el-input v-model="filters.supplierName" clearable placeholder="供应商名称" />
        </el-form-item>
        <el-form-item label="负责人">
          <el-input v-model="filters.owner" clearable placeholder="采购负责人" />
        </el-form-item>
        <el-form-item label="部门">
          <el-input v-model="filters.department" clearable placeholder="经办部门" />
        </el-form-item>
        <el-form-item label="采购品类">
          <el-input v-model="filters.category" clearable placeholder="设备 / 服务 / 耗材" />
        </el-form-item>
        <el-form-item label="合同状态">
          <el-select v-model="filters.statuses" multiple collapse-tags collapse-tags-tooltip clearable placeholder="全部">
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="付款状态">
          <el-select v-model="filters.paymentStatus" clearable placeholder="全部">
            <el-option v-for="item in paymentOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="归档状态">
          <el-select v-model="filters.archiveStatus" clearable placeholder="全部">
            <el-option v-for="item in archiveOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="金额范围">
          <div class="range-inputs">
            <el-input-number v-model="filters.amountMin" :min="0" :precision="2" controls-position="right" placeholder="最低" />
            <span>-</span>
            <el-input-number v-model="filters.amountMax" :min="0" :precision="2" controls-position="right" placeholder="最高" />
          </div>
        </el-form-item>
        <el-form-item label="签订日期">
          <el-date-picker
            v-model="filters.signRange"
            type="daterange"
            value-format="YYYY-MM-DD"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
          />
        </el-form-item>
        <el-form-item label="到期范围">
          <el-date-picker
            v-model="filters.dueRange"
            type="daterange"
            value-format="YYYY-MM-DD"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
          />
        </el-form-item>
        <el-form-item label="临期合同">
          <el-switch v-model="filters.expiringSoon" active-text="30天内" />
        </el-form-item>
      </div>
      <div class="query-actions">
        <el-button :icon="Search" type="primary" @click="$emit('search')">查询</el-button>
        <el-button :icon="Refresh" @click="$emit('reset')">重置</el-button>
      </div>
    </el-form>
  </section>
</template>

<script setup>
import { Refresh, Search } from '@element-plus/icons-vue'
import { archiveOptions, paymentOptions, statusOptions } from '../constants/contracts'

defineEmits(['search', 'reset'])

const filters = defineModel({ required: true })
</script>

<style scoped>
.range-inputs {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto minmax(0, 1fr);
  gap: 8px;
  width: 100%;
  align-items: center;
}
</style>

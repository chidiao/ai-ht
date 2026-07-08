<template>
  <section class="table-panel">
    <el-table v-loading="loading" :data="contracts" border height="520">
      <el-table-column prop="contractNo" label="合同编号" width="140" fixed />
      <el-table-column prop="name" label="合同名称" min-width="190" show-overflow-tooltip />
      <el-table-column prop="supplierName" label="供应商" min-width="190" show-overflow-tooltip />
      <el-table-column prop="category" label="采购品类" width="120" show-overflow-tooltip />
      <el-table-column prop="amount" label="金额" width="120">
        <template #default="{ row }">{{ money(row.amount) }}</template>
      </el-table-column>
      <el-table-column prop="paidAmount" label="已付" width="120">
        <template #default="{ row }">{{ money(row.paidAmount) }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="128">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="paymentStatus" label="付款" width="112">
        <template #default="{ row }">
          <el-tag :type="paymentType(row.paymentStatus)" effect="plain">{{ paymentLabel(row.paymentStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="owner" label="负责人" width="100" />
      <el-table-column prop="expiryDate" label="到期日期" width="130">
        <template #default="{ row }">
          <span :class="{ danger: isExpiring(row.expiryDate) }">{{ row.expiryDate }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="190" fixed="right">
        <template #default="{ row }">
          <div class="row-actions">
            <el-button link type="primary" @click="$emit('detail', row)">详情</el-button>
            <el-button v-if="canEditRow(row)" link type="primary" @click="$emit('edit', row)">编辑</el-button>
            <el-button v-if="canDeleteRow(row)" link type="danger" @click="$emit('delete', row)">删除</el-button>
          </div>
        </template>
      </el-table-column>
      <template #empty>
        <div class="table-empty-state">
          <el-icon><Document /></el-icon>
          <strong>暂无合同数据</strong>
          <span>可以调整查询条件，或在合同管理中创建新的采购合同。</span>
        </div>
      </template>
    </el-table>
  </section>
</template>

<script setup>
import { Document } from '@element-plus/icons-vue'
import {
  isExpiring,
  money,
  paymentLabel,
  paymentType,
  statusLabel,
  statusType
} from '../utils/contractFormat'

defineProps({
  contracts: {
    type: Array,
    required: true
  },
  loading: {
    type: Boolean,
    default: false
  },
  canDeleteRow: {
    type: Function,
    default: () => false
  },
  canEditRow: {
    type: Function,
    default: () => false
  }
})

defineEmits(['detail', 'edit', 'delete'])
</script>

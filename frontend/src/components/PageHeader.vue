<template>
  <header class="page-head">
    <div>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item v-for="item in breadcrumbs" :key="item.label" :to="item.to">
          {{ item.label }}
        </el-breadcrumb-item>
      </el-breadcrumb>
      <p class="eyebrow">{{ eyebrow }}</p>
      <h1>{{ title }}</h1>
    </div>
    <div class="page-actions inline">
      <el-button v-if="backTo" :icon="ArrowLeft" @click="$router.push(backTo)">返回</el-button>
      <slot />
    </div>
  </header>
</template>

<script setup>
import { ArrowLeft } from '@element-plus/icons-vue'

defineProps({
  title: {
    type: String,
    required: true
  },
  eyebrow: {
    type: String,
    default: ''
  },
  breadcrumbs: {
    type: Array,
    default: () => []
  },
  backTo: {
    type: String,
    default: ''
  }
})
</script>

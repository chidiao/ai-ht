<template>
  <aside class="sidenav">
    <el-menu :default-active="$route.path" router>
      <el-menu-item index="/dashboard">
        <el-icon><DataBoard /></el-icon>
        <span>首页</span>
      </el-menu-item>
      <el-menu-item index="/contracts">
        <el-icon><Document /></el-icon>
        <span>合同列表</span>
      </el-menu-item>
      <el-menu-item index="/contracts/new">
        <el-icon><CirclePlus /></el-icon>
        <span>新建合同</span>
      </el-menu-item>
    </el-menu>
  </aside>
</template>

<script setup>
import { CirclePlus, DataBoard, Document } from '@element-plus/icons-vue'
</script>

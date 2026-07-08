<template>
  <aside class="sidenav">
    <el-menu :default-active="$route.path" router>
      <el-menu-item index="/dashboard">
        <el-icon><DataBoard /></el-icon>
        <span>首页</span>
      </el-menu-item>
      <el-menu-item index="/contracts">
        <el-icon><Document /></el-icon>
        <span>合同管理</span>
      </el-menu-item>
      <el-menu-item v-if="canAccess(['APPROVER', 'ADMIN'])" index="/approval">
        <el-icon><Stamp /></el-icon>
        <span>审批中心</span>
      </el-menu-item>
      <el-menu-item v-if="canAccess(['FINANCE', 'ADMIN'])" index="/finance">
        <el-icon><Money /></el-icon>
        <span>财务管理</span>
      </el-menu-item>
      <el-menu-item v-if="canAccess(['ACCEPTOR', 'ADMIN'])" index="/fulfillment">
        <el-icon><Checked /></el-icon>
        <span>履约验收</span>
      </el-menu-item>
      <el-menu-item v-if="canAccess(['ARCHIVIST', 'ADMIN'])" index="/archive">
        <el-icon><FolderChecked /></el-icon>
        <span>档案管理</span>
      </el-menu-item>
    </el-menu>
    <a class="resume-link" href="https://me.chidiao.xin/" target="_blank" rel="noopener noreferrer">
      <el-icon><User /></el-icon>
      <span>个人简历</span>
    </a>
  </aside>
</template>

<script setup>
import { Checked, DataBoard, Document, FolderChecked, Money, Stamp, User } from '@element-plus/icons-vue'
import { useSession } from '../stores/session'

const { currentRole } = useSession()

function canAccess(roles) {
  return roles.includes(currentRole.value)
}
</script>

<template>
  <header class="topnav">
    <button class="brand" @click="$router.push('/dashboard')">
      <span class="brand-mark">CM</span>
      <div>
        <strong>采购合同管理系统</strong>
        <p>Contract Management</p>
      </div>
    </button>

    <el-dropdown trigger="click" @command="handleCommand">
      <button class="avatar-switch">
        <el-avatar :size="34">{{ currentRoleLabel.slice(0, 1) }}</el-avatar>
        <span>{{ currentUserName }} · {{ currentRoleLabel }}</span>
      </button>
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item v-for="item in roleOptions" :key="item.value" :command="item.value">
            切换为{{ item.label }}
          </el-dropdown-item>
          <el-dropdown-item divided command="LOGOUT">退出登录</el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </header>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { roleOptions, useSession } from '../stores/session'

const router = useRouter()
const { currentRoleLabel, currentUserName, logout, switchRole } = useSession()

function handleCommand(command) {
  if (command === 'LOGOUT') {
    logout()
    router.push('/login')
    return
  }
  switchRole(command)
  router.go(0)
}
</script>

<template>
  <main class="login-page">
    <section class="login-panel bg-white/88">
      <p class="eyebrow">Contract Management</p>
      <h1>选择身份登录</h1>
      <p class="login-copy">本演示系统使用简化身份，不需要账号密码。</p>

      <div class="role-grid">
        <button v-for="item in roleOptions" :key="item.value" class="role-card" @click="loginAs(item.value)">
          <strong>{{ item.userName }} · {{ item.label }}</strong>
          <span>{{ item.description }}</span>
        </button>
      </div>
    </section>
  </main>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { roleOptions, useSession } from '../stores/session'

const router = useRouter()
const { login } = useSession()

function loginAs(role) {
  login(role)
  router.push('/contracts')
}
</script>

<template>
  <main>
    <AppTopNav />
    <AppSideNav />
    <section class="page-shell" :class="{ narrow }">
      <div class="page-surface bg-white/82">
        <slot />
      </div>
    </section>
  </main>
</template>

<script setup>
import AppSideNav from './AppSideNav.vue'
import AppTopNav from './AppTopNav.vue'

defineProps({
  narrow: {
    type: Boolean,
    default: false
  }
})
</script>

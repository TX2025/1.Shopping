<template>
  <div class="main-layout">
    <AppHeader />
    <div class="main-content">
      <router-view />
    </div>
    <AppFooter />
  </div>
</template>

<script setup>
import AppHeader from '../components/Header.vue'
import AppFooter from '../components/Footer.vue'
</script>

<style scoped>
.main-content { padding-top: 64px; min-height: calc(100vh - 200px); }
</style>

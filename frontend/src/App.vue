<script setup lang="ts">
import { RouterLink, RouterView } from 'vue-router'
import HelloWorld from './components/HelloWorld.vue'
import HeaderView from '@/views/HeaderView.vue'
</script>

<template>
  <HeaderView/>
  <div class="content">
    <RouterView />
  </div>
</template>

<style scoped>
.content {
  margin: 0 16px;
}
</style>

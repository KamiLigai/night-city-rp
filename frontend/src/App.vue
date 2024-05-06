<script setup lang="ts">
import { RouterLink, RouterView } from 'vue-router'
import HelloWorld from './components/HelloWorld.vue'
import HeaderView from '@/views/HeaderView.vue'
</script>

<template>
  <HeaderView/>
  <RouterView />
</template>

<style scoped>

</style>

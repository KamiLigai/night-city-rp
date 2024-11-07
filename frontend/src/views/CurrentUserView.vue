<script setup lang="ts">

import {onMounted, ref} from 'vue'
import client from '@/Clients/Client'
import {toast} from 'vue3-toastify'
import type {UserDto} from "@/dto/UserDto";

const user = ref<UserDto>()

onMounted(() => {
  client.getCurrentUser()
      .then(response => user.value = response.data)
      .catch(() => toast('Ошибка запроса персонажа', {type: toast.TYPE.ERROR}))
})
</script>

<template>
  <h1>Текущий юзер</h1>
  <p>ID: {{ user?.id }}</p>
  <p>Юзернейм: {{ user?.username }}</p>
  <p>Роли: {{ user?.roles.join(", ") }}</p>
</template>

<style scoped>

</style>
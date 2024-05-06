<script setup lang="ts">
import { computed, ref } from 'vue'
import client from '@/Clients/Client'
import router from '@/router'

const username = ref<string>()
const password = ref<string>()

function register(): void {
  client.createUser(username.value!, password.value!)
    .then(() => router.push({name: "login"}))
    .catch(() => console.error("registration failed"))
}

const inputIsValid = computed(() => {
  return username.value
    && username.value.length > 3
    && password.value
    && password.value.length > 3
})
</script>

<template>
  <div class="container">
    <h1>Регистрация</h1>
    <input v-model="username" placeholder="логин">
    <input v-model="password" placeholder="пароль">
    <button v-on:click="register" :disabled="!inputIsValid">Зарегистрироваться</button>
  </div>
</template>

<style>
.container {
  display: flex;
  flex-direction: column;
  align-items: center
}
</style>

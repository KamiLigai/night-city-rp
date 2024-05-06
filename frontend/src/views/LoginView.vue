<script setup lang="ts">
import client from '@/client'
import { ref } from 'vue'

const username = ref<string>()
const password = ref<string>()

function enter() {
  client.login(username.value!, password.value!)
    .then(() => console.log("login success"))
    .catch(() => console.log("login failed"))
}
</script>

<template>
  <div class="container">
    <h1>Вход</h1>
    <input v-model="username" placeholder="логин">
    <input v-model="password" placeholder="пароль">
    <button v-on:click="enter">Войти</button>
  </div>
</template>

<style>
.container {
  display: flex;
  flex-direction: column;
  align-items: center
}
</style>

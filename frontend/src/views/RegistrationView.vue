<script setup lang="ts">
import {computed, onMounted, ref} from 'vue'
import client from '@/Clients/Client'
import router from '@/router'
import {toast} from 'vue3-toastify'

const username = ref<string>()
const password = ref<string>()

const usernameInput = ref<HTMLInputElement>()

function register(): void {
  client.createUser(username.value!, password.value!)
      .then(() => router.push({name: 'login', force: true})
          .then(() => toast('Новый юзер создан', {type: toast.TYPE.SUCCESS}))
      ).catch(() => toast('Не удалось зарегистрироваться', {type: toast.TYPE.ERROR}))
}

onMounted(() => {
  usernameInput.value?.focus()
})

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
    <input v-model="username" placeholder="логин" ref="usernameInput">
    <input v-model="password" placeholder="пароль" type="password" v-on:keydown.enter="register">
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

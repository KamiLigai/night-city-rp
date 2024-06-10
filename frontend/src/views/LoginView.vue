<script setup lang="ts">
import { computed, ref } from 'vue'
import router from '@/router'
import client from '@/Clients/Client'
import { toast } from 'vue3-toastify'

const username = ref<string>()
const password = ref<string>()

function enter() {
    client.login(username.value!, password.value!)
        .then(() => router.push({ name: 'home' }))
        .catch(() => toast('Не удалось войти', { type: toast.TYPE.ERROR }))
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
        <h1>Вход</h1>
        <input v-model="username" placeholder="логин">
        <input v-model="password" placeholder="пароль">
        <button v-on:click="enter" :disabled="!inputIsValid">Войти</button>
    </div>
</template>

<style>
.container {
    display: flex;
    flex-direction: column;
    align-items: center
}
</style>

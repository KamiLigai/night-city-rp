<script setup lang="ts">
import { useUserStore } from '@/stores/userStore'
import router from '@/router'
import client from '@/Clients/Client'
import { onMounted } from 'vue'
import { Base64 } from 'js-base64'

const userStore = useUserStore()

function logout() {
    client.logout()
    router.push({ name: "login" })
}

</script>

<template>
    <div class="container">
        <h1>Night City RP</h1>
        <router-link :to="{name: 'home'}">Главная</router-link>
        <router-link v-if="userStore.user?.username" :to="{name: 'characters'}">Персонажи</router-link>
        <router-link v-if="!userStore.user?.username" :to="{name: 'login'}">Войти</router-link>
        <router-link v-if="!userStore.user?.username" :to="{name: 'registration'}">Зарегистрироваться</router-link>
        <p v-if="userStore.user?.username">{{ userStore.user?.username }}</p>
        <button v-if="userStore.user?.username" v-on:click="logout">Выйти</button>
    </div>

</template>

<style scoped>
.container {
    display: flex;
    flex-direction: row;
    gap: 24px;
}

</style>

<script setup lang="ts">
import {useUserStore} from '@/stores/userStore'
import router from '@/router'
import client from '@/Clients/Client'
import {computed, type Ref} from 'vue'
import type {UserDto} from "@/dto/users/UserDto";

const userStore = useUserStore()

const userLoggedIn: Ref<boolean> = computed(() => !!userStore.user?.username)

const userNotLoggedIn: Ref<boolean> = computed(() => !userLoggedIn.value)

const user: Ref<UserDto | null> = computed(() => userStore.user)

function logout() {
  client.logout()
  router.push({name: "login"})
}

</script>

<template>
  <div class="container">
    <h1>Night City RP</h1>
    <router-link :to="{name: 'home'}">Главная</router-link>
    <router-link v-if="userNotLoggedIn" :to="{name: 'login'}">Войти</router-link>
    <router-link v-if="userNotLoggedIn" :to="{name: 'registration'}">Зарегистрироваться</router-link>
    <router-link v-if="userLoggedIn" :to="{name: 'characters'}">Персонажи</router-link>
    <router-link v-if="userLoggedIn" :to="{name: 'implants'}">Импланты</router-link>
    <router-link v-if="userLoggedIn" :to="{name: 'skills'}">Навыки</router-link>
    <router-link v-if="userLoggedIn" :to="{name: 'weapons'}">Оружие</router-link>
    <router-link v-if="userLoggedIn" :to="{name: 'current-user'}">{{ user?.username }}</router-link>
    <button v-if="userLoggedIn" v-on:click="logout">Выйти</button>
  </div>

</template>

<style scoped>
.container {
  display: flex;
  flex-direction: row;
  gap: 24px;
  margin: 0 16px ;
}

</style>

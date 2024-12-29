<script setup lang="ts">

import {onMounted, ref} from 'vue'
import client from '@/Clients/Client'
import {toast} from 'vue3-toastify'
import type {Page} from "@/dto/Page";
import type {ImplantDto} from "@/dto/implants/ImplantDto";
import type {WeaponDto} from "@/dto/weapons/WeaponDto";

const page = ref<Page<WeaponDto>>()
const currentPage = ref(0)
const size = ref(3)

function goToPreviousPage() {
    if (currentPage.value <= 0) {
        return
    }
    currentPage.value -= 1
    reloadWeapons()
}

function goToNextPage() {
    if (page.value?.last) {
        return
    }
    currentPage.value += 1
    reloadWeapons()
}

function reloadWeapons() {
    client.getWeapons(currentPage.value, size.value)
        .then(response => page.value = response.data)
        .catch(() => toast('Ошибка запроса оружия', { type: toast.TYPE.ERROR }))
}


onMounted(() => {
    reloadWeapons()
})

</script>

<template>
    <h1>Оружие</h1>
    <div v-for="weapon in page?.content" :key="weapon.id">
        <router-link :to="{name: 'weapon', params: { weaponId: weapon.id }}">{{ weapon.name }}</router-link>
    </div>
    <div class="buttons-container">
        <button v-on:click="goToPreviousPage" :disabled="currentPage <= 0">&lt;</button>
        <p>{{ currentPage + 1 }}</p>
        <button v-on:click="goToNextPage" :disabled="page?.last">&gt;</button>
    </div>

    <br>
    <router-link :to="{name: 'create-weapon'}">Создать оружие</router-link>
</template>

<style scoped>
.buttons-container {
    display: flex;
    gap: 12px;
}
</style>
<script setup lang="ts">

import {onMounted, ref} from 'vue'
import client from '@/Clients/Client'
import {toast} from 'vue3-toastify'
import type {Page} from "@/dto/Page";
import type {ImplantDto} from "@/dto/implants/ImplantDto";

const page = ref<Page<ImplantDto>>()
const currentPage = ref(0)
const size = ref(3)

function goToPreviousPage() {
    if (currentPage.value <= 0) {
        return
    }
    currentPage.value -= 1
    reloadImplants()
}

function goToNextPage() {
    if (page.value?.last) {
        return
    }
    currentPage.value += 1
    reloadImplants()
}

function reloadImplants() {
    client.getImplants(currentPage.value, size.value)
        .then(response => page.value = response.data)
        .catch(() => toast('Ошибка запроса имплантов', { type: toast.TYPE.ERROR }))
}


onMounted(() => {
    reloadImplants()
})

</script>

<template>
    <h1>Импланты</h1>
    <div v-for="implant in page?.content" :key="implant.id">
        <router-link :to="{name: 'implant', params: { implantId: implant.id }}">{{ implant.name }}</router-link>
    </div>
    <div class="buttons-container">
        <button v-on:click="goToPreviousPage" :disabled="currentPage <= 0">&lt;</button>
        <p>{{ currentPage + 1 }}</p>
        <button v-on:click="goToNextPage" :disabled="page?.last">&gt;</button>
    </div>

    <br>
    <router-link :to="{name: 'create-implant'}">Создать имплант</router-link>
</template>

<style scoped>
.buttons-container {
    display: flex;
    gap: 12px;
}
</style>
<script setup lang="ts">

import {onMounted, ref} from 'vue'
import client from '@/Clients/Client'
import {toast} from 'vue3-toastify'
import type {Page} from "@/dto/Page";
import type {SkillDto} from "@/dto/skills/SkillDto";

const page = ref<Page<SkillDto>>()
const currentPage = ref(0)
const size = ref(3)

function goToPreviousPage() {
    if (currentPage.value <= 0) {
        return
    }
    currentPage.value -= 1
    reloadSkills()
}

function goToNextPage() {
    if (page.value?.last) {
        return
    }
    currentPage.value += 1
    reloadSkills()
}

function reloadSkills() {
    client.getSkills(currentPage.value, size.value)
        .then(response => page.value = response.data)
        .catch(() => toast('Ошибка запроса навыков', { type: toast.TYPE.ERROR }))
}


onMounted(() => {
    reloadSkills()
})

</script>

<template>
    <h1>Навыки</h1>
    <div v-for="skill in page?.content" :key="skill.id">
        <router-link :to="{name: 'skill', params: { skillId: skill.id }}">{{ skill.name }}</router-link>
    </div>
    <div class="buttons-container">
        <button v-on:click="goToPreviousPage" :disabled="currentPage <= 0">&lt;</button>
        <p>{{ currentPage + 1 }}</p>
        <button v-on:click="goToNextPage" :disabled="page?.last">&gt;</button>
    </div>

    <br>
    <router-link :to="{name: 'create-skill'}">Создать навык</router-link>
</template>

<style scoped>
.buttons-container {
    display: flex;
    gap: 12px;
}
</style>
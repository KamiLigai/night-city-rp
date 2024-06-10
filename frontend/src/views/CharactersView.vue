<script setup lang="ts">

import { onMounted, ref } from 'vue'
import client from '@/Clients/Client'
import { CharactersPage } from '@/dto/CharactersPage'
import { toast } from 'vue3-toastify'

const charactersResponse = ref<CharactersPage>()
const page = ref(0)
const size = ref(3)

function goToPreviousPage() {
    if (page.value <= 0) {
        return
    }
    page.value -= 1
    reloadCharacters()
}

function goToNextPage() {
    if (charactersResponse.value?.last) {
        return
    }
    page.value += 1
    reloadCharacters()
}

function reloadCharacters() {
    client.getCharacters(page.value, size.value)
        .then(response => charactersResponse.value = response.data)
        .catch(() => toast('Ошибка запроса персонажей', { type: toast.TYPE.ERROR }))
}


onMounted(() => {
    reloadCharacters()
})

</script>

<template>
    <h1>Персонажи</h1>
    <div v-for="character in charactersResponse?.content">
        <router-link :to="{name: 'character', params: { characterId: character.id }}">{{ character.name }}</router-link>
    </div>
    <div class="buttons-container">
        <button v-on:click="goToPreviousPage" :disabled="page <= 0">&lt;</button>
        <p>{{ page + 1 }}</p>
        <button v-on:click="goToNextPage" :disabled="charactersResponse?.last">&gt;</button>
    </div>

    <br>
    <router-link :to="{name: 'create-character'}">Создать персонажа</router-link>
</template>

<style scoped>
.buttons-container {
    display: flex;
    gap: 12px;
}
</style>
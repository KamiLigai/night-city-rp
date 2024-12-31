<script setup lang="ts">

import client from '@/Clients/Client'
import { ref } from 'vue'
import { CreateCharacterRequest } from '@/dto/characters/CreateCharacterRequest'
import { useRouter } from 'vue-router'
import { toast } from 'vue3-toastify'

const router = useRouter()
const request = ref<CreateCharacterRequest>(new CreateCharacterRequest())

function createCharacter() {
    client.createCharacter(request.value!)
        .then(response => router.push({ name: 'character', force: true, params: { characterId: response.data.id } }))
        .catch(() => toast('Не удалось создать персонажа', { type: toast.TYPE.ERROR }))
}
</script>

<template>
    <div class="container">
        <h1>Создать персонажа</h1>
        <input class="item" placeholder="Имя" v-model="request.name">
        <input class="item" placeholder="Возраст" type="number" v-model="request.age" />
        <input class="item" placeholder="Репутация" type="number" v-model="request.reputation" />
        <button class="item" v-on:click="createCharacter">Создать</button>
    </div>
</template>

<style scoped>
.container {
    display: flex;
    flex-direction: column;
    align-items: center
}
.container {
    display: flex;
    flex-direction: column;
    align-items: center;
}

.item {
    margin: 4px;
}
</style>
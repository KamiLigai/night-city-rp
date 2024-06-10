<script setup lang="ts">

import { onMounted, ref } from 'vue'
import { CharacterDto } from '@/dto/CharacterDto'
import client from '@/Clients/Client'
import { useRoute } from 'vue-router'
import { toast } from 'vue3-toastify'

const route = useRoute()
const character = ref<CharacterDto>()

onMounted(() => {

    const id = route.params.characterId as string
    client.getCharacter(id)
        .then(response => character.value = response.data)
        .catch(() => toast('Ошибка запроса персонажа', { type: toast.TYPE.ERROR }))
})
</script>

<template>
    <h1>Персонаж</h1>
    <p>Имя: {{ character?.name }}</p>
    <p>Возраст: {{ character?.age }}</p>
    <p>Владелец: -</p>
</template>

<style scoped>

</style>
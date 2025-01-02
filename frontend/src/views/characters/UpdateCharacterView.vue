<script setup lang="ts">

import client from '@/Clients/Client'
import {computed, ref} from 'vue'
import {UpdateCharacterRequest} from '@/dto/characters/UpdateCharacterRequest'
import {useRoute, useRouter} from 'vue-router'
import {toast} from 'vue3-toastify'

const route = useRoute()
const router = useRouter()
const request = ref<UpdateCharacterRequest>(new UpdateCharacterRequest())

const characterId = computed(() => route.params.characterId as string)

function updateCharacter() {
  client.updateCharacter(characterId.value, request.value!)
      .then(() => router.push({name: 'character', params: {characterId: characterId.value}})
          .then(() => toast('Персонаж изменен', {type: toast.TYPE.SUCCESS}))
      ).catch(() => toast('Не удалось изменить персонажа', {type: toast.TYPE.ERROR}))
}
</script>

<template>
  <div class="container">
    <h1>Обновить персонажа</h1>
    <input class="item" placeholder="Имя" v-model="request.name">
    <input class="item" placeholder="Возраст" type="number" v-model="request.age"/>
    <input class="item" placeholder="Репутация" type="number" v-model="request.reputation"/>
    <button class="item" v-on:click="updateCharacter">Сохранить</button>
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
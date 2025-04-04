<script setup lang="ts">

import {computed, onMounted, ref, watch} from "vue";
import {useRoute} from "vue-router";
import {CharacterDto} from "@/dto/characters/CharacterDto";
import client from "@/Clients/Client";
import {toast} from "vue3-toastify";
import router from "@/router";
import type {ImplantDto} from "@/dto/implants/ImplantDto";

const route = useRoute()
const character = ref<CharacterDto>()
const allImplantIds = ref<string[]>()
const assignedImplants = ref<string[]>([])
const implants = ref<ImplantDto[]>()

const notAssignedImplants = computed(() =>
    allImplantIds.value?.filter(implantId => !assignedImplants.value.includes(implantId))
)

const characterId = computed<string>(() => route.params.characterId as string)

onMounted(() => {
  loadCharacter()
  loadImplantIds()
})

watch(allImplantIds, () => loadImplants())

function loadCharacter() {
  client.getCharacter(characterId.value)
      .then(response => {
        character.value = response.data
        assignedImplants.value = [...character.value.implantIds ?? []]
      }).catch(() => toast('Ошибка запроса персонажа', {type: toast.TYPE.ERROR}))
}

function loadImplantIds() {
  client.getImplantIds()
      .then(response => {
        allImplantIds.value = response.data
      }).catch(() => toast('Ошибка запроса имплантов', {type: toast.TYPE.ERROR}))
}

function assignImplant(implantId: string) {
  assignedImplants.value.push(implantId)
}

function unassignImplant(implantId: string) {
  assignedImplants.value = assignedImplants.value.filter(assignedImplantId => assignedImplantId !== implantId);
}

function saveImplants() {
  client.updateCharacterImplants(characterId.value, assignedImplants.value)
      .then(() => router.push({name: 'character', params: {characterId: characterId.value}})
          .then(() => toast('Импланты обновлено', {type: toast.TYPE.SUCCESS}))
      ).catch(() => toast('Ошибка сохранения имплантов', {type: toast.TYPE.ERROR}))
}

function resetImplants() {
  assignedImplants.value = [...character.value?.implantIds ?? []]
}

function goToCharacter() {
  router.push({name: 'character', params: {characterId: characterId.value}})
}

function toImplantLabel(implantId: string) {
  return (implants.value ?? []).find(implant => implant.id == implantId)?.name ?? "-"
}

function loadImplants() {
  client.getImplantsBulk(allImplantIds.value ?? [])
      .then(response => implants.value = response.data)
      .catch(() => toast('Ошибка запроса имплантов', {type: toast.TYPE.ERROR}))
}
</script>

<template>
  <h1>Импланты персонажа {{ character?.name ?? '-' }}</h1>
  <ul>
    <li v-for="implant in assignedImplants" :key="implant">
      {{ toImplantLabel(implant) }}
      <button v-on:click="unassignImplant(implant)" class="close-button">X</button>
    </li>
  </ul>
  <button v-on:click="saveImplants">Сохранить</button>
  <button v-on:click="resetImplants">Сбросить</button>
  <button v-on:click="goToCharacter">К персонажу</button>
  <h2>Прочие импланты</h2>
  <ul>
    <li v-for="implantId in notAssignedImplants" :key="implantId" >
      <router-link :to="{name: 'implant', params: { implantId: implantId }}">{{ toImplantLabel(implantId) }}</router-link>
      <button v-on:click="assignImplant(implantId)" class="close-button">+</button>
    </li>
  </ul>
</template>

<style scoped>
.close-button{
  margin: 0 8px;
  border-width: 0;
  cursor: pointer;
  color: grey;
  background-color: white;
}
</style>
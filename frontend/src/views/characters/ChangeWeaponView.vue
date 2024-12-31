<script setup lang="ts">

import {computed, onMounted, ref, watch} from "vue";
import {useRoute} from "vue-router";
import {CharacterDto} from "@/dto/characters/CharacterDto";
import client from "@/Clients/Client";
import {toast} from "vue3-toastify";
import router from "@/router";
import {WeaponDto} from "@/dto/weapons/WeaponDto";

const route = useRoute()
const character = ref<CharacterDto>()
const allWeaponIds = ref<string[]>()
const assignedWeapons = ref<string[]>([])
const weapons = ref<WeaponDto[]>()

const notAssignedWeapons = computed(() =>
    allWeaponIds.value?.filter(weaponId => !assignedWeapons.value.includes(weaponId))
)

const characterId = computed<string>(() => route.params.characterId as string)

onMounted(() => {
  loadCharacter()
  loadWeaponIds()
})

watch(allWeaponIds, () => loadWeapons())

function loadCharacter() {
  client.getCharacter(characterId.value)
      .then(response => {
        character.value = response.data
        assignedWeapons.value = [...character.value.weaponIds ?? []]
      }).catch(() => toast('Ошибка запроса персонажа', {type: toast.TYPE.ERROR}))
}

function loadWeaponIds() {
  client.getWeaponIds()
      .then(response => {
        allWeaponIds.value = response.data
      }).catch(() => toast('Ошибка запроса оружия', {type: toast.TYPE.ERROR}))
}

function assignWeapon(weaponId: string) {
  assignedWeapons.value.push(weaponId)
}

function unassignWeapon(weaponId: string) {
  assignedWeapons.value = assignedWeapons.value.filter(assignedWeaponId => assignedWeaponId !== weaponId);
}

function saveWeapons() {
  client.updateCharacterWeapons(characterId.value, assignedWeapons.value)
      .then(() => router.push({name: 'character', force: true, params: {characterId: characterId.value}})
          .then(() => toast('Оружие обновлено', {type: toast.TYPE.SUCCESS}))
      ).catch(() => toast('Ошибка сохранения оружия', {type: toast.TYPE.ERROR}))
}

function resetWeapons() {
  assignedWeapons.value = [...character.value?.weaponIds ?? []]
}

function goToCharacter() {
  router.push({name: 'character', force: true, params: {characterId: characterId.value}})
}

function toWeaponLabel(weaponId: string) {
  return (weapons.value ?? []).find(weapon => weapon.id == weaponId)?.name ?? "-"
}

function loadWeapons() {
  client.getWeaponsBulk(allWeaponIds.value ?? [])
      .then(response => weapons.value = response.data)
      .catch(() => toast('Ошибка запроса оружия', {type: toast.TYPE.ERROR}))
}
</script>

<template>
  <h1>Оружие персонажа {{ character?.name ?? '-' }}</h1>
  <ul>
    <li v-for="weapon in assignedWeapons" :key="weapon">
      {{ toWeaponLabel(weapon) }}
      <button v-on:click="unassignWeapon(weapon)" class="close-button">X</button>
    </li>
  </ul>
  <button v-on:click="saveWeapons">Сохранить</button>
  <button v-on:click="resetWeapons">Сбросить</button>
  <button v-on:click="goToCharacter">К персонажу</button>
  <h2>Прочее оружие</h2>
  <ul>
    <li v-for="weapon in notAssignedWeapons" :key="weapon" >
      {{ toWeaponLabel(weapon) }}
      <button v-on:click="assignWeapon(weapon)" class="close-button">X</button>
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
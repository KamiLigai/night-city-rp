<script setup lang="ts">

import {computed, onMounted, ref, watch} from 'vue'
import {CharacterDto} from '@/dto/characters/CharacterDto'
import client from '@/Clients/Client'
import {useRoute} from 'vue-router'
import {toast} from 'vue3-toastify'
import {WeaponDto} from "@/dto/weapons/WeaponDto";

const route = useRoute()
const character = ref<CharacterDto>()
const weapons = ref<WeaponDto[]>()

const charId = computed(() => route.params.characterId as string)

onMounted(() => {
  client.getCharacter(charId.value)
      .then(response => character.value = response.data)
      .catch(() => toast('Ошибка запроса персонажа', {type: toast.TYPE.ERROR}))
})

watch(character, () => loadWeapons())

function loadWeapons() {
  client.getWeaponsBulk(character.value?.weaponIds ?? [])
      .then(response => weapons.value = response.data)
      .catch(() => toast('Ошибка запроса оружия', {type: toast.TYPE.ERROR}))
}

function toWeaponLabel(weaponId: string) {
  return (weapons.value ?? []).find(weapon => weapon.id == weaponId)?.name ?? "-"
}
</script>

<template>
  <h1>Персонаж</h1>
  <p>Имя: {{ character?.name }}</p>
  <p>Владелец: {{ character?.ownerId }}</p>
  <p>Возраст: {{ character?.age }}</p>
  <p>Репутация: {{ character?.reputation }}</p>
  <p>Очки Имплантов: {{ character?.implantPoints }}</p>
  <p>Особые Очки Имплантов: {{ character?.specialImplantPoints }}</p>
  <p>Боевые Очки Навыков: {{ character?.battlePoints }}</p>
  <p>Мирные Очки Навыков: {{ character?.civilPoints }}</p>
  <div>
    <p>
      <router-link :to="{name: 'change-character-weapons', params: { characterId: charId }}">Оружие:</router-link>
    </p>
    <ul>
      <li v-for="weaponId in character?.weaponIds" :key="weaponId">{{ toWeaponLabel(weaponId)}}</li>
    </ul>
  </div>
</template>

<style scoped>

</style>
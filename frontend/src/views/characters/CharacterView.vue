<script setup lang="ts">

import {computed, onMounted, ref, watch} from 'vue'
import {CharacterDto} from '@/dto/characters/CharacterDto'
import client from '@/Clients/Client'
import {useRoute} from 'vue-router'
import {toast} from 'vue3-toastify'
import {WeaponDto} from "@/dto/weapons/WeaponDto";
import router from "@/router";
import type {ImplantDto} from "@/dto/implants/ImplantDto";
import type {SkillDto} from "@/dto/skills/SkillDto";

const route = useRoute()
const character = ref<CharacterDto>()
const implants = ref<ImplantDto[]>()
const skills = ref<SkillDto[]>()
const weapons = ref<WeaponDto[]>()

const characterId = computed(() => route.params.characterId as string)

onMounted(() => {
  client.getCharacter(characterId.value)
      .then(response => character.value = response.data)
      .catch(() => toast('Ошибка запроса персонажа', {type: toast.TYPE.ERROR}))
})

watch(character, () => {
  loadImplants();
  loadSkills();
  loadWeapons();
})

function loadImplants() {
  client.getImplantsBulk(character.value?.implantIds ?? [])
      .then(response => implants.value = response.data)
      .catch(() => toast('Ошибка запроса имплантов', {type: toast.TYPE.ERROR}))
}

function loadSkills() {
  client.getSkillsBulk(character.value?.skillIds ?? [])
      .then(response => skills.value = response.data)
      .catch(() => toast('Ошибка запроса навыков', {type: toast.TYPE.ERROR}))
}

function loadWeapons() {
  client.getWeaponsBulk(character.value?.weaponIds ?? [])
      .then(response => weapons.value = response.data)
      .catch(() => toast('Ошибка запроса оружия', {type: toast.TYPE.ERROR}))
}

function toImplantLabel(implantId: string) {
  return (implants.value ?? []).find(implant => implant.id == implantId)?.name ?? "-"
}

function toSkillLabel(skillId: string) {
  console.log(skillId)
  console.log(skills.value)
  return (skills.value ?? []).find(skill => skill.id == skillId)?.name ?? "-"
}

function toWeaponLabel(weaponId: string) {
  return (weapons.value ?? []).find(weapon => weapon.id == weaponId)?.name ?? "-"
}

function goToUpdateCharacter() {
  router.push({name: 'update-character', params: {characterId: characterId.value}})
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
      <router-link :to="{name: 'update-character-implants', params: { characterId: characterId }}">Импланты:</router-link>
    </p>
    <ul>
      <li v-for="implantId in character?.implantIds" :key="implantId">
        <router-link :to="{name: 'implant', params: { implantId: implantId }}">{{ toImplantLabel(implantId) }}</router-link>
      </li>
    </ul>
  </div>
  <div>
    <p>
      <router-link :to="{name: 'update-character-skills', params: { characterId: characterId }}">Навыки:</router-link>
    </p>
    <ul>
      <li v-for="skillId in character?.skillIds" :key="skillId">
        <router-link :to="{name: 'skill', params: { skillId: skillId }}">{{ toSkillLabel(skillId) }}</router-link>
      </li>
    </ul>
  </div>
  <div>
    <p>
      <router-link :to="{name: 'update-character-weapons', params: { characterId: characterId }}">Оружие:</router-link>
    </p>
    <ul>
      <li v-for="weaponId in character?.weaponIds" :key="weaponId">
        <router-link :to="{name: 'weapon', params: { weaponId: weaponId }}">{{ toWeaponLabel(weaponId) }}</router-link>
      </li>
    </ul>
  </div>
  <button v-on:click="goToUpdateCharacter">Изменить</button>
</template>

<style scoped>

</style>
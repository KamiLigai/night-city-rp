<script setup lang="ts">

import {computed, onMounted, ref, watch} from "vue";
import {useRoute} from "vue-router";
import {CharacterDto} from "@/dto/characters/CharacterDto";
import client from "@/Clients/Client";
import {toast} from "vue3-toastify";
import router from "@/router";
import type {SkillDto} from "@/dto/skills/SkillDto";

const route = useRoute()
const character = ref<CharacterDto>()
const allSkillIds = ref<string[]>()
const assignedSkills = ref<string[]>([])
const skills = ref<SkillDto[]>()

const notAssignedSkills = computed(() =>
    allSkillIds.value?.filter(skillId => !assignedSkills.value.includes(skillId))
)

const characterId = computed<string>(() => route.params.characterId as string)

onMounted(() => {
  loadCharacter()
  loadSkillIds()
})

watch(allSkillIds, () => loadSkills())

function loadCharacter() {
  client.getCharacter(characterId.value)
      .then(response => {
        character.value = response.data
        assignedSkills.value = [...character.value.skillIds ?? []]
      }).catch(() => toast('Ошибка запроса навыков', {type: toast.TYPE.ERROR}))
}

function loadSkillIds() {
  client.getSkillIds()
      .then(response => {
        allSkillIds.value = response.data
      }).catch(() => toast('Ошибка запроса навыков', {type: toast.TYPE.ERROR}))
}

function assignSkill(skillId: string) {
  assignedSkills.value.push(skillId)
}

function unassignSkill(skillId: string) {
  assignedSkills.value = assignedSkills.value.filter(assignedSkillId => assignedSkillId !== skillId);
}

function saveSkills() {
  client.updateCharacterSkills(characterId.value, assignedSkills.value)
      .then(() => router.push({name: 'character', params: {characterId: characterId.value}})
          .then(() => toast('Навыки обновлено', {type: toast.TYPE.SUCCESS}))
      ).catch(() => toast('Ошибка сохранения навыков', {type: toast.TYPE.ERROR}))
}

function resetSkills() {
  assignedSkills.value = [...character.value?.skillIds ?? []]
}

function goToCharacter() {
  router.push({name: 'character', params: {characterId: characterId.value}})
}

function toSkillLabel(skillId: string) {
  return (skills.value ?? []).find(skill => skill.id == skillId)?.name ?? "-"
}

function loadSkills() {
  client.getSkillsBulk(allSkillIds.value ?? [])
      .then(response => skills.value = response.data)
      .catch(() => toast('Ошибка запроса навыков', {type: toast.TYPE.ERROR}))
}
</script>

<template>
  <h1>Навыки персонажа {{ character?.name ?? '-' }}</h1>
  <ul>
    <li v-for="skill in assignedSkills" :key="skill">
      {{ toSkillLabel(skill) }}
      <button v-on:click="unassignSkill(skill)" class="close-button">X</button>
    </li>
  </ul>
  <button v-on:click="saveSkills">Сохранить</button>
  <button v-on:click="resetSkills">Сбросить</button>
  <button v-on:click="goToCharacter">К персонажу</button>
  <h2>Прочие навыки</h2>
  <ul>
    <li v-for="skillId in notAssignedSkills" :key="skillId" >
      <router-link :to="{name: 'skill', params: { skillId: skillId }}">{{ toSkillLabel(skillId) }}</router-link>
      <button v-on:click="assignSkill(skillId)" class="close-button">+</button>
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
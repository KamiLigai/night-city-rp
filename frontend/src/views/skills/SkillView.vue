<script setup lang="ts">

import {computed, onMounted, ref} from 'vue'
import client from '@/Clients/Client'
import {useRoute} from 'vue-router'
import {toast} from 'vue3-toastify'
import type {SkillDto} from "@/dto/skills/SkillDto";
import router from "@/router";

const route = useRoute()
const skill = ref<SkillDto>()

const skillId = computed(() => route.params.skillId as string)

onMounted(() => {
  client.getSkill(skillId.value)
      .then(response => skill.value = response.data)
      .catch(() => toast('Ошибка запроса навыка', {type: toast.TYPE.ERROR}))
})

function goToUpdateSkill() {
  router.push({name: 'update-skill', params: {skillId: skillId.value}})
}
</script>

<template>
  <h1>Навык</h1>
  <p>Название: {{ skill?.name }}</p>
  <p>Описание: {{ skill?.description }}</p>
  <p>Уровень: {{ skill?.level }}</p>
  <p>Тип: {{ skill?.type }}</p>
  <p>Стоимость: {{ skill?.cost }}</p>
  <button v-on:click="goToUpdateSkill">Изменить</button>
</template>

<style scoped>

</style>
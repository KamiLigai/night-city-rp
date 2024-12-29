<script setup lang="ts">

import {onMounted, ref} from 'vue'
import client from '@/Clients/Client'
import {useRoute} from 'vue-router'
import {toast} from 'vue3-toastify'
import type {SkillDto} from "@/dto/skills/SkillDto";

const route = useRoute()
const skill = ref<SkillDto>()

onMounted(() => {

  const id = route.params.skillId as string
  client.getSkill(id)
      .then(response => skill.value = response.data)
      .catch(() => toast('Ошибка запроса навыка', {type: toast.TYPE.ERROR}))
})
</script>

<template>
  <h1>Навык</h1>
  <p>Название: {{ skill?.name }}</p>
  <p>Описание: {{ skill?.description }}</p>
  <p>Уровень: {{ skill?.level }}</p>
  <p>Тип: {{ skill?.type }}</p>
  <p>Стоимость: {{ skill?.cost }}</p>
</template>

<style scoped>

</style>
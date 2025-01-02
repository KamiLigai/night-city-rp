<script setup lang="ts">

import client from '@/Clients/Client'
import {computed, onMounted, ref} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {toast} from 'vue3-toastify'
import {UpdateSkillRequest} from "@/dto/skills/UpdateSkillRequest";

const route = useRoute()
const router = useRouter()
const request = ref<UpdateSkillRequest>(new UpdateSkillRequest())

const skillId = computed(() => route.params.skillId as string)

onMounted(() => {
  loadSkill()
})

function loadSkill() {
  client.getSkill(skillId.value)
      .then(response => {
        request.value.name = response.data.name
        request.value.description = response.data.description
      }).catch(() => toast('Не удалось загрузить навык', {type: toast.TYPE.ERROR}))
}

function updateSkill() {
  client.updateSkill(skillId.value, request.value!)
      .then(() => router.push({name: 'skill', params: {skillId: skillId.value}})
          .then(() => toast('Навык изменен', {type: toast.TYPE.SUCCESS}))
      ).catch(() => toast('Не удалось изменить навык', {type: toast.TYPE.ERROR}))
}
</script>

<template>
  <div class="container">
    <h1>Изменить навык</h1>
    <input class="item" placeholder="Название" v-model="request.name">
    <input class="item" placeholder="Описание" v-model="request.description"/>
    <button class="item" v-on:click="updateSkill">Сохранить</button>
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
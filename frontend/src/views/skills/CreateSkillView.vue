<script setup lang="ts">

import client from '@/Clients/Client'
import {ref} from 'vue'
import {useRouter} from 'vue-router'
import {toast} from 'vue3-toastify'
import {CreateSkillRequest} from "@/dto/skills/CreateSkillRequest";

const router = useRouter()
const request = ref<CreateSkillRequest>(new CreateSkillRequest())

function createSkill() {
  client.createSkill(request.value!)
      .then(response => router.push({name: 'skill', params: {skillId: response.data.id}})
          .then(() => toast('Новый навык создан', {type: toast.TYPE.SUCCESS}))
      ).catch(() => toast('Не удалось создать навык', {type: toast.TYPE.ERROR}))
}
</script>

<template>
  <div class="container">
    <h1>Создать навык</h1>
    <input class="item" placeholder="Название" v-model="request.name">
    <input class="item" placeholder="Описание" v-model="request.description"/>
    <input class="item" placeholder="Уровень" type="number" v-model="request.level"/>
    <input class="item" placeholder="Тип" v-model="request.type"/>
    <input class="item" placeholder="Стоимость" type="number" v-model="request.cost"/>
    <button class="item" v-on:click="createSkill">Создать</button>
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